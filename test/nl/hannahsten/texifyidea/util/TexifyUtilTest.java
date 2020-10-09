package nl.hannahsten.texifyidea.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hannah Schellekens
 */
public class TexifyUtilTest {

    @Test
    public void appendExtension() throws Exception {
        String path = "SomePath";
        String extension = "tex";
        String actualResult = StringsKt.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionEndsDot() throws Exception {
        String path = "SomePath.";
        String extension = "tex";
        String actualResult = StringsKt.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath. + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionAlreadyThere() throws Exception {
        String path = "SomePath.tex";
        String extension = "tex";
        String actualResult = StringsKt.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath.tex + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionDoubleExtesion() throws Exception {
        String path = "SomePath.tex.tex";
        String extension = "tex";
        String actualResult = StringsKt.appendExtension(path, extension);

        String expectedResult = "SomePath.tex.tex";
        assertEquals("SomePath.tex.tex + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionCrazyCapitals() throws Exception {
        String path = "SoMEPaTH.TEx";
        String extension = "tEX";
        String actualResult = StringsKt.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertTrue("SoMEPaTH.TEx + tEX", actualResult.equalsIgnoreCase(expectedResult));
    }
}