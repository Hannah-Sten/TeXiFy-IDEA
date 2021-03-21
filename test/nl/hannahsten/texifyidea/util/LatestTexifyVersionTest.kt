package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.LatexErrorReportSubmitter

class LatestTexifyVersionTest : BasePlatformTestCase() {

    fun testVersion() {
        val version = LatexErrorReportSubmitter.getLatestVersion()
        assertTrue(version.isNotBlank())
    }
}