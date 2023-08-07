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

    object Util {
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

    companion object {

        const val DESCRIPTOR = "LaTeX"
        const val FILE_TEMPLATE_TEX = "LaTeX Source.tex"
        const val FILE_TEMPLATE_TEX_WITH_BIB = "LaTeX Source With BibTeX.tex"
        const val FILE_TEMPLATE_STY = "LaTeX Package.sty"
        const val FILE_TEMPLATE_CLS = "LaTeX Document class.cls"
        const val FILE_TEMPLATE_BIB = "BibTeX Bibliography.bib"
        const val FILE_TEMPLATE_TIKZ = "TikZ Picture.tikz"

    }

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val descriptor = FileTemplateGroupDescriptor(
            DESCRIPTOR,
            TexifyIcons.LATEX_FILE
        )

        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_TEX, TexifyIcons.LATEX_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_TEX_WITH_BIB, TexifyIcons.LATEX_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_STY, TexifyIcons.STYLE_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_CLS, TexifyIcons.CLASS_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_BIB, TexifyIcons.BIBLIOGRAPHY_FILE))
        descriptor.addTemplate(FileTemplateDescriptor(FILE_TEMPLATE_TIKZ, TexifyIcons.TIKZ_FILE))

        return descriptor
    }
}
