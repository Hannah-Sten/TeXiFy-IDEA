package nl.rubensten.texifyidea.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruben Schellekens
 */
public class TexifyUtilTest {

    @Test
    public void appendExtension() throws Exception {
        String path = "SomePath";
        String extension = "tex";
        String actualResult = TexifyUtil.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionEndsDot() throws Exception {
        String path = "SomePath.";
        String extension = "tex";
        String actualResult = TexifyUtil.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath. + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionAlreadyThere() throws Exception {
        String path = "SomePath.tex";
        String extension = "tex";
        String actualResult = TexifyUtil.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SomePath.tex + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionDoubleExtesion() throws Exception {
        String path = "SomePath.tex.tex";
        String extension = "tex";
        String actualResult = TexifyUtil.appendExtension(path, extension);

        String expectedResult = "SomePath.tex.tex";
        assertEquals("SomePath.tex.tex + tex", expectedResult, actualResult);
    }

    @Test
    public void appendExtensionCrazyCapitals() throws Exception {
        String path = "SoMEPaTH.TEx";
        String extension = "tEX";
        String actualResult = TexifyUtil.appendExtension(path, extension);

        String expectedResult = "SomePath.tex";
        assertEquals("SoMEPaTH.TEx + tEX", true,
                actualResult.equalsIgnoreCase(expectedResult));
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendExtesionNullParameters() throws Exception {
        TexifyUtil.appendExtension(null, null);
    }

}