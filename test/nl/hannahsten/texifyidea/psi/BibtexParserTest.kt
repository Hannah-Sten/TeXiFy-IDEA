package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.BibtexFileType

class BibtexParserTest : BasePlatformTestCase() {

    fun testUrl() {
        myFixture.configureByText(
            BibtexFileType,
            """
            @book{Friedman2015a,
                title = {{Chronomorphic Programs : Using Runtime Diversity to Prevent Code Reuse Attacks}},
                year = {2015},
                author = {Friedman, Scott E and Musliner, David J and Keller, Peter K},
                number = {c},
                pages = {76--82},
                url = {https://www.sift.net/sites/default/files/publications/icds_2015_4_40_10108 %282%29.pdf},
                isbn = {9781612083810},
                keywords = {- cyber defense, are repeatedly changing or, attack tool cannot accumulate, brop, enough information about the, moving, program, s memory layout to, self-modifying code, so that even a, software diversity, succeed}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}