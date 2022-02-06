package nl.hannahsten.texifyidea.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.util.Container
import java.io.IOException
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexTemplatesFactory : FileTemplateGroupDescriptorFactory {

    companion object {

        const val descriptor = "LaTeX"
        const val fileTemplateTex = "LaTeX Source.tex"
        const val fileTemplateTexWithBib = "LaTeX Source With BibTeX.tex"
        const val fileTemplateSty = "LaTeX Package.sty"
        const val fileTemplateCls = "LaTeX Document class.cls"
        const val fileTemplateBib = "BibTeX Bibliography.bib"
        const val fileTemplateTikz = "TikZ Picture.tikz"

        @JvmStatic
        fun createFromTemplate(
            directory: PsiDirectory, fileName: String,
            templateName: String, fileType: FileType
        ): PsiFile {
            val project = directory.project
            val templateText = getTemplateText(project, templateName, fileName)

            val fileFactory = PsiFileFactory.getInstance(project)
            val file = fileFactory.createFileFromText(fileName, fileType, templateText)

            val createdFile = Container<PsiFile>()
            val application = ApplicationManager.getApplication()
            application.runWriteAction { createdFile.item = directory.add(file) as PsiFile }

            return createdFile.item ?: throw TeXception("No created file in container.")
        }

        /**
         * Get the text from a certain template.
         *
         * @param project
         * The IntelliJ project that contains the templates.
         * @param templateName
         * The name of the template. Use the constants prefixed with `FILE_TEMPLATE_` from
         * [LatexTemplatesFactory].
         * @return The contents of the template with applied properties.
         */
        @JvmStatic
        fun getTemplateText(project: Project, templateName: String, fileName: String): String {
            val templateManager = FileTemplateManager.getInstance(project)
            val template = templateManager.getInternalTemplate(templateName)
            val properties = Properties(templateManager.defaultProperties)
            properties["FILE_NAME"] = fileName

            try {
                return template.getText(properties)
            }
            catch (e: IOException) {
                throw TeXception("Could not load template $templateName", e)
            }
        }
    }

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val descriptor = FileTemplateGroupDescriptor(
            descriptor,
            TexifyIcons.LATEX_FILE
        )

        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateTex, TexifyIcons.LATEX_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateTexWithBib, TexifyIcons.LATEX_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateSty, TexifyIcons.STYLE_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateCls, TexifyIcons.CLASS_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateBib, TexifyIcons.BIBLIOGRAPHY_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(fileTemplateTikz, TexifyIcons.TIKZ_FILE))

        return descriptor
    }
}
