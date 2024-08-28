import java.io.File
import com.modrinth.minotaur.dependencies.ModDependency

//region Setup
plugins {
    signing
    checkstyle
    `maven-publish`
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
}

loom {
    accessWidenerPath.set(file("src/main/resources/neoskies.accesswidener"))
    splitEnvironmentSourceSets()

    mods {
        create("neoskies") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
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

    create("testmodClient") {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath

        compileClasspath += sourceSets.getByName("client").compileClasspath
        runtimeClasspath += sourceSets.getByName("client").runtimeClasspath

        compileClasspath += sourceSets.getByName("testmod").compileClasspath
        runtimeClasspath += sourceSets.getByName("testmod").runtimeClasspath
    }
}

loom {
    runs {
        create("datagenServer") {
            server()
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=neoskies")
            runDir("build/datagen")
            ideConfigGenerated(true)
        }
        create("testmodServer") {
            server()
            name("Testmod Server")
            ideConfigGenerated(true)
            source(sourceSets.getByName("testmod"))
        }
        create("testmodClient") {
            client()
            name("Testmod Client")
            runDir("run_client")
            //TODO: Generate dev world
            programArg("--quickPlaySingleplayer \"world\"")
            ideConfigGenerated(true)
            source(sourceSets.getByName("testmodClient"))
        }
    }
}
//endregion

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
    maven("https://maven.deftu.dev/snapshots")
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
    modImplementation(include("maven.modrinth:apel:0.1.0+1.20.6") as Any)
    modImplementation(include("me.lucko:fabric-permissions-api:${property("fabric_permission_api")}") as Any)
    //endregion
    // region Non mod dependencies
    include(api("blue.endless:jankson:${property("jankson_version")}") as Any)
    include(api("com.ezylang:EvalEx:3.3.0") as Any)
    //endregion

    //region Client
    "clientImplementation"("dev.deftu:dearimguimc-1.20.6-fabric:0.1.0") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    listOf(
        "binding",
        "lwjgl3",
        "natives-windows",
        "natives-linux",
        "natives-macos"
    ).forEach { module ->
        val version = "1.86.11"
        implementation("io.github.spair:imgui-java-$module:$version") {
            exclude(group = "org.lwjgl")
        }
    }
    //endregion

    // region Tests
    "testmodImplementation"(sourceSets.main.get().output)
    checkstyle(project(":checkstyle-rules"))
    //endregion
}

//region Misc
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
        rename { "${it}_${archivesBaseName}" }
    }
}

checkstyle {
    configFile = rootProject.file("checkstyle.xml")
    toolVersion = "10.17.0"
}

tasks.register<ValidateCheckstyleTask>("validateCheckstyle") {
    group = "verification" // Optional: Add the task to a group
    description = "Validates the Checkstyle configuration file."
}
//endregion

//region Publishing
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

val CHANGELOG: String =
    if (file("CHANGELOG.md").exists()) {
        file("CHANGELOG.md").readText()
    } else {
        "No changelog provided"
    }

fun file(path: String): File {
    return rootProject.file(path)
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
//endregion
