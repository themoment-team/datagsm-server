package team.themoment.datagsm.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

class KmpExportProcessor(
    private val logger: KSPLogger,
    private val outputDir: File,
    private val tsOutputDir: File?,
) : SymbolProcessor {
    companion object {
        private val ARRAY_LIKE_COLLECTIONS =
            setOf(
                "kotlin.collections.List",
                "kotlin.collections.MutableList",
                "kotlin.collections.Set",
                "kotlin.collections.MutableSet",
                "kotlin.collections.Collection",
                "kotlin.collections.MutableCollection",
                "kotlin.collections.Iterable",
                "kotlin.collections.MutableIterable",
            )

        private val TS_NUMBER_TYPES =
            setOf(
                "kotlin.Long",
                "kotlin.Int",
                "kotlin.Short",
                "kotlin.Byte",
                "kotlin.Double",
                "kotlin.Float",
            )

        private val TS_STRING_TYPES =
            setOf(
                "java.time.LocalDate",
                "java.time.LocalDateTime",
                "java.time.LocalTime",
                "java.time.Instant",
                "kotlinx.datetime.LocalDate",
                "kotlinx.datetime.LocalDateTime",
                "kotlinx.datetime.LocalTime",
                "kotlinx.datetime.Instant",
            )
    }

    private data class PropertyInfo(
        val name: String,
        val serialName: String,
        val typeName: TypeName,
        val tsType: String,
        val tsOptional: Boolean,
    )

    private data class ClassInfo(
        val targetPackage: String,
        val className: String,
        val isEnum: Boolean,
        val enumEntries: List<String>,
        val properties: List<PropertyInfo>,
    )

    // Phase 1 (KSP resolution) and Phase 2 (file writing) are separated.
    // Files are written directly to the filesystem (bypassing codeGenerator.createNewFile)
    // to prevent the KSP 2.x bug: KaInvalidLifetimeOwnerAccessException caused by
    // Analysis API PSI invalidation when createNewFile triggers a second round.
    // TODO: revert to codeGenerator.createNewFile() after upgrading past KSP 2.3.6 once the
    //  PSI invalidation issue is fixed upstream — restores incremental processing support.
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver
                .getSymbolsWithAnnotation("team.themoment.datagsm.ksp.annotation.KmpExport")
                .filterIsInstance<KSClassDeclaration>()
                .toList()

        val classInfos = symbols.mapNotNull { collectClassInfo(it) }

        classInfos.forEach { writeFileDirect(it) }

        tsOutputDir?.let { writeTsDefinitions(classInfos, it) }

        return emptyList()
    }

    private fun collectClassInfo(classDecl: KSClassDeclaration): ClassInfo? {
        val targetPackage = transformPackage(classDecl.packageName.asString())
        val className = classDecl.simpleName.asString()

        return when (classDecl.classKind) {
            ClassKind.ENUM_CLASS -> {
                val entries =
                    classDecl.declarations
                        .filterIsInstance<KSClassDeclaration>()
                        .filter { it.classKind == ClassKind.ENUM_ENTRY }
                        .map { it.simpleName.asString() }
                        .toList()
                ClassInfo(targetPackage, className, isEnum = true, enumEntries = entries, properties = emptyList())
            }
            else -> {
                val properties =
                    (classDecl.primaryConstructor?.parameters ?: emptyList())
                        .mapNotNull { param ->
                            val propName = param.name?.asString() ?: return@mapNotNull null
                            val serialName = resolveSerialName(param, propName, className)
                            val resolvedType =
                                runCatching { param.type.resolve() }
                                    .getOrElse { e ->
                                        logger.error("Failed to resolve type for $className.$propName: ${e.message}")
                                        return@mapNotNull null
                                    }
                            val typeName =
                                runCatching { mapType(resolvedType) }
                                    .getOrElse { e ->
                                        logger.error("Failed to map type for $className.$propName: ${e.message}")
                                        return@mapNotNull null
                                    }
                            PropertyInfo(
                                name = propName,
                                serialName = serialName,
                                typeName = typeName,
                                tsType = mapTsType(resolvedType),
                                tsOptional = resolvedType.isMarkedNullable,
                            )
                        }
                ClassInfo(targetPackage, className, isEnum = false, enumEntries = emptyList(), properties = properties)
            }
        }
    }

    // Writes directly to the filesystem to bypass codeGenerator.createNewFile(),
    // preventing the KSP 2.x PSI invalidation bug.
    // TODO: switch back to fileSpec.writeTo(codeGenerator) once KSP no longer triggers
    //  KaInvalidLifetimeOwnerAccessException — this restores KSP-tracked output and
    //  incremental processing.
    private fun writeFileDirect(classInfo: ClassInfo) {
        val fileSpec = if (classInfo.isEnum) buildEnumFileSpec(classInfo) else buildDataClassFileSpec(classInfo)
        val pkgDir = classInfo.targetPackage.replace('.', File.separatorChar)
        val outFile = outputDir.resolve("$pkgDir/${classInfo.className}.kt")
        outFile.parentFile.mkdirs()
        outFile.writeText(fileSpec.toString())
    }

    private fun transformPackage(pkg: String): String = pkg.replace(".common.domain.", ".shared.domain.")

    private fun buildEnumFileSpec(info: ClassInfo): FileSpec {
        val enumBuilder =
            TypeSpec
                .enumBuilder(info.className)
                .addAnnotation(serializableAnnotation())
                .addAnnotation(jsExportAnnotation())
        info.enumEntries.forEach { enumBuilder.addEnumConstant(it) }
        return FileSpec
            .builder(info.targetPackage, info.className)
            .addType(enumBuilder.build())
            .build()
    }

    private fun buildDataClassFileSpec(info: ClassInfo): FileSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        val propSpecs = mutableListOf<PropertySpec>()

        for (prop in info.properties) {
            constructorBuilder.addParameter(
                ParameterSpec
                    .builder(prop.name, prop.typeName)
                    .addAnnotation(serialNameAnnotation(prop.serialName))
                    .build(),
            )
            propSpecs.add(PropertySpec.builder(prop.name, prop.typeName).initializer(prop.name).build())
        }

        val typeSpec =
            TypeSpec
                .classBuilder(info.className)
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializableAnnotation())
                .addAnnotation(jsExportAnnotation())
                .primaryConstructor(constructorBuilder.build())
                .addProperties(propSpecs)
                .build()

        return FileSpec
            .builder(info.targetPackage, info.className)
            .addType(typeSpec)
            .build()
    }

    private fun resolveSerialName(
        param: KSValueParameter,
        propName: String,
        ownerClassName: String,
    ): String {
        val value =
            param.annotations
                .find { it.shortName.asString() == "JsonProperty" }
                ?.arguments
                ?.find { it.name?.asString() == "value" || it.name == null }
                ?.value as? String
        if (value == null) {
            logger.warn("@KmpExport $ownerClassName.$propName has no @field:JsonProperty — using property name '$propName' as SerialName")
        }
        return value ?: propName
    }

    private fun mapType(type: KSType): TypeName {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: ""

        if (qualifiedName in ARRAY_LIKE_COLLECTIONS) {
            val elementArg = type.arguments.firstOrNull()
            val elementType =
                when (elementArg?.variance) {
                    Variance.STAR, null -> STAR
                    else -> elementArg.type?.resolve()?.let { mapType(it) } ?: STAR
                }
            val arrayType = ClassName("kotlin", "Array").parameterizedBy(elementType)
            return if (type.isMarkedNullable) arrayType.copy(nullable = true) else arrayType
        }

        val baseClassName =
            javaTimeToKotlinxDatetime(qualifiedName)
                ?: if (qualifiedName.contains(".common.domain.")) {
                    val remapped = qualifiedName.replace(".common.domain.", ".shared.domain.")
                    ClassName(remapped.substringBeforeLast("."), declaration.simpleName.asString())
                } else {
                    ClassName(qualifiedName.substringBeforeLast("."), declaration.simpleName.asString())
                }

        val resolved =
            if (type.arguments.isEmpty()) {
                baseClassName
            } else {
                val mappedArgs =
                    type.arguments.map { arg ->
                        when (arg.variance) {
                            Variance.STAR -> STAR
                            else -> arg.type?.resolve()?.let { mapType(it) } ?: STAR
                        }
                    }
                baseClassName.parameterizedBy(*mappedArgs.toTypedArray())
            }

        return if (type.isMarkedNullable) resolved.copy(nullable = true) else resolved
    }

    private fun javaTimeToKotlinxDatetime(qualifiedName: String): ClassName? =
        when (qualifiedName) {
            "java.time.LocalDate" -> ClassName("kotlinx.datetime", "LocalDate")
            "java.time.LocalDateTime" -> ClassName("kotlinx.datetime", "LocalDateTime")
            "java.time.LocalTime" -> ClassName("kotlinx.datetime", "LocalTime")
            "java.time.Instant" -> ClassName("kotlinx.datetime", "Instant")
            else -> null
        }

    // Maps a Kotlin type to a plain TypeScript type string (matching the raw JSON shape).
    // Nested @KmpExport DTOs and enums are flattened to their simple name (no namespace).
    private fun mapTsType(type: KSType): String {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: ""

        if (qualifiedName in ARRAY_LIKE_COLLECTIONS) {
            val elementArg = type.arguments.firstOrNull()
            val elementType =
                when (elementArg?.variance) {
                    Variance.STAR, null -> "unknown"
                    else -> elementArg.type?.resolve()?.let { mapTsElementType(it) } ?: "unknown"
                }
            return "$elementType[]"
        }

        return when (qualifiedName) {
            "kotlin.String", "kotlin.Char" -> "string"
            "kotlin.Boolean" -> "boolean"
            in TS_NUMBER_TYPES -> "number"
            in TS_STRING_TYPES -> "string"
            else -> declaration.simpleName.asString()
        }
    }

    // Array elements keep their nullability inline as `(T | null)[]`, unlike top-level
    // properties which become optional fields.
    private fun mapTsElementType(type: KSType): String {
        val base = mapTsType(type)
        return if (type.isMarkedNullable) "($base | null)" else base
    }

    // Emits a single flat index.d.ts: CommonApiResponse<T> preamble, enums as string-literal
    // unions, and DTOs as interfaces. Written directly to the filesystem like the Kotlin output.
    private fun writeTsDefinitions(
        classInfos: List<ClassInfo>,
        outDir: File,
    ) {
        val duplicates =
            classInfos
                .groupBy { it.className }
                .filterValues { it.size > 1 }
                .keys
        if (duplicates.isNotEmpty()) {
            logger.error("KmpExport TS emitter found duplicate class names after flattening: ${duplicates.joinToString(", ")}")
            return
        }

        val sb = StringBuilder()
        sb.appendLine("// AUTO-GENERATED by KmpExport KSP processor — DO NOT EDIT")
        sb.appendLine()
        sb.appendLine("export interface CommonApiResponse<T> {")
        sb.appendLine("  status: string;")
        sb.appendLine("  code: number;")
        sb.appendLine("  message: string;")
        sb.appendLine("  data: T;")
        sb.appendLine("}")
        sb.appendLine()

        classInfos
            .filter { it.isEnum }
            .sortedBy { it.className }
            .forEach { info ->
                val union = info.enumEntries.joinToString(" | ") { "'$it'" }
                sb.appendLine("export type ${info.className} = $union;")
            }
        if (classInfos.any { it.isEnum }) sb.appendLine()

        classInfos
            .filter { !it.isEnum }
            .sortedBy { it.className }
            .forEach { info ->
                sb.appendLine("export interface ${info.className} {")
                info.properties.forEach { prop ->
                    val optional = if (prop.tsOptional) "?" else ""
                    sb.appendLine("  ${prop.serialName}$optional: ${prop.tsType};")
                }
                sb.appendLine("}")
                sb.appendLine()
            }

        outDir.mkdirs()
        outDir.resolve("index.d.ts").writeText(sb.toString().trimEnd() + "\n")
    }

    private fun serializableAnnotation() = AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build()

    private fun jsExportAnnotation() = AnnotationSpec.builder(ClassName("kotlin.js", "JsExport")).build()

    private fun serialNameAnnotation(name: String) =
        AnnotationSpec
            .builder(ClassName("kotlinx.serialization", "SerialName"))
            .addMember("%S", name)
            .build()

    private fun suppressAnnotation(vararg keys: String) =
        AnnotationSpec
            .builder(ClassName("kotlin", "Suppress"))
            .apply { keys.forEach { addMember("%S", it) } }
            .build()
}
