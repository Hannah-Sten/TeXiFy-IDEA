package nl.hannahsten.texifyidea.run.latex

import com.intellij.configurationStore.Property
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.StoredProperty
import com.intellij.openapi.components.StoredPropertyBase
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    @get:OptionTag("compiler_arguments")
    var compilerArguments by transformed(string()) { it?.trim() }

    @get:OptionTag("working_directory")
    var workingDirectory by string()

    @Property(description = "Environment variables")
    @get:XMap(propertyElementName = "envs", entryTagName = "env", keyAttributeName = "name")
    var env by linkedMap<String, String>()

    @get:OptionTag("passParentEnvs")
    var isPassParentEnv by property(true)
}


fun <T> transformed(stored: StoredPropertyBase<T>, transform: (T) -> T): ReadWriteProperty<BaseState, T> {
    return object : ReadWriteProperty<BaseState, T> by stored {
        operator fun provideDelegate(thisRef: Any, property: KProperty<*>): ReadWriteProperty<BaseState, T> {
            return stored.provideDelegate(thisRef, property)
        }

        fun provideDelegate(thisRef: Any, propertyName: String): StoredProperty<T> {
            return stored.provideDelegate(thisRef, propertyName)
        }

        override fun setValue(thisRef: BaseState, property: KProperty<*>, value: T) {
            stored.setValue(thisRef, property, transform(value))
        }
    }
}
