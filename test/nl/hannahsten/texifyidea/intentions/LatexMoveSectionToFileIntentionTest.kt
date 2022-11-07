package nl.hannahsten.texifyidea.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexMoveSectionToFileIntentionTest : BasePlatformTestCase() {

    fun testBaseCaseMiddle() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{Simple<caret> Chapter}
                With text
            \end{document}
            """.trimIndent()
        )

        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)

        myFixture.checkResult(
            """
            \begin{document}
                \chapter{Simple<caret> Chapter}
                \input{simple-chapter}
            
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("simple-chapter.tex", "With text", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \section{Simple<caret> Section}
                With More text
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \section{Simple<caret> Section}
                \input{simple-section}
            
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("simple-section.tex", "With More text", true)
    }

    fun testMultipleChaptersInDocumentSingleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \ch<caret>apter{Simple Chapter}
                With text
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \ch<caret>apter{Simple Chapter}
                \input{simple-chapter}
            
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("simple-chapter.tex", "With text", true)
    }

    fun testMultipleChaptersInDocumentMultipleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{First<caret> Chapter}
                First text case
                \chapter{Second Chapter}
                Second text case
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{First<caret> Chapter}
                \input{first-chapter}
            
                \chapter{Second Chapter}
                Second text case
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("first-chapter.tex", "First text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{First Chapter}
                \input{first-chapter}
            
                \chapter{Second Chapter}
                Second text case
                \chapter{Last Chapt<caret>er}
                Last text case
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{First Chapter}
                \input{first-chapter}
            
                \chapter{Second Chapter}
                Second text case
                \chapter{Last Chapt<caret>er}
                \input{last-chapter}
            
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("last-chapter.tex", "Last text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{First Chapter}
                \input{first-chapter}
            
                \chapter{Second <caret>Chapter}
                Second text case
                \chapter{Last Chapter}
                \input{last-chapter}
            
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{First Chapter}
                \input{first-chapter}
            
                \chapter{Second <caret>Chapter}
                \input{second-chapter}
            
                \chapter{Last Chapter}
                \input{last-chapter}
            
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("second-chapter.tex", "Second text case", true)
    }

    fun testMultipleChaptersAndSetionsInDocumentMultipleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{First Chapter}
                First text case
                \section{Secon<caret>d Section}
                Second text case
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{First Chapter}
                First text case
                \section{Secon<caret>d Section}
                \input{second-section}
            
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("second-section.tex", "Second text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{First Chapter<caret>}
                First text case
                \section{Second Section}
                \input{second-section}
            
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{First Chapter<caret>}
                \input{first-chapter}
            
                \chapter{Last Chapter}
                Last text case
            \end{document}
            """.trimIndent()
        )

        myFixture.checkResult("second-section.tex", "Second text case", true)
        myFixture.checkResult(
            "first-chapter.tex",
            """
            First text case
            \section{Second Section}
            \input{second-section}
            """.trimIndent(),
            true
        )
    }

    fun testNoDocument() {
        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{Simple<caret> Chapter}
            With text
            """.trimIndent()
        )

        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)

        myFixture.checkResult(
            """
            \chapter{Simple<caret> Chapter}
            \input{simple-chapter}
            
            
            """.trimIndent()
        )

        myFixture.checkResult("simple-chapter.tex", "With text", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \section{Simple<caret> Section}
            With More text
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \section{Simple<caret> Section}
            \input{simple-section}
            
            
            """.trimIndent()
        )

        myFixture.checkResult("simple-section.tex", "With More text", true)
    }

    fun testMultipleChaptersOutsideDocumentSingleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \ch<caret>apter{Simple Chapter}
            With text
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \ch<caret>apter{Simple Chapter}
            \input{simple-chapter}
            
            
            """.trimIndent()
        )

        myFixture.checkResult("simple-chapter.tex", "With text", true)
    }

    fun testMultipleChaptersOutsideDocumentMultipleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First<caret> Chapter}
            First text case
            \chapter{Second Chapter}
            Second text case
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First<caret> Chapter}
            \input{first-chapter}
        
            \chapter{Second Chapter}
            Second text case
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )

        myFixture.checkResult("first-chapter.tex", "First text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First Chapter}
            \input{first-chapter}
        
            \chapter{Second Chapter}
            Second text case
            \chapter{Last Chapt<caret>er}
            Last text case
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First Chapter}
            \input{first-chapter}
        
            \chapter{Second Chapter}
            Second text case
            \chapter{Last Chapt<caret>er}
            \input{last-chapter}
            
            
            """.trimIndent()
        )

        myFixture.checkResult("last-chapter.tex", "Last text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First Chapter}
            \input{first-chapter}
        
            \chapter{Second <caret>Chapter}
            Second text case
            \chapter{Last Chapter}
            \input{last-chapter}
            
            
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First Chapter}
            \input{first-chapter}
        
            \chapter{Second <caret>Chapter}
            \input{second-chapter}
        
            \chapter{Last Chapter}
            \input{last-chapter}
            
            
            """.trimIndent()
        )

        myFixture.checkResult("second-chapter.tex", "Second text case", true)
    }

    fun testMultipleChaptersAndSetionsOutsideDocumentMultipleExtraction() {
        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First Chapter}
            First text case
            \section{Secon<caret>d Section}
            Second text case
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First Chapter}
            First text case
            \section{Secon<caret>d Section}
            \input{second-section}
        
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )

        myFixture.checkResult("second-section.tex", "Second text case", true)

        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First Chapter<caret>}
            First text case
            \section{Second Section}
            \input{second-section}
        
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First Chapter<caret>}
            \input{first-chapter}
        
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )

        myFixture.checkResult("second-section.tex", "Second text case", true)
        myFixture.checkResult(
            "first-chapter.tex",
            """
            First text case
            \section{Second Section}
            \input{second-section}
            """.trimIndent(),
            true
        )
    }

    fun testMultipleChaptersAndSetionsOutsideDocumentWithEquation() {
        myFixture.configureByText(
            LatexFileType,
            """
            \chapter{First Chapter}
            \begin{equation}
                The Math adds up
            \end{equation}
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )
        myFixture.filterAvailableIntentions("Move section contents to separate file")[0]
            .invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \chapter{First Chapter}
            \input{first-chapter}
        
            \chapter{Last Chapter}
            Last text case
            """.trimIndent()
        )

        myFixture.checkResult(
            "first-chapter.tex",
            """
            \begin{equation}
                The Math adds up
            \end{equation}
            """.trimIndent(),
            true
        )
    }
}