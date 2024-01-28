package nl.hannahsten.texifyidea.modules

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.templates.LatexTemplatesFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * This class provides functionality to create default files from templates, which is useful for example when creating a new project.
 */
class DefaultFileCreator(
    /** The project to add the file to. */
    val project: Project,
    /** The directory path of the file (no seperator and no file name). */
    val path: String
) {

    /**
     * Creates the main.tex file and applies the default file template.
     */
    fun addMainFile(isBibtexEnabled: Boolean) {
        val fileName = "main.tex"
        val mainFilePath = path + File.separator + fileName
        val mainFile = File(mainFilePath)

        // Create main file.
        try {
            mainFile.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .tex file.", e)
        }

        // Apply template.
        val template = if (isBibtexEnabled) {
            LatexTemplatesFactory.FILE_TEMPLATE_TEX_WITH_BIB
        }
        else {
            LatexTemplatesFactory.FILE_TEMPLATE_TEX
        }
        val templateText = LatexTemplatesFactory.Util.getTemplateText(project, template, fileName)

        try {
            FileOutputStream(mainFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .tex template to main file.", e)
        }
    }

    /**
     * Creates the main.bib file and applies the default file template.
     */
    fun addBibFile() {
        val fileName = "main.bib"
        val bibFilePath = path + File.separator + fileName
        val bibFile = File(bibFilePath)

        try {
            bibFile.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .bib file", e)
        }

        // Apply template.
        val template = LatexTemplatesFactory.FILE_TEMPLATE_BIB
        val templateText = LatexTemplatesFactory.Util.getTemplateText(project, template, fileName)

        try {
            FileOutputStream(bibFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .bib template to main bibliography file.", e)
        }
    }
}
