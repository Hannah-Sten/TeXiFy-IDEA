package nl.hannahsten.texifyidea.util.files

import java.nio.file.Path
import kotlin.io.path.name

internal object LatexTemporaryBuildArtifacts {

    val extensions: Set<String> = linkedSetOf(
        "aux",
        "log",
        "bbl",
        "bcf",
        "brf",
        "fls",
        "idx",
        "ind",
        "lof",
        "lot",
        "nav",
        "out",
        "snm",
        "toc",
        "glo",
        "gls",
        "ist",
        "xdy",
        "blg",
        "ilg",
        "glg",
        "glstex",
        "fdb_latexmk",
        "xdv",
    )

    val suffixes: Set<String> = linkedSetOf(
        ".synctex",
        ".synctex.gz",
        ".synctex(busy)",
    )

    private val mainDocumentSuffixes: Set<String> = linkedSetOf<String>().apply {
        addAll(suffixes)
        extensions.mapTo(this) { ".$it" }
    }

    val ignoredFileMasks: Set<String> = linkedSetOf<String>().apply {
        extensions.mapTo(this) { "*.$it" }
        suffixes.mapTo(this) { "*$it" }
    }

    fun matches(path: Path): Boolean = matches(path.name)

    fun matches(fileName: String): Boolean {
        val normalized = fileName.lowercase()
        return suffixes.any(normalized::endsWith) || extensions.any { normalized.endsWith(".$it") }
    }

    fun matchesMainDocumentArtifact(path: Path, mainBaseName: String): Boolean {
        val fileName = path.name
        if (!fileName.startsWith(mainBaseName, ignoreCase = true)) {
            return false
        }

        val artifactSuffix = fileName.substring(mainBaseName.length).lowercase()
        return artifactSuffix in mainDocumentSuffixes
    }
}
