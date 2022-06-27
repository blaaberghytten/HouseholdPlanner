plugins {
    java
    id("org.openapi.generator") version "6.0.0"
}

group = Default.GROUP

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

/**
 * Registers a task of type <pre>GenerateTask</pre> that can generate a client based on the API specification located at
 * the path specified in the <pre>inputSpec</pre> property.
 */
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateApi") {
    group = "openapi tools"

    setProperty("groupId", project.group)
    setProperty("id", project.name)
    setProperty("version", project.version)

    setProperty("generatorName", "spring")
    setProperty("inputSpec", "$projectDir/src/main/resources/${project.name}-api.yaml")
    setProperty("outputDir", "$buildDir/generated/api/${project.name}")

    setProperty("apiPackage", "${project.group}.generated.api")
    setProperty("modelPackage", "${project.group}.generated.model")

    setProperty("enablePostProcessFile", true)
    setProperty("skipOverwrite", false)

    setProperty(
        "configOptions",
        mapOf(
            "configPackage" to "${project.group}.${dashCaseToLowerConcatCase(project.name)}.config",
            "dateLibrary" to "java8",
            "library" to "spring-boot",
            "serializationLibrary" to "jackson",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "openApiNullable" to "false"
        )
    )
}

/**
 * Registers a task of type <pre>Copy</pre> that first generates the API and then copies it into the source folder of
 * the project.
 */
tasks.register<Copy>("generateAndCopyApi") {
    group = "openapi tools"

    dependsOn("generateApi")

    val packagePath = project.group.toString().replace(".", "/")
    val targetPath = "src/main/java/$packagePath/generated"

    from("$buildDir/generated/api/${project.name}/$targetPath")
    into("${projectDir}/$targetPath")
}

tasks.compileJava {
    dependsOn("generateAndCopyApi")
}

tasks.register<Delete>("cleanGenerated") {
    group = "build"
    description = "Deletes all generated source directories."

    file(projectDir).walk()
        .onEnter { it.name != buildDir.name }
        .filter { it.name == "generated" }
        .forEach { delete(it) }
}

/**
 * Converts a string written in dash case (i.e. words separated by dashes) into lower concatenation case (i.e. all words
 * in lower case without separation characters between them).
 */
fun dashCaseToLowerConcatCase(string: String) = string.toLowerCase().replace("-", "")

object Default {
    const val GROUP = "dk.blaaberghytten"

    const val IS_PRODUCTION = true
    const val IS_TEST = false
}
