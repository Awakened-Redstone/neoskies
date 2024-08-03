import com.puppycrawl.tools.checkstyle.Checker
import com.puppycrawl.tools.checkstyle.ConfigurationLoader
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions
import com.puppycrawl.tools.checkstyle.PropertiesExpander
import com.puppycrawl.tools.checkstyle.api.CheckstyleException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ValidateCheckstyleTask : DefaultTask() {
    @TaskAction
    fun validateCheckstyle() {
        try {
            val configuration = ConfigurationLoader.loadConfiguration(
                project.file("checkstyle.xml").absolutePath, // Path to your checkstyle.xml
                PropertiesExpander(System.getProperties()),
                IgnoredModulesOptions.OMIT
            )
            Checker(
                // ... (configuration and other parameters required by Checker)
            )

            println("Checkstyle configuration is valid.")
        } catch (e: CheckstyleException) {
            println("Error in Checkstyle configuration: ${e.message}")
            throw e // Fail the build on error
        }
    }
}
