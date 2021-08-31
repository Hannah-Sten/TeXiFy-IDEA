package nl.hannahsten.texifyidea.action.reformat

import nl.hannahsten.texifyidea.file.BibtexFileType

/**
 * Run external tool 'bibtex-tidy' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithBibtexTidy : ExternalReformatAction("Reformat File with bibtex-tidy", { it.fileType == BibtexFileType } ) {

    override fun getCommand(fileName: String): List<String> {
        return listOf("bibtex-tidy", fileName, "--no-backup")
    }
}