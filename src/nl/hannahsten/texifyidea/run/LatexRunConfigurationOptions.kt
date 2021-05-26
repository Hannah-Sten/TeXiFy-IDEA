package nl.hannahsten.texifyidea.run

import com.intellij.configurationStore.Property
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XMap
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.PdflatexCompiler
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Options which are global to the run configuration (i.e. not specific to a certain step).
 */
class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    @get:OptionTag("compiler", converter = LatexCompiler.Converter::class)
    var compiler by property<LatexCompiler?>(PdflatexCompiler) { it is PdflatexCompiler }

    @get:OptionTag("compilerArguments")
    var compilerArguments by string() // todo transformed(string()) { it.trim() }

    @get:OptionTag("workingDirectory")
    var workingDirectory by string()

    @Property(description = "Environment variables")
    @get:XMap(propertyElementName = "envs", entryTagName = "env", keyAttributeName = "name")
    var env by linkedMap<String, String>()

    @get:OptionTag("passParentEnvs")
    var isPassParentEnv by property(true)

    // Derived property, so not saved
    var environmentVariables: EnvironmentVariablesData
        get() = EnvironmentVariablesData.create(env, isPassParentEnv)
        set(value) {
            env
            isPassParentEnv = value.isPassParentEnvs
        }

    @get:OptionTag("outputFormat")
    var outputFormat by enum(LatexCompiler.OutputFormat.PDF)

    /**
     * Use [LatexRunConfiguration.getLatexDistributionType] to take the Project SDK into account.
     */
    @get:OptionTag("latexDistribution")
    internal var latexDistribution by enum(LatexDistributionType.PROJECT_SDK)

    fun setDefaultDistribution(project: Project) {
        latexDistribution = LatexSdkUtil.getDefaultLatexDistributionType(project)
    }

    /** Whether the run configuration has already been run or not, since it has been created */
    @get:OptionTag("hasBeenRun")
    var hasBeenRun by property(false)

    @get:OptionTag("mainFile", converter = LatexRunConfigurationDirectoryOption.Converter::class)
    var mainFile by property(LatexRunConfigurationDirectoryOption()) { it.isDefault() }

    @get:OptionTag("outputPath", converter = LatexRunConfigurationDirectoryOption.Converter::class)
    var outputPath by property(LatexRunConfigurationOutputPathOption()) { it.isDefault() }

    @get:OptionTag("auxilPath", converter = LatexRunConfigurationDirectoryOption.Converter::class)
    var auxilPath by property(LatexRunConfigurationOutputPathOption()) { it.isDefault() }
}


fun <U, T> transformed(stored: KMutableProperty0<T>, transform: (T) -> T): ReadWriteProperty<U, T> {
    return object : ReadWriteProperty<U, T> {
        override fun getValue(thisRef: U, property: KProperty<*>) = stored.getValue(thisRef, property)

        override fun setValue(thisRef: U, property: KProperty<*>, value: T) {
            stored.setValue(thisRef, property, transform(value))
        }
    }
}
