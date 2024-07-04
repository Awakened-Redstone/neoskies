import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec

fun MavenPom.default() {
	url.set("https://awakenedredstone.com")
	issueManagement {
		system.set("GitHub")
		url.set("https://github.com/Awakened-Redstone/neoskies/issues")
	}
	inceptionYear.set("2024")
	developers { all }
	licenses {
		license {
			name.set("GNU Lesser General Public License version 3")
			distribution.set("repo")
			url.set("https://opensource.org/license/lgpl-3-0")
		}
	}
	scm {
		connection.set("scm:git:https://github.com/Awakened-Redstone/neoskies.git")
		developerConnection.set("scm:git:git@github.com:Awakened-Redstone/neoskies.git")
		url.set("https://github.com/Awakened-Redstone/neoskies")
	}
}

val MavenPomDeveloperSpec.all: Unit
	get() {
        AwakenedRedstone()
	}

fun MavenPomDeveloperSpec.AwakenedRedstone() {
	developer {
		id.set("Awakened-Redstone")
		name.set("Awakened Redstone")
		email.set("git@awakenedredstone.com")
		roles.addAll("owner", "maintainer")
	}
}
