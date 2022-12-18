// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package nl.hannahsten.texifyidea.refactoring

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiFileFactory
import com.intellij.refactoring.BaseRefactoringProcessor.ConflictsInTestsException
import com.intellij.refactoring.MockInlineMethodOptions
import com.intellij.refactoring.inline.InlineOptions
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import junit.framework.TestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.refactoring.inlinecommand.LatexInlineCommandHandler.Companion.getReference
import nl.hannahsten.texifyidea.refactoring.inlinefile.LatexInlineFileHandler.Companion.canInlineLatexElement
import nl.hannahsten.texifyidea.refactoring.inlinefile.LatexInlineFileHandler.Companion.resolveInlineFile
import nl.hannahsten.texifyidea.refactoring.inlinefile.LatexInlineFileProcessor
import nl.hannahsten.texifyidea.util.runWriteAction
import org.jetbrains.annotations.NonNls
import java.io.File

class InlineFileTest : LightPlatformCodeInsightTestCase() {

    init {
        myTestDataPath = "test/resources/refactoring/inline/"
    }

    private val inlineFile = "filetoinline.tex"

    fun testFileWithReference() {
        doTest()
    }

    fun testFileWithNoReference() {
        doTestAssertBadReturn()
    }

    fun testFileWithNoCommand() {
        doTestAssertBadReturn()
    }

    fun testFileWithIncompleteInput() {
        doTestAssertBadReturn(2)
    }

    fun testFileWithMultReference() {
        doTest(4)
    }

    fun testFileWithMultReferenceSingle() {
        doTestInlineThisOnly(4)
    }

    fun testFileWithReferenceOnCommand() {
        doTest()
    }

    private fun doTestInlineThisOnly(numTests: Int? = null) {
        if (numTests == null) {
            @NonNls val fileName = configure()
            performAction(
                object : MockInlineMethodOptions() {
                    override fun isInlineThisOnly(): Boolean {
                        return true
                    }
                },
                false
            )
            checkResultByFile("$fileName.after")
        }
        else {
            for (testIndex in 1..numTests) {
                @NonNls val fileName = configure(testIndex)
                performAction(
                    object : MockInlineMethodOptions() {
                        override fun isInlineThisOnly(): Boolean {
                            return true
                        }
                    },
                    false
                )
                checkResultByFile("$fileName.after")
            }
        }
    }

    private fun doTest(numTests: Int? = null) {
        if (numTests == null) {
            @NonNls val fileName = configure()
            performAction()
            checkResultByFile(null, "$fileName.after", true)
        }
        else {
            for (testIndex in 1..numTests) {
                @NonNls val fileName = configure(testIndex)
                performAction()
                checkResultByFile(null, "$fileName.after", true)
            }
        }
    }

    private fun doTestAssertBadReturn(numTests: Int? = null) {
        if (numTests == null) {
            @NonNls val fileName = configure()
            ConflictsInTestsException.withIgnoredConflicts<RuntimeException> {
                performAction(
                    MockInlineMethodOptions(),
                    true
                )
            }
            checkResultByFile("$fileName.after")
        }
        else {
            for (testIndex in 1..numTests) {
                @NonNls val fileName = configure(testIndex)
                ConflictsInTestsException.withIgnoredConflicts<RuntimeException> {
                    performAction(
                        MockInlineMethodOptions(),
                        true
                    )
                }
                checkResultByFile("$fileName.after")
            }
        }
    }

    private fun configure(testIndex: Int? = null): String {
        @NonNls val fileName = getTestName(false) + (testIndex ?: "") + ".tex"
        configureByFile(fileName)
        TestCase.assertTrue(file.parent != null)
        if (file.parent?.children?.any { it.containingFile.name == inlineFile } == false) {
            val ioFile = File(testDataPath + inlineFile)
            checkCaseSensitiveFS(testDataPath + inlineFile, ioFile)
            val fileText = FileUtilRt.loadFile(ioFile, CharsetToolkit.UTF8, true)
            runWriteAction {
                file.parent?.add(
                    PsiFileFactory.getInstance(project).createFileFromText(
                        inlineFile,
                        LatexFileType,
                        fileText
                    )
                )
            }
        }
        return fileName
    }

    private fun performAction() {
        performAction(MockInlineMethodOptions(), false)
    }

    private fun performAction(options: InlineOptions, assertBadReturn: Boolean) {
        val element = TargetElementUtil
            .findTargetElement(
                editor,
                TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED
            )

        if (element == null) {
            assertTrue("Could not resolve element to inline", assertBadReturn)
            return
        }

        val inlineFile = resolveInlineFile(element)

        val canInlineElement = canInlineLatexElement(element)

        if (assertBadReturn) {
            assertTrue("Bad returns not found", inlineFile == null || !canInlineElement)
        }
        else {
            assertFalse("Bad returns found", inlineFile == null || !canInlineElement)
        }

        if (inlineFile == null || !canInlineElement)
            return

        val ref = getReference(editor)

        val processor = LatexInlineFileProcessor(
            project, inlineFile, ref, options.isInlineThisOnly, !options.isKeepTheDeclaration
        )
        processor.run()
    }
}