import org.gradle.api.Project

val Project.mavenRepositoryUrl: String
	get() = System.getenv("MAVEN_REPO") ?: ""

val Project.mavenRepositoryUsername: String
	get() = System.getenv("MAVEN_AUTH_USERNAME") ?: ""

val Project.mavenRepositoryPassword: String
	get() = System.getenv("MAVEN_AUTH_KEY") ?: ""
