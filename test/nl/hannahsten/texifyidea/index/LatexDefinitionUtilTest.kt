package nl.hannahsten.texifyidea.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.LatexPsiHelper

/**
 * Tests for [LatexDefinitionUtil], specifically the argument context inference logic.
 */
class LatexDefinitionUtilTest : BasePlatformTestCase() {

    private val emptyLookup = object : LatexSemanticsLookup {
        override fun lookup(name: String): LSemanticEntity? = null
        override fun allEntitiesSeq(): Sequence<LSemanticEntity> = emptySequence()
        override fun findByRelatedContext(context: LatexContext): List<LSemanticEntity> = emptyList()
    }

    fun testPlaceholderInNormalTextIsDetected() {
        // Simple code where #1 appears in normal text
        val codeWithNormalPlaceholder = """Hello #1 world"""

        val codeElement = LatexPsiHelper.createFromText(codeWithNormalPlaceholder, project)

        val result = LatexDefinitionUtil.guessArgumentContextIntro(codeElement, 1, emptyLookup)

        assertEquals("Should have one argument context", 1, result.size)

        // The argument should be detected (not fall back to Comment)
        val argIntro = result[0]
        assertFalse(
            "Detected placeholder should NOT result in Comment context",
            isCommentContext(argIntro)
        )
    }

    /**
     * Tests that parameter placeholders (#1, #2, etc.) inside RAW_TEXT_TOKEN elements
     * are correctly detected for context inference.
     *
     * This is a regression test for the fix that added RAW_TEXT_TOKEN scanning.
     * Without this fix, placeholders in verbatim-like contexts would not be detected,
     * causing arguments to fall back to Comment context (greying out code).
     */
    fun testPlaceholderInRawTextTokenIsDetected() {
        // A command definition where #1 appears inside \lstinline (verbatim-like context)
        // The lexer will produce RAW_TEXT_TOKEN for content inside lstinline
        val codeWithRawTextPlaceholder = """\lstinline{#1}"""

        val codeElement = LatexPsiHelper.createFromText(codeWithRawTextPlaceholder, project)

        val result = LatexDefinitionUtil.guessArgumentContextIntro(codeElement, 1, emptyLookup)

        assertEquals("Should have one argument context", 1, result.size)

        // The key assertion: the argument should NOT be assigned to Comment context
        // Before the fix, undetected placeholders would fall back to Comment
        val argIntro = result[0]
        assertFalse(
            "Argument should NOT be assigned to Comment context (would grey out code)",
            isCommentContext(argIntro)
        )
    }

    /**
     * Tests that the fallback for arguments without detected placeholders is inherit(),
     * not Comment. This prevents large code blocks from being greyed out when
     * context inference fails.
     */
    fun testFallbackIsInheritNotComment() {
        // Code where there's no placeholder at all
        val codeWithoutPlaceholder = """some text without any placeholders"""

        val codeElement = LatexPsiHelper.createFromText(codeWithoutPlaceholder, project)

        val result = LatexDefinitionUtil.guessArgumentContextIntro(codeElement, 1, emptyLookup)

        assertEquals("Should have one argument context", 1, result.size)

        // The argument should fall back to inherit(), not Comment
        val argIntro = result[0]
        assertFalse(
            "Fallback should NOT be Comment context",
            isCommentContext(argIntro)
        )

        // Verify it's actually inherit
        assertTrue(
            "Fallback should be inherit()",
            isInheritContext(argIntro)
        )
    }

    private fun isCommentContext(intro: LatexContextIntro): Boolean {
        // Check if the intro assigns to Comment context
        if (intro is LatexContextIntro.Assign) {
            return LatexContexts.Comment in intro.contexts
        }
        return intro.toString().contains("Comment")
    }

    private fun isInheritContext(intro: LatexContextIntro): Boolean = intro == LatexContextIntro.inherit() || intro is LatexContextIntro.Inherit
}
