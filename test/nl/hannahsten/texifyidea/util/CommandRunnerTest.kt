package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CommandRunnerTest : BasePlatformTestCase() {

    override fun tearDown() {
        SystemEnvironment.executableLocationCache.clear()
        super.tearDown()
    }

    fun testRunCommandSyncExecutesDirectly() {
        val result = runCommandSync("echo", "texify-test")
        assertEquals(0, result.exitCode)
        assertEquals("texify-test", result.standardOutput)
    }

    fun testRunCommandUsesSyncExecution() {
        val output = runCommand("echo", "texify-sync-output")
        assertEquals("texify-sync-output", output)
    }

    fun testWhichCommandIsCachedInExecutableLocationCache() {
        SystemEnvironment.executableLocationCache["my-custom-test-binary"] = "/usr/bin/my-custom-test-binary"
        val (output, exitCode) = runCommandWithExitCode("which", "my-custom-test-binary")
        assertEquals(0, exitCode)
        assertEquals("/usr/bin/my-custom-test-binary", output)
    }

    fun testReadlinkCommandIsCached() {
        SystemEnvironment.executableLocationCache["readlink:/symlink/path"] = "/target/path"
        val output = runCommand("readlink", "-f", "/symlink/path")
        assertEquals("/target/path", output)
    }

    fun testNegativeExecutableLookupIsCached() {
        val fakeBinary = "nonexistent-fake-latex-binary-12345"
        val result1 = runCommandWithExitCode("which", fakeBinary)
        assertFalse(result1.second == 0)

        // Cache should now contain empty string for negative lookup
        assertEquals("", SystemEnvironment.executableLocationCache[fakeBinary])

        // Subsequent lookup should hit cache and return non-zero exit code immediately
        val result2 = runCommandWithExitCode("which", fakeBinary)
        assertEquals(1, result2.second)
        assertNull(result2.first)
    }
}
