package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.report.LatexErrorReportSubmitter

class LatestTexifyVersionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        LatexErrorReportSubmitter.Util.resetLatestVersionStateForTest()
        LatexErrorReportSubmitter.Util.latestVersionResponseProvider = {
            """
            <plugin-repository>
              <category name="TeXiFy">
                <idea-plugin>
                  <version>1.2.3</version>
                  <idea-version since-build="243.0"/>
                </idea-plugin>
              </category>
            </plugin-repository>
            """.trimIndent()
        }
    }

    override fun tearDown() {
        try {
            LatexErrorReportSubmitter.Util.resetLatestVersionStateForTest()
        }
        finally {
            super.tearDown()
        }
    }

    fun testVersion() {
        val version = LatexErrorReportSubmitter.Util.getLatestVersion()
        assertEquals("1.2.3", version.version.toString())
    }
}
