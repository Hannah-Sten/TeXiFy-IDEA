package nl.hannahsten.texifyidea.run.latex

import com.intellij.configurationStore.Property
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.StoredPropertyBase
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

    @get:OptionTag("compiler")
    var compiler by string()

    @get:OptionTag("compiler_arguments")
    var compilerArguments by string()

    @get:OptionTag("working_directory")
    var workingDirectory by string()

    @Property(description = "Environment variables")
    @get:XMap(propertyElementName = "envs", entryTagName = "env", keyAttributeName = "name")
    var env by linkedMap<String, String>()

    @get:OptionTag("passParentEnvs")
    var isPassParentEnv by property(true)
}

interface Serializer<T, S> {

    fun serialize(value: T): S

    fun deserialize(value: S): T
}

fun <U, T, S> serialized(stored: KMutableProperty0<S>, serializer: Serializer<T, S>): ReadWriteProperty<U, T> {
    return object : ReadWriteProperty<U, T> {

        private var isInitialized = false
        private var value: T? = null

        override fun getValue(thisRef: U, property: KProperty<*>): T {
            if (!isInitialized) {
                value = serializer.deserialize(stored.getValue(thisRef, property))
                isInitialized = true
            }

            @Suppress("UNCHECKED_CAST")
            return value as T
        }

        override fun setValue(thisRef: U, property: KProperty<*>, value: T) {
            this.value = value
            this.isInitialized = true
            stored.setValue(thisRef, property, serializer.serialize(value))
        }

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
