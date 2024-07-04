import java.io.File
import com.modrinth.minotaur.dependencies.ModDependency

plugins {
    id("signing")
    id("maven-publish")
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
}

val CHANGELOG: String =
    if (file("CHANGELOG.md").exists()) {
        file("CHANGELOG.md").readText()
    } else {
        "No changelog provided"
    }

fun file(path: String): File {
    return rootProject.file(path)
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/generated")
        }
    }

    create("testmod") {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

loom {
    accessWidenerPath.set(file("src/main/resources/neoskies.accesswidener"))

    runs {
        create("datagenServer") {
            server()
            name = "Data Generation"
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=neoskies")

            //ideConfigGenerated = true
            runDir = "build/datagen"
        }

        create("testmodServer") {
            server()
            name = "Testmod Server"
            source(sourceSets.getByName("testmod"))
        }

        create("testmodClient") {
            client()
            name = "Testmod Client"
            runDir("run_client")
            source(sourceSets.getByName("testmod"))
        }
    }
}

var archivesBaseName: String = property("archives_base_name").toString()
base {
    archivesName.set(property("archives_base_name").toString())
}
version = property("mod_version")!!
group = property("maven_group")!!

repositories {
    mavenLocal()
	maven("https://maven.nucleoid.xyz")
	maven("https://maven.awakenedredstone.com")
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	maven("https://maven.ladysnake.org/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://jitpack.io")
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    //region Fabric
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")
    //endregion
    // region Cardinal Components
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-api:${property("cardinal_components_api")}")
	include("org.ladysnake.cardinal-components-api:cardinal-components-base:${property("cardinal_components_api")}")
	include("org.ladysnake.cardinal-components-api:cardinal-components-world:${property("cardinal_components_api")}")
    // endregion
    // region Nucleoid
    modImplementation(include("xyz.nucleoid:fantasy:${property("fantasy")}") as Any)
    modImplementation(include("xyz.nucleoid:stimuli:${property("stimuli")}") as Any)
    modImplementation(include("xyz.nucleoid:server-translations-api:${property("server_translations_api")}") as Any)
    //endregion
    // region Patbox
    modImplementation(include("eu.pb4:sgui:${property("server_gui")}") as Any)
    modImplementation(include("eu.pb4:placeholder-api:${property("placeholder_api")}") as Any)
    modImplementation(include("eu.pb4:common-economy-api:${property("common_economy_api")}") as Any)
    modImplementation(include("eu.pb4:common-protection-api:${property("common_protection_api")}") as Any)
    //region Polymer
    modImplementation(include("eu.pb4:polymer-core:${property("polymer")}") as Any)
    modImplementation(include("eu.pb4:polymer-resource-pack:${property("polymer")}") as Any)
    modImplementation(include("eu.pb4:polymer-virtual-entity:${property("polymer")}") as Any)
    //endregion
    //endregion
    // region Others
    modImplementation(include("maven.modrinth:particleanimationlib:0.0.2+1.20") as Any)
    modImplementation(include("me.lucko:fabric-permissions-api:${property("fabric_permission_api")}") as Any)
    //endregion
    // region Non mod dependencies
    include(api("blue.endless:jankson:${property("jankson_version")}") as Any)
    //endregion

    // region Tests
    "testmodImplementation"(sourceSets.main.get().output)
    //endregion
}

tasks.processResources {
    val map = mapOf(
        "version" to version
    )

    inputs.properties(map)

    filesMatching("fabric.mod.json") {
        expand(map)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
	withSourcesJar()
    withJavadocJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${property("archivesBaseName")}" }
    }
}

publishing {
    repositories {
        maven {
            name = "maven"
            url = uri(project.mavenRepositoryUrl)
            credentials {
                username = project.mavenRepositoryUsername
                password = project.mavenRepositoryPassword
            }
        }
    }
    publications {
        create<MavenPublication>("main") {
            from(components["java"])
            pom.default()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["main"])
}

modrinth {
    val projectVersion: String = property("mod_version").toString()
    val projectVersionNumber: List<String> = projectVersion.split(Regex("-"), 2)

    var releaseName = "Release ${projectVersionNumber[0]}"
    if (projectVersion.contains("beta")) {
        val projectBeta: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Beta ${projectBeta[1]}"
        versionType = "beta"
    } else if (projectVersion.contains("alpha")) {
        val projectAlpha: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Alpha ${projectAlpha[1]}"
        versionType = "alpha"
    } else if (projectVersion.contains("rc")) {
        val projectRC: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
        releaseName = "${projectVersionNumber[0]} - Release Candidate ${projectRC[1]}"
        versionType = "beta"
    }

    token = System.getenv("MODRINTH_TOKEN")
    projectId = "YowGXm51"
    versionName = releaseName
    changelog = CHANGELOG
    uploadFile = tasks.getByName("remapJar")
    syncBodyFrom = file("README.md").readText()
    dependencies = listOf(
        ModDependency("fabric-api", "required"),
        ModDependency("luckperms", "optional"),
        ModDependency("placeholder-api", "embedded")
    )
}
