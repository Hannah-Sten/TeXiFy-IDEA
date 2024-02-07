package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.util.parser.firstChildOfType
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import nl.hannahsten.texifyidea.util.parser.getTagContent
import org.intellij.lang.annotations.Language

class BibtexEntryImplUtilTest : BasePlatformTestCase() {

    private val url = "https://github.com/hannah-sten/TeXiFy-IDEA"

    @Language("Bibtex")
    private val entryText =
        """@article{texify,
        author = {Hannah-Sten},
        title = {TeXiFy IDEA},
        journal = {GitHub},
        year = {2020},
        url = {$url},
        biburl = {$url}
    }"""

    private val entryElement by lazy {
        PsiDocumentManager.getInstance(myFixture.project)
            .getPsiFile(myFixture.editor.document)!!
            .firstChildOfType(BibtexEntry::class)!!
    }

    override fun setUp() {
        super.setUp()
        myFixture.configureByText(BibtexFileType, entryText)
    }

    fun testEntryGetReferences() {
        listOf(WebReference(entryElement, url)).map { it.url }.forEach {
            UsefulTestCase.assertContainsElements(entryElement.references.map { reference -> (reference as WebReference).url }, it)
        }
    }

    fun testGetTagContent() {
        TestCase.assertEquals("TeXiFy IDEA", entryElement.getTagContent("title"))
    }

    fun `test get id of 'empty' element`() {
        myFixture.configureByText(
            BibtexFileType,
            """
                @misc{identifier,
                }
            """.trimIndent()
        )
        TestCase.assertEquals("identifier", entryElement.getIdentifier())
    }

    fun `test get id of @string element`() {
        myFixture.configureByText(BibtexFileType, "@string{a = b}")
        TestCase.assertEquals("a", entryElement.getIdentifier())
    }
}