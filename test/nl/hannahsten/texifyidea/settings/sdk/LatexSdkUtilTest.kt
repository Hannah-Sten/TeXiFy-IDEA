package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests for [LatexSdkUtil] functionality.
 * Tests both project-level and module-level SDK support.
 */
class LatexSdkUtilTest : BasePlatformTestCase() {

    private val testSdks = mutableListOf<Sdk>()

    override fun tearDown() {
        // Clean up any test SDKs
        if (testSdks.isNotEmpty()) {
            ApplicationManager.getApplication().invokeAndWait {
                runWriteAction {
                    testSdks.forEach { sdk ->
                        ProjectJdkTable.getInstance().removeJdk(sdk)
                    }
                }
            }
            testSdks.clear()
        }
        super.tearDown()
    }

    /**
     * Helper to create and register a test LaTeX SDK.
     */
    private fun createTestLatexSdk(name: String): Sdk {
        val sdkType = TexliveSdk()
        val sdk = ProjectJdkImpl(name, sdkType)

        // Use SdkModificator to set the home path (required by newer IntelliJ versions)
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

    /**
     * Helper to set the module SDK.
     */
    private fun setModuleSdk(sdk: Sdk?) {
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
    }

    /**
     * Helper to set the project SDK.
     */
    private fun setProjectSdk(sdk: Sdk?) {
        runWriteAction {
            ProjectRootManager.getInstance(project).projectSdk = sdk
        }
    }

    fun testGetLatexProjectSdkReturnsNullWhenNoSdkConfigured() {
        val result = LatexSdkUtil.getLatexProjectSdk(project)
        assertNull("Expected null when no LaTeX SDK is configured", result)
    }

    fun testGetLatexProjectSdkReturnsNullForNonLatexSdk() {
        val result = LatexSdkUtil.getLatexProjectSdk(project)
        assertNull("Expected null for non-LaTeX SDK", result)
    }

    fun testGetLatexSdkForFileFallsBackToProjectSdk() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")

        // Without any SDK configured, should return null
        val result = LatexSdkUtil.getLatexSdkForFile(file.virtualFile, project)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testGetLatexSdkForPsiFile() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")

        // Without any SDK configured, should return null
        val result = LatexSdkUtil.getLatexSdkForFile(file)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testGetAllLatexSdksReturnsEmptyListWhenNoSdksConfigured() {
        val result = LatexSdkUtil.getAllLatexSdks()
        val latexSdks = result.filter { it.sdkType is LatexSdk }
        assertTrue("Expected no LaTeX SDKs initially", latexSdks.isEmpty())
    }

    fun testGetLatexModuleSdkReturnsNullWhenNoSdk() {
        val module = myFixture.module
        val result = LatexSdkUtil.getLatexModuleSdk(module)
        assertNull("Expected null when module has no SDK", result)
    }

    fun testGetLatexProjectSdkTypeReturnsNullWhenNoSdk() {
        val result = LatexSdkUtil.getLatexProjectSdkType(project)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testGetLatexSdkTypeForFileReturnsNullWhenNoSdk() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexSdkTypeForFile(file.virtualFile, project)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testGetLatexDistributionTypeReturnsNullWhenNoSdk() {
        val result = LatexSdkUtil.getLatexDistributionType(project)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testGetLatexDistributionTypeForFileReturnsNullWhenNoSdk() {
        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexDistributionTypeForFile(file.virtualFile, project)
        assertNull("Expected null when no SDK is configured", result)
    }

    fun testParsePdflatexOutputTexLive() {
        val output = """
            pdfTeX 3.14159265-2.6-1.40.20 (TeX Live 2019)
            kpathsea version 6.3.1
            Copyright 2019 Han The Thanh (pdfTeX) et al.
        """.trimIndent()

        assertEquals("TeX Live 2019", LatexSdkUtil.parsePdflatexOutput(output))
    }

    fun testParsePdflatexOutputMiktex() {
        val output = """
            MiKTeX-pdfTeX 2.9.6870 (1.40.19) (MiKTeX 2.9.6880 64-bit)
            Copyright (C) 1982 D. E. Knuth, (C) 1996-2018 Han The Thanh
        """.trimIndent()

        assertEquals("MiKTeX 2.9.6880 64-bit", LatexSdkUtil.parsePdflatexOutput(output))
    }

    fun testParsePdflatexOutputEmptyInput() {
        assertEquals("", LatexSdkUtil.parsePdflatexOutput(""))
    }

    fun testParsePdflatexOutputNoParentheses() {
        val output = "pdfTeX version 1.0"
        assertEquals("", LatexSdkUtil.parsePdflatexOutput(output))
    }

    fun testGetLatexModuleSdkReturnsModuleSdk() {
        val sdk = createTestLatexSdk("Test TeX Live SDK")
        setModuleSdk(sdk)

        val result = LatexSdkUtil.getLatexModuleSdk(module)

        assertNotNull("Expected module SDK to be returned", result)
        assertEquals("Expected the correct SDK", sdk, result)
    }

    fun testGetLatexSdkForFileReturnsModuleSdkOverProjectSdk() {
        val moduleSdk = createTestLatexSdk("Module TeX Live SDK")
        val projectSdk = createTestLatexSdk("Project TeX Live SDK")

        setModuleSdk(moduleSdk)
        setProjectSdk(projectSdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexSdkForFile(file.virtualFile, project)

        assertNotNull("Expected SDK to be returned", result)
        assertEquals("Expected module SDK to take precedence over project SDK", moduleSdk, result)
    }

    fun testGetLatexSdkForFileFallsBackToProjectSdkWhenNoModuleSdk() {
        val projectSdk = createTestLatexSdk("Project TeX Live SDK")
        setProjectSdk(projectSdk)
        // Don't set module SDK

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexSdkForFile(file.virtualFile, project)

        assertNotNull("Expected project SDK to be returned as fallback", result)
        assertEquals("Expected project SDK when no module SDK is set", projectSdk, result)
    }

    fun testGetLatexProjectSdkReturnsSdk() {
        val sdk = createTestLatexSdk("Test TeX Live SDK")
        setProjectSdk(sdk)

        val result = LatexSdkUtil.getLatexProjectSdk(project)

        assertNotNull("Expected project SDK to be returned", result)
        assertEquals("Expected the correct SDK", sdk, result)
    }

    fun testGetAllLatexSdksReturnsRegisteredSdks() {
        val sdk1 = createTestLatexSdk("TeX Live SDK 1")
        val sdk2 = createTestLatexSdk("TeX Live SDK 2")

        val result = LatexSdkUtil.getAllLatexSdks()

        assertTrue("Expected SDK 1 to be in the list", result.contains(sdk1))
        assertTrue("Expected SDK 2 to be in the list", result.contains(sdk2))
    }

    fun testGetLatexSdkTypeForFileReturnsCorrectType() {
        val sdk = createTestLatexSdk("Test TeX Live SDK")
        setModuleSdk(sdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexSdkTypeForFile(file.virtualFile, project)

        assertNotNull("Expected SDK type to be returned", result)
        assertTrue("Expected TexliveSdk type", result is TexliveSdk)
    }

    fun testGetLatexDistributionTypeForFileReturnsCorrectType() {
        val sdk = createTestLatexSdk("Test TeX Live SDK")
        setModuleSdk(sdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexDistributionTypeForFile(file.virtualFile, project)

        assertNotNull("Expected distribution type to be returned", result)
        assertEquals(
            "Expected TEXLIVE distribution type",
            nl.hannahsten.texifyidea.run.latex.LatexDistributionType.TEXLIVE, result
        )
    }

    fun testGetLatexSdkForPsiFileReturnsModuleSdk() {
        val sdk = createTestLatexSdk("Test TeX Live SDK")
        setModuleSdk(sdk)

        val file = myFixture.addFileToProject("test.tex", "\\documentclass{article}")
        val result = LatexSdkUtil.getLatexSdkForFile(file)

        assertNotNull("Expected SDK to be returned", result)
        assertEquals("Expected the module SDK", sdk, result)
    }
}
