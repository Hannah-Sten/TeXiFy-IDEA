package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.report.LatexErrorReportSubmitter

class LatestTexifyVersionTest : BasePlatformTestCase() {

    fun testVersion() {
        val version = LatexErrorReportSubmitter.Util.getLatestVersion()
        assertTrue(version.version.toString().isNotBlank())
    }
}