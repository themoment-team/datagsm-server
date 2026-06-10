package team.themoment.datagsm.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
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

class KmpExportProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    // Guards against a second KSP round: createNewFile schedules another round over the
    // generated files, and re-running resolution there triggers the KSP 2.x Analysis API
    // PSI invalidation (KaInvalidLifetimeOwnerAccessException). Returning early before any
    // resolution makes the second round a no-op, so createNewFile is safe.
    private var generated = false

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
        val tsReferences: Set<String>,
    )

    private data class ClassInfo(
        val targetPackage: String,
        val className: String,
        val isEnum: Boolean,
        val enumEntries: List<String>,
        val properties: List<PropertyInfo>,
        val typeParameters: List<String> = emptyList(),
    )

    // Emits generated files through codeGenerator.createNewFile so the build tool (Gradle KSP
    // or Bazel rules_kotlin) captures them as declared outputs. The `generated` guard skips
    // the createNewFile-triggered second round before any resolution, avoiding the KSP 2.x
    // Analysis API PSI invalidation that previously forced direct filesystem writes.
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols =
            resolver
                .getSymbolsWithAnnotation("team.themoment.datagsm.ksp.annotation.KmpExport")
                .filterIsInstance<KSClassDeclaration>()
                .toList()

        val classInfos = symbols.mapNotNull { collectClassInfo(it) }
        if (classInfos.isEmpty()) return emptyList()

        classInfos.forEach { writeKotlinFile(it) }
        writeTsDefinitions(classInfos)

        generated = true
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
                            val tsReferences = mutableSetOf<String>()
                            PropertyInfo(
                                name = propName,
                                serialName = serialName,
                                typeName = typeName,
                                tsType = mapTsType(resolvedType, tsReferences),
                                tsOptional = resolvedType.isMarkedNullable,
                                tsReferences = tsReferences,
                            )
                        }
                val typeParameters = classDecl.typeParameters.map { it.name.asString() }
                ClassInfo(
                    targetPackage,
                    className,
                    isEnum = false,
                    enumEntries = emptyList(),
                    properties = properties,
                    typeParameters = typeParameters,
                )
            }
        }
    }

    private fun writeKotlinFile(classInfo: ClassInfo) {
        val fileSpec = if (classInfo.isEnum) buildEnumFileSpec(classInfo) else buildDataClassFileSpec(classInfo)
        codeGenerator
            .createNewFile(Dependencies(aggregating = true), classInfo.targetPackage, classInfo.className)
            .use { it.write(fileSpec.toString().toByteArray()) }
    }

    private fun transformPackage(pkg: String): String = pkg.replace(".common.domain.", ".shared.domain.")

    private fun buildEnumFileSpec(info: ClassInfo): FileSpec {
        val enumBuilder =
            TypeSpec
                .enumBuilder(info.className)
                .addAnnotation(serializableAnnotation())
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
    // Every flattened (non-builtin) name is collected into `refs` so the emitter can verify
    // it is actually exported, instead of silently emitting a dangling type reference.
    private fun mapTsType(
        type: KSType,
        refs: MutableSet<String>,
    ): String {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: ""

        if (qualifiedName in ARRAY_LIKE_COLLECTIONS) {
            val elementArg = type.arguments.firstOrNull()
            val elementType =
                when (elementArg?.variance) {
                    Variance.STAR, null -> "unknown"
                    else -> elementArg.type?.resolve()?.let { mapTsElementType(it, refs) } ?: "unknown"
                }
            return "$elementType[]"
        }

        if (qualifiedName == "kotlin.collections.Map" || qualifiedName == "kotlin.collections.MutableMap") {
            val keyType =
                type.arguments
                    .getOrNull(0)
                    ?.type
                    ?.resolve()
                    ?.let { mapTsType(it, refs) } ?: "string"
            val valueType =
                type.arguments
                    .getOrNull(1)
                    ?.type
                    ?.resolve()
                    ?.let { mapTsType(it, refs) } ?: "unknown"
            return "Record<$keyType, $valueType>"
        }

        return when (qualifiedName) {
            "kotlin.String", "kotlin.Char" -> "string"
            "kotlin.Boolean" -> "boolean"
            "kotlin.Any" -> "any"
            in TS_NUMBER_TYPES -> "number"
            in TS_STRING_TYPES -> "string"
            else -> declaration.simpleName.asString().also { refs.add(it) }
        }
    }

    // Array elements keep their nullability inline as `(T | null)[]`, unlike top-level
    // properties which become optional fields.
    private fun mapTsElementType(
        type: KSType,
        refs: MutableSet<String>,
    ): String {
        val base = mapTsType(type, refs)
        return if (type.isMarkedNullable) "($base | null)" else base
    }

    // Builds the KSP-free TsModel and delegates rendering/validation to TsDefinitionEmitter.
    // Validation errors are reported via logger.error, which fails the KSP round, instead of
    // emitting an index.d.ts with dangling type references that only tsc would later reject.
    private fun writeTsDefinitions(classInfos: List<ClassInfo>) {
        val model =
            TsModel(
                enums =
                    classInfos
                        .filter { it.isEnum }
                        .map { TsEnum(it.className, it.enumEntries) },
                interfaces =
                    classInfos
                        .filter { !it.isEnum }
                        .map { info ->
                            TsInterface(
                                name = info.className,
                                typeParameters = info.typeParameters,
                                properties =
                                    info.properties.map { prop ->
                                        TsProperty(prop.serialName, prop.tsType, prop.tsOptional, prop.tsReferences)
                                    },
                            )
                        },
            )

        val errors = TsDefinitionEmitter.validate(model)
        if (errors.isNotEmpty()) {
            errors.forEach { logger.error(it) }
            return
        }

        // Emitted as a KSP resource (index.d.ts) so the build tool captures it as a declared
        // output; the assembleTsPackage step reads it from the KSP resource output dir.
        codeGenerator
            .createNewFileByPath(Dependencies(aggregating = true), "index", extensionName = "d.ts")
            .use { it.write(TsDefinitionEmitter.render(model).toByteArray()) }
    }

    private fun serializableAnnotation() = AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build()

    private fun serialNameAnnotation(name: String) =
        AnnotationSpec
            .builder(ClassName("kotlinx.serialization", "SerialName"))
            .addMember("%S", name)
            .build()
}
