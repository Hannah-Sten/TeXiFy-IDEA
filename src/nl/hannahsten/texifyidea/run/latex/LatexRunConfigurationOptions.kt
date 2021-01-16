package nl.hannahsten.texifyidea.run.latex

import com.intellij.configurationStore.Property
import com.intellij.execution.configurations.LocatableRunConfigurationOptions
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.XMap

class LatexRunConfigurationOptions : LocatableRunConfigurationOptions() {

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
