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
    }

    private data class PropertyInfo(
        val name: String,
        val serialName: String,
        val typeName: TypeName,
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
    // This workaround also forces ksp.incremental=false in gradle.properties.
    // TODO: not version-gated. As of KSP 2.3.8 there is no release note confirming this
    //  is fixed, and reverting requires also moving datagsm-shared's srcDir back to the
    //  KSP standard output path. Before reverting to codeGenerator.createNewFile(), prove
    //  the multi-round PSI invalidation no longer reproduces (a single clean build passing
    //  is not sufficient — it only triggers under specific multi-round conditions).
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver
                .getSymbolsWithAnnotation("team.themoment.datagsm.ksp.annotation.KmpExport")
                .filterIsInstance<KSClassDeclaration>()
                .toList()

        val classInfos = symbols.mapNotNull { collectClassInfo(it) }

        classInfos.forEach { writeFileDirect(it) }

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
                            val typeName =
                                runCatching { mapType(param.type.resolve()) }
                                    .getOrElse { e ->
                                        logger.error("Failed to resolve type for $className.$propName: ${e.message}")
                                        return@mapNotNull null
                                    }
                            PropertyInfo(propName, serialName, typeName)
                        }
                ClassInfo(targetPackage, className, isEnum = false, enumEntries = emptyList(), properties = properties)
            }
        }
    }

    // Writes directly to the filesystem to bypass codeGenerator.createNewFile().
    // See the process() comment above for the rationale and revert conditions.
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

    private fun serializableAnnotation() = AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build()

    private fun jsExportAnnotation() = AnnotationSpec.builder(ClassName("kotlin.js", "JsExport")).build()

    private fun serialNameAnnotation(name: String) =
        AnnotationSpec
            .builder(ClassName("kotlinx.serialization", "SerialName"))
            .addMember("%S", name)
            .build()
}
