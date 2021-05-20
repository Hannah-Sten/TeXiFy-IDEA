package nl.hannahsten.texifyidea.run

import com.intellij.configurationStore.Property
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.openapi.components.StoredPropertyBase
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XMap
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.PdflatexCompiler
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
}


fun <U, T> transformed(stored: KMutableProperty0<T>, transform: (T) -> T): ReadWriteProperty<U, T> {
    return object : ReadWriteProperty<U, T> {
        override fun getValue(thisRef: U, property: KProperty<*>) = stored.getValue(thisRef, property)

        override fun setValue(thisRef: U, property: KProperty<*>, value: T) {
            stored.setValue(thisRef, property, transform(value))
        }
    }
}
