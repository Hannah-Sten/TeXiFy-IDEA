package nl.hannahsten.texifyidea.util

import org.junit.Assert
import org.junit.Test

/**
 * @author Hannah Schellekens
 */
class TexifyUtilTest {

    @Test
    fun appendExtension() {
        val path = "SomePath"
        val extension = "tex"
        val actualResult = path.appendExtension(extension)
        val expectedResult = "SomePath.tex"
        Assert.assertEquals("SomePath + tex", expectedResult, actualResult)
    }

    @Test
    fun appendExtensionEndsDot() {
        val path = "SomePath."
        val extension = "tex"
        val actualResult = path.appendExtension(extension)
        val expectedResult = "SomePath.tex"
        Assert.assertEquals("SomePath. + tex", expectedResult, actualResult)
    }

    @Test
    fun appendExtensionAlreadyThere() {
        val path = "SomePath.tex"
        val extension = "tex"
        val actualResult = path.appendExtension(extension)
        val expectedResult = "SomePath.tex"
        Assert.assertEquals("SomePath.tex + tex", expectedResult, actualResult)
    }

    @Test
    fun appendExtensionDoubleExtesion() {
        val path = "SomePath.tex.tex"
        val extension = "tex"
        val actualResult = path.appendExtension(extension)
        val expectedResult = "SomePath.tex.tex"
        Assert.assertEquals("SomePath.tex.tex + tex", expectedResult, actualResult)
    }
}