package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import nl.hannahsten.texifyidea.util.runWriteAction

object LatexIgnoredFileMasks {

    val presetMasks = linkedSetOf(
        "*.aux",
        "*.bbl",
        "*.blg",
        "*.bcf",
        "*.fls",
        "*.fdb_latexmk",
        "*.toc",
        "*.out",
        "*.log",
        "*.synctex",
        "*.synctex.gz",
        "*.nav",
        "*.snm",
        "*.lof",
        "*.lot",
        "*.idx",
        "*.ind",
        "*.ilg",
        "*.glg",
        "*.glo",
        "*.gls",
        "*.ist",
        "*.xdy",
        "*.xdv",
    )

    fun getCurrentMasks(): LinkedHashSet<String> = parseMasks(FileTypeManagerEx.getInstanceEx().ignoredFilesList)

    fun applyMasks(masks: Set<String>) {
        runWriteAction {
            FileTypeManagerEx.getInstanceEx().ignoredFilesList = renderMasks(masks)
        }
    }

    fun findMissingMasks(currentMasks: Set<String>): LinkedHashSet<String> = presetMasks
        .asSequence()
        .filterNot { it in currentMasks }
        .toCollection(linkedSetOf())

    fun mergeWithPreset(currentMasks: Set<String>): LinkedHashSet<String> = (currentMasks + presetMasks)
        .asSequence()
        .map(::normalizeMask)
        .filter { it.isNotEmpty() }
        .toCollection(linkedSetOf())

    fun parseMasks(masks: String): LinkedHashSet<String> = masks
        .split(';')
        .asSequence()
        .map(::normalizeMask)
        .filter { it.isNotEmpty() }
        .toCollection(linkedSetOf())

    fun renderMasks(masks: Set<String>): String = masks
        .asSequence()
        .map(::normalizeMask)
        .filter { it.isNotEmpty() }
        .toCollection(linkedSetOf())
        .joinToString(";")

    private fun normalizeMask(mask: String): String = mask.trim()
}
