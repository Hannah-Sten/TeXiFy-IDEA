package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.components.fields.ExtendableTextField
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import java.nio.file.Path
import java.util.function.Predicate

class LatexBasicFragmentsTest : BasePlatformTestCase() {

    fun testOutputDirectoryFragmentUsesMacrosDialogExtension() {
        val fragment = LatexBasicFragments.createOutputDirectoryFragment("Common settings", project)
        val field = fragment.component().component as TextFieldWithBrowseButton
        val textField = field.textField as ExtendableTextField

        assertTrue(textField.extensions.isNotEmpty())
        assertFalse(textField.extensions.any { it.tooltip == "Insert path macro" })
    }

    fun testAuxiliaryDirectoryFragmentUsesOutputDirectoryEmptyStateHint() {
        val fragment = LatexBasicFragments.createAuxiliaryDirectoryFragment("Common settings", project)
        val field = fragment.component().component as TextFieldWithBrowseButton
        val textField = field.textField as ExtendableTextField

        assertEquals("Leave empty to use Output directory", textField.emptyText.text)
    }

    fun testAuxiliaryDirectoryFragmentIsInitiallyHiddenWithoutCustomValue() {
        val fragment = LatexBasicFragments.createAuxiliaryDirectoryFragment("Common settings", project)
        val settings = createSettings()

        assertFalse(initialVisibility(fragment, settings))
    }

    fun testAuxiliaryDirectoryFragmentIsInitiallyVisibleWhenCustomValueConfigured() {
        val fragment = LatexBasicFragments.createAuxiliaryDirectoryFragment("Common settings", project)
        val settings = createSettings(auxPath = Path.of("{projectDir}", "aux"))

        assertTrue(initialVisibility(fragment, settings))
    }

    private fun createSettings(auxPath: Path? = null): RunnerAndConfigurationSettingsImpl {
        val factory = LatexRunConfigurationProducer().configurationFactory
        val runConfig = LatexRunConfiguration(project, factory, "run config").apply {
            auxilPath = auxPath
        }
        return RunManagerImpl.getInstanceImpl(project)
            .createConfiguration(runConfig, factory) as RunnerAndConfigurationSettingsImpl
    }

    @Suppress("UNCHECKED_CAST")
    private fun initialVisibility(
        fragment: Any,
        settings: RunnerAndConfigurationSettingsImpl
    ): Boolean {
        val field = fragment.javaClass.superclass.getDeclaredField("myInitialVisibility").apply {
            isAccessible = true
        }
        val predicate = field.get(fragment) as Predicate<RunnerAndConfigurationSettingsImpl>
        return predicate.test(settings)
    }
}
