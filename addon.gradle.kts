import com.gtnewhorizons.gtnhgradle.GTNHGradlePlugin
import com.gtnewhorizons.retrofuturagradle.mcp.ReobfuscatedJar
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.darkhax.curseforgegradle.CurseForgeGradlePlugin
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

val fentMavenName = "Fent Maven"
val fentMavenUrl = uri("https://maven.fentanylsolutions.org/releases")
val curseForgeLimitedMessage =
    "For enhanced file-related functionality, download FentLib from a different source than CurseForge"

fun RepositoryHandler.keepFentMavenFirst() {
    fun currentFentRepo(): MavenArtifactRepository? = withType(MavenArtifactRepository::class.java)
        .firstOrNull { it.url == fentMavenUrl || it.name == fentMavenName }

    fun promoteFentRepo() {
        val fentRepo = currentFentRepo() ?: maven {
            name = fentMavenName
            url = fentMavenUrl
        }
        if (firstOrNull() !== fentRepo) {
            remove(fentRepo)
            addFirst(fentRepo)
        }
    }

    promoteFentRepo()
    whenObjectAdded {
        promoteFentRepo()
    }
}

gradle.allprojects {
    repositories.keepFentMavenFirst()
    buildscript.repositories.keepFentMavenFirst()
}

val configuredCurseForgeProjectId = providers.gradleProperty("curseForgeProjectId")
    .orNull
    ?.trim()
    .orEmpty()
val configuredCurseForgeRelations = providers.gradleProperty("curseForgeRelations")
    .orNull
    ?.trim()
    .orEmpty()
val configuredMinecraftVersion = providers.gradleProperty("minecraftVersion")
    .orNull
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "1.7.10"
val configuredUsesMixins = providers.gradleProperty("usesMixins")
    .orNull
    ?.trim()
    ?.equals("true", ignoreCase = true) == true

extensions.findByType<GTNHGradlePlugin.GTNHExtension>()
    ?.configuration
    ?.curseForgeProjectId = ""

val sourceSets = extensions.getByType<SourceSetContainer>()
val mainSourceSet = sourceSets.named("main").get()
val curseForgeReplacementSourceSet = sourceSets.create("curseforgeReplacement") {
    java.srcDir("src/curseforge/java")
    resources.setSrcDirs(emptyList<String>())
    compileClasspath = mainSourceSet.compileClasspath + mainSourceSet.output
    runtimeClasspath = output + compileClasspath
}

tasks.named<JavaCompile>(curseForgeReplacementSourceSet.compileJavaTaskName).configure {
    sourceCompatibility = tasks.named<JavaCompile>("compileJava").get().sourceCompatibility
    targetCompatibility = tasks.named<JavaCompile>("compileJava").get().targetCompatibility
    options.encoding = "UTF-8"
    options.annotationProcessorPath = configurations.named("annotationProcessor").get()
}

val curseForgeMcmodInfoFile = layout.buildDirectory.file("generated/curseforgeResources/mcmod.info")
val generateCurseForgeMcmodInfo = tasks.register("generateCurseForgeMcmodInfo") {
    val processedMcmodInfo = layout.buildDirectory.file("resources/main/mcmod.info")

    notCompatibleWithConfigurationCache("Uses script-local JSON mutation for a generated CurseForge-only mcmod.info.")
    dependsOn(tasks.named("processResources"))
    inputs.file(processedMcmodInfo)
    outputs.file(curseForgeMcmodInfoFile)

    doLast {
        val inputFile = processedMcmodInfo.get().asFile
        val parsed = JsonSlurper().parse(inputFile) as MutableMap<*, *>
        val modList = parsed["modList"] as? List<*> ?: emptyList<Any>()

        for (modEntry in modList) {
            @Suppress("UNCHECKED_CAST")
            val mod = modEntry as? MutableMap<String, Any?> ?: continue
            val currentDescription = mod["description"]?.toString().orEmpty()
            mod["description"] = if (currentDescription.contains(curseForgeLimitedMessage)) {
                currentDescription
            } else if (currentDescription.isBlank()) {
                curseForgeLimitedMessage
            } else {
                "$currentDescription\n\n$curseForgeLimitedMessage"
            }
        }

        val outputFile = curseForgeMcmodInfoFile.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(parsed)) + "\n")
    }
}

val curseForgeJar = tasks.register<Jar>("curseForgeJar") {
    group = "build"
    description = "Assembles a CurseForge-specific dev jar with the AWT-only FileUtil replacement."

    val normalJar = tasks.named<Jar>("jar")

    dependsOn(normalJar, tasks.named(curseForgeReplacementSourceSet.classesTaskName), generateCurseForgeMcmodInfo)
    archiveClassifier.set("curseforge-dev")
    manifest {
        attributes(
            "FMLCorePluginContainsFMLMod" to "true",
            "FMLCorePlugin" to "org.fentanylsolutions.fentlib.core.EarlyMixinLoader",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.fentlib.json",
            "ForceLoadAsMod" to "true",
        )
    }

    from(normalJar.map { zipTree(it.archiveFile.get().asFile) }) {
        exclude(
            "META-INF/MANIFEST.MF",
            "mcmod.info",
            "org/fentanylsolutions/fentlib/util/FileUtil.class",
            "org/fentanylsolutions/fentlib/util/FileUtil\$*.class",
        )
    }
    from(curseForgeReplacementSourceSet.output.classesDirs) {
        include(
            "org/fentanylsolutions/fentlib/util/FileUtil.class",
            "org/fentanylsolutions/fentlib/util/FileUtil\$*.class",
        )
    }
    from(curseForgeMcmodInfoFile) {
    }
}

val reobfCurseForgeJar = tasks.named<ReobfuscatedJar>("reobfCurseForgeJar") {
    archiveClassifier.set("curseforge")
    getExtraSrgFiles().from(layout.buildDirectory.file("tmp/mixins/mixins.srg"))
}

tasks.named("assemble").configure {
    dependsOn(reobfCurseForgeJar)
}

if (configuredCurseForgeProjectId.isNotEmpty()) {
    plugins.apply(CurseForgeGradlePlugin::class.java)

    val changelogFile = file(System.getenv("CHANGELOG_FILE") ?: "CHANGELOG.md")
    val modVersionProvider = providers.provider {
        val extras = extensions.extraProperties
        if (extras.has("modVersion")) {
            extras.get("modVersion").toString()
        } else {
            version.toString()
        }
    }

    val publishCurseforge = tasks.register<TaskPublishCurseForge>("publishCurseforge") {
        group = "publishing"
        description = "Publishes the CurseForge-specific FentLib jar to CurseForge."
        dependsOn(reobfCurseForgeJar)

        apiToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        disableVersionDetection()
        val artifact = upload(configuredCurseForgeProjectId, reobfCurseForgeJar.flatMap { it.archiveFile })
        if (changelogFile.exists()) {
            artifact.changelogType = "markdown"
            artifact.changelog = changelogFile
        }
        artifact.releaseType = modVersionProvider.map { if (it.endsWith("-pre")) "beta" else "release" }
        artifact.addGameVersion(configuredMinecraftVersion, "Forge")
        artifact.addModLoader("Forge")

        configuredCurseForgeRelations.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { relation ->
                val parts = relation.split(":", limit = 2)
                if (parts.size == 2) {
                    artifact.addRelation(parts[1], parts[0])
                }
            }
        if (configuredUsesMixins) {
            artifact.addRelation("unimixins", "requiredDependency")
        }
    }

    if (providers.environmentVariable("CURSEFORGE_TOKEN").orNull != null) {
        tasks.named("publish").configure {
            dependsOn(publishCurseforge)
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    exclude("META-INF/services/javax.imageio.spi.*")
}

tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        // WawelAuth GUI stack is client-only. Strip these from dedicated-server
        // runtime right before launch (GTNH setup appends classpath later).
        doFirst("wawelauthStripClientOnlyMods") {
            classpath = classpath.filter { file ->
                val n = file.name
                !n.contains("ModularUI2", ignoreCase = true) && !n.contains("Baubles-Expanded", ignoreCase = true) && !n.contains("angelica", ignoreCase = true)
            }
        }
    }
}
