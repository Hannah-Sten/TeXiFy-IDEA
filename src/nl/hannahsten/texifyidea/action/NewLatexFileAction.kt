package nl.hannahsten.texifyidea.action

import com.intellij.ide.actions.CreateElementActionBase
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.CreateFileFromTemplateDialog.FileCreator
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.templates.LatexTemplatesFactory
import nl.hannahsten.texifyidea.templates.LatexTemplatesFactory.Companion.createFromTemplate
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.files.FileUtil.fileTypeByExtension

/**
 * @author Hannah Schellekens
 */
class NewLatexFileAction : CreateElementActionBase("LaTeX File", "Create a new LaTeX file", TexifyIcons.LATEX_FILE) {

    override fun invokeDialog(project: Project, psiDirectory: PsiDirectory, elementsConsumer: java.util.function.Consumer<Array<PsiElement>>) {
        val fileCreator = LatexFileCreator(project, psiDirectory)
        val builder = CreateFileFromTemplateDialog.createDialog(project)
        builder.setTitle("Create a New LaTeX File")
        builder.addKind("Sources (.tex)", TexifyIcons.LATEX_FILE, OPTION_TEX_FILE)
        builder.addKind("Bibliography (.bib)", TexifyIcons.BIBLIOGRAPHY_FILE, OPTION_BIB_FILE)
        builder.addKind("Package (.sty)", TexifyIcons.STYLE_FILE, OPTION_STY_FILE)
        builder.addKind("Document class (.cls)", TexifyIcons.CLASS_FILE, OPTION_CLS_FILE)
        builder.addKind("TikZ (.tikz)", TexifyIcons.TIKZ_FILE, OPTION_TIKZ_FILE)
        val consumer = com.intellij.util.Consumer<PsiElement> { }
        builder.show<PsiElement>("", null, fileCreator, consumer)
    }

    override fun create(s: String, psiDirectory: PsiDirectory): Array<PsiElement> {
        return arrayOf()
    }

    override fun getErrorTitle(): String {
        return "Error"
    }

    override fun getActionName(psiDirectory: PsiDirectory, s: String): String {
        return ""
    }

    private inner class LatexFileCreator(private val project: Project, private val directory: PsiDirectory) : FileCreator<PsiElement?> {

        private fun openFile(virtualFile: VirtualFile) {
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(virtualFile, true)
        }

        private fun getTemplateNameFromExtension(extensionWithoutDot: String): String {
            return when (extensionWithoutDot) {
                OPTION_STY_FILE -> LatexTemplatesFactory.fileTemplateSty
                OPTION_CLS_FILE -> LatexTemplatesFactory.fileTemplateCls
                OPTION_BIB_FILE -> LatexTemplatesFactory.fileTemplateBib
                OPTION_TIKZ_FILE -> LatexTemplatesFactory.fileTemplateTikz
                else -> LatexTemplatesFactory.fileTemplateTex
            }
        }

        private fun getFileType(fileName: String, option: String): FileType {
            val smallFileName = fileName.toLowerCase()
            if (smallFileName.endsWith(".$OPTION_TEX_FILE")) {
                return LatexFileType
            }
            if (smallFileName.endsWith(".$OPTION_CLS_FILE")) {
                return ClassFileType
            }
            if (smallFileName.endsWith(".$OPTION_STY_FILE")) {
                return StyleFileType
            }
            if (smallFileName.endsWith(".$OPTION_BIB_FILE")) {
                return BibtexFileType
            }
            return if (smallFileName.endsWith(".$OPTION_TIKZ_FILE")) {
                TikzFileType
            }
            else fileTypeByExtension(option)
        }

        private fun getNewFileName(fileName: String, fileType: FileType): String {
            val smallFileName = fileName.toLowerCase()
            return if (smallFileName.endsWith("." + fileType.defaultExtension)) {
                smallFileName
            }
            else fileName.appendExtension(fileType.defaultExtension)
        }

        override fun createFile(fileName: String, option: String): PsiElement? {
            val fileType = getFileType(fileName, option)
            val newFileName = getNewFileName(fileName, fileType)
            val templateName = getTemplateNameFromExtension(fileType.defaultExtension)
            val file = createFromTemplate(
                directory, newFileName,
                templateName, fileType
            )
            openFile(file.virtualFile)
            return file
        }

        override fun getActionName(fileName: String, option: String): String {
            return "New LaTeX File"
        }

        override fun startInWriteAction(): Boolean {
            return false
        }
    }

    companion object {

        private const val OPTION_TEX_FILE = "tex"
        private const val OPTION_STY_FILE = "sty"
        private const val OPTION_CLS_FILE = "cls"
        private const val OPTION_BIB_FILE = "bib"
        private const val OPTION_TIKZ_FILE = "tikz"
    }
}