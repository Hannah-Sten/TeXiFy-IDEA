package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.ui.LatexDistributionSelection
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import org.jdom.Element

/**
 * Tests for [LatexRunConfiguration] distribution type selection functionality.
 * Tests the interaction between run configuration, module SDK, and project SDK.
 *
 * @see LatexRunConfiguration.latexDistribution for design documentation
 */
class LatexRunConfigurationSdkTest : BasePlatformTestCase() {

    private val testSdks = mutableListOf<Sdk>()

    override fun tearDown() {
        // Clean up all LaTeX SDKs created during tests
        ApplicationManager.getApplication().invokeAndWait {
            runWriteAction {
                LatexSdkUtil.getAllLatexSdks().forEach { sdk ->
                    ProjectJdkTable.getInstance().removeJdk(sdk)
                }
            }
        }
        testSdks.clear()
        super.tearDown()
    }

    private fun createTestLatexSdk(name: String): Sdk {
        val sdkType = TexliveSdk()
        val sdk = ProjectJdkImpl(name, sdkType)

        val modificator = sdk.sdkModificator
        modificator.homePath = "/fake/texlive/2024"
        ApplicationManager.getApplication().invokeAndWait {
            runWriteAction {
                modificator.commitChanges()
            }
        }

        ApplicationManager.getApplication().invokeAndWait {
            runWriteAction {
                ProjectJdkTable.getInstance().addJdk(sdk)
            }
        }
        testSdks.add(sdk)
        return sdk
    }

    private fun setModuleSdk(sdk: Sdk?) {
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
    }

    private fun setProjectSdk(sdk: Sdk?) {
        runWriteAction {
            ProjectRootManager.getInstance(project).projectSdk = sdk
        }
    }

    private fun createRunConfiguration(): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "Test SDK Run Config"
    )

    // Tests for getLatexDistributionType resolution

    fun testSdkFromMainFileResolvesToDistributionTypeFromModuleSdk() {
        val moduleSdk = createTestLatexSdk("Module TeX Live SDK")
        val projectSdk = createTestLatexSdk("Project TeX Live SDK")
        setModuleSdk(moduleSdk)
        setProjectSdk(projectSdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val runConfig = createRunConfiguration()
        runConfig.mainFilePath = file.virtualFile.name
        runConfig.latexDistribution = LatexDistributionType.MODULE_SDK

        assertEquals(
            "Expected TEXLIVE distribution type from module SDK",
            LatexDistributionType.TEXLIVE,
            runConfig.getLatexDistributionType()
        )
    }

    fun testSdkFromMainFileFallsBackToProjectSdk() {
        val projectSdk = createTestLatexSdk("Project TeX Live SDK")
        setProjectSdk(projectSdk)
        // Don't set module SDK

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val runConfig = createRunConfiguration()
        runConfig.mainFilePath = file.virtualFile.name
        runConfig.latexDistribution = LatexDistributionType.MODULE_SDK

        assertEquals(
            "Expected TEXLIVE distribution type from project SDK fallback",
            LatexDistributionType.TEXLIVE,
            runConfig.getLatexDistributionType()
        )
    }

    fun testProjectSdkResolvesToDistributionTypeFromProjectSdk() {
        val moduleSdk = createTestLatexSdk("Module TeX Live SDK")
        val projectSdk = createTestLatexSdk("Project TeX Live SDK")
        setModuleSdk(moduleSdk)
        setProjectSdk(projectSdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val runConfig = createRunConfiguration()
        runConfig.mainFilePath = file.virtualFile.name
        runConfig.latexDistribution = LatexDistributionType.PROJECT_SDK

        assertEquals(
            "Expected TEXLIVE distribution type from project SDK",
            LatexDistributionType.TEXLIVE,
            runConfig.getLatexDistributionType()
        )
    }

    fun testGetLatexDistributionTypeFallsBackToTexliveWhenNoSdk() {
        val runConfig = createRunConfiguration()
        runConfig.latexDistribution = LatexDistributionType.MODULE_SDK

        assertEquals(
            "Expected TEXLIVE fallback when no SDK found",
            LatexDistributionType.TEXLIVE,
            runConfig.getLatexDistributionType()
        )
    }

    fun testGetLatexDistributionTypeReturnsConcreteTypeDirectly() {
        val runConfig = createRunConfiguration()
        runConfig.latexDistribution = LatexDistributionType.MIKTEX

        assertEquals(
            "Expected MIKTEX to be returned directly",
            LatexDistributionType.MIKTEX,
            runConfig.getLatexDistributionType()
        )
    }

    // Tests for persistence (read/write external)

    private fun createTexifyParentElement(): Pair<Element, Element> {
        val element = Element("configuration")
        val texifyParent = Element("texify")
        element.addContent(texifyParent)
        // Add required elements to avoid NPE during readExternal
        texifyParent.addContent(Element("compiler").also { it.text = "PDFLATEX" })
        texifyParent.addContent(Element("main-file").also { it.text = "" })
        return Pair(element, texifyParent)
    }

    fun testPersistSdkFromMainFile() {
        val runConfig = createRunConfiguration()
        runConfig.latexDistribution = LatexDistributionType.MODULE_SDK

        val element = Element("configuration")
        runConfig.writeExternal(element)

        val newRunConfig = createRunConfiguration()
        newRunConfig.readExternal(element)

        assertEquals(
            "Expected MODULE_SDK to be persisted",
            LatexDistributionType.MODULE_SDK,
            newRunConfig.latexDistribution
        )
    }

    fun testPersistProjectSdk() {
        val runConfig = createRunConfiguration()
        runConfig.latexDistribution = LatexDistributionType.PROJECT_SDK

        val element = Element("configuration")
        runConfig.writeExternal(element)

        val newRunConfig = createRunConfiguration()
        newRunConfig.readExternal(element)

        assertEquals(
            "Expected PROJECT_SDK to be persisted",
            LatexDistributionType.PROJECT_SDK,
            newRunConfig.latexDistribution
        )
    }

    fun testPersistConcreteDistributionType() {
        val runConfig = createRunConfiguration()
        runConfig.latexDistribution = LatexDistributionType.MIKTEX

        val element = Element("configuration")
        runConfig.writeExternal(element)

        val newRunConfig = createRunConfiguration()
        newRunConfig.readExternal(element)

        assertEquals(
            "Expected MIKTEX to be persisted",
            LatexDistributionType.MIKTEX,
            newRunConfig.latexDistribution
        )
    }

    // Tests for backwards compatibility with old run configuration format

    fun testBackwardsCompatibilityOldProjectSdk() {
        val (element, texifyParent) = createTexifyParentElement()
        texifyParent.addContent(Element("latex-distribution").also { it.text = "PROJECT_SDK" })

        val runConfig = createRunConfiguration()
        runConfig.readExternal(element)

        assertEquals(
            "Expected old PROJECT_SDK to be read correctly",
            LatexDistributionType.PROJECT_SDK,
            runConfig.latexDistribution
        )
    }

    fun testBackwardsCompatibilityOldTexlive() {
        val (element, texifyParent) = createTexifyParentElement()
        texifyParent.addContent(Element("latex-distribution").also { it.text = "TEXLIVE" })

        val runConfig = createRunConfiguration()
        runConfig.readExternal(element)

        assertEquals(
            "Expected old TEXLIVE to be read correctly",
            LatexDistributionType.TEXLIVE,
            runConfig.latexDistribution
        )
    }

    fun testBackwardsCompatibilityOldMiktex() {
        val (element, texifyParent) = createTexifyParentElement()
        texifyParent.addContent(Element("latex-distribution").also { it.text = "MIKTEX" })

        val runConfig = createRunConfiguration()
        runConfig.readExternal(element)

        assertEquals(
            "Expected old MIKTEX to be read correctly",
            LatexDistributionType.MIKTEX,
            runConfig.latexDistribution
        )
    }

    fun testDefaultsToTexliveWhenNoDistributionSetting() {
        val (element, _) = createTexifyParentElement()
        // No latex-distribution element - simulates old run config format

        val runConfig = createRunConfiguration()
        runConfig.readExternal(element)

        // Old run configs without latex-distribution element default to TEXLIVE for backwards compatibility
        assertEquals(
            "Expected default to be TEXLIVE for backwards compatibility",
            LatexDistributionType.TEXLIVE,
            runConfig.latexDistribution
        )
    }

    // Tests for LatexDistributionSelection UI helper

    fun testDistributionSelectionFromDistributionType() {
        val selection = LatexDistributionSelection.fromDistributionType(LatexDistributionType.MODULE_SDK)

        assertEquals(
            "Expected distribution type to be preserved",
            LatexDistributionType.MODULE_SDK,
            selection.distributionType
        )
    }

    fun testDistributionSelectionSecondaryLabelForModuleSdk() {
        val selection = LatexDistributionSelection(LatexDistributionType.MODULE_SDK)

        assertEquals(
            "Expected secondary label for MODULE_SDK",
            "Module SDK",
            selection.secondaryLabel
        )
    }

    fun testDistributionSelectionSecondaryLabelForProjectSdk() {
        val selection = LatexDistributionSelection(LatexDistributionType.PROJECT_SDK)

        assertEquals(
            "Expected secondary label for PROJECT_SDK",
            "Project SDK",
            selection.secondaryLabel
        )
    }

    fun testDistributionSelectionNoSecondaryLabelForConcreteTypes() {
        val selection = LatexDistributionSelection(LatexDistributionType.TEXLIVE)

        assertNull(
            "Expected no secondary label for concrete distribution types",
            selection.secondaryLabel
        )
    }

    fun testGetAvailableSelectionsIncludesSdkOptionsWhenSdksExist() {
        val sdk = createTestLatexSdk("TeX Live 2024")
        setProjectSdk(sdk)

        val selections = LatexDistributionSelection.getAvailableSelections(project)

        assertTrue(
            "Expected MODULE_SDK option when SDKs exist",
            selections.any { it.distributionType == LatexDistributionType.MODULE_SDK }
        )
        assertTrue(
            "Expected PROJECT_SDK option when project SDK is configured",
            selections.any { it.distributionType == LatexDistributionType.PROJECT_SDK }
        )
    }

    fun testGetAvailableSelectionsExcludesSdkOptionsWhenNoSdks() {
        // Don't create any SDKs

        val selections = LatexDistributionSelection.getAvailableSelections(project)

        assertFalse(
            "Expected no MODULE_SDK option when no SDKs exist",
            selections.any { it.distributionType == LatexDistributionType.MODULE_SDK }
        )
        assertFalse(
            "Expected no PROJECT_SDK option when no SDKs exist",
            selections.any { it.distributionType == LatexDistributionType.PROJECT_SDK }
        )
    }

    // Test for resolved SDK name display

    fun testGetDisplayNameForSdkFromMainFile() {
        val sdk = createTestLatexSdk("My TeX Live SDK")
        setProjectSdk(sdk)

        val selection = LatexDistributionSelection(LatexDistributionType.MODULE_SDK)
        val displayName = selection.getDisplayName(null, project)

        assertEquals(
            "Expected resolved SDK name",
            "My TeX Live SDK",
            displayName
        )
    }

    fun testGetDisplayNameForProjectSdk() {
        val sdk = createTestLatexSdk("Project TeX Live SDK")
        setProjectSdk(sdk)

        val selection = LatexDistributionSelection(LatexDistributionType.PROJECT_SDK)
        val displayName = selection.getDisplayName(null, project)

        assertEquals(
            "Expected resolved SDK name",
            "Project TeX Live SDK",
            displayName
        )
    }

    fun testGetDisplayNameForConcreteDistribution() {
        val selection = LatexDistributionSelection(LatexDistributionType.TEXLIVE)
        val displayName = selection.getDisplayName(null, project)

        assertEquals(
            "Expected distribution display name",
            LatexDistributionType.TEXLIVE.displayName,
            displayName
        )
    }

    fun testGetDisplayNameShowsNoSdkConfiguredWhenMissing() {
        // Don't set any SDK

        val selection = LatexDistributionSelection(LatexDistributionType.PROJECT_SDK)
        val displayName = selection.getDisplayName(null, project)

        assertEquals(
            "Expected no SDK configured message",
            "<no SDK configured>",
            displayName
        )
    }
}
