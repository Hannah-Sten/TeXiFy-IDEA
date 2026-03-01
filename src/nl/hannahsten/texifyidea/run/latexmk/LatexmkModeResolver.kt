package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.util.magic.PackageMagic

internal fun preferredCompileModeForPackages(packages: Set<LatexLib>): LatexmkCompileMode? {
    if (packages.any { it in PackageMagic.preferredXeEngineLibraries }) {
        return LatexmkCompileMode.XELATEX_PDF
    }
    if (packages.any { it in PackageMagic.preferredLuaEngineLibraries }) {
        return LatexmkCompileMode.LUALATEX_PDF
    }
    return null
}

internal fun compileModeFromMagicCommand(command: String?): LatexmkCompileMode? {
    if (command.isNullOrBlank()) return null
    val parsed = ParametersListUtil.parse(command).filter { it.isNotBlank() }
    if (parsed.isEmpty()) return null

    val executable = parsed.first().normalizedExecutable()
    val flags = parsed.drop(1).map { it.lowercase() }

    return when (executable) {
        "latexmk" -> compileModeFromLatexmkFlags(flags)
        "pdflatex" -> LatexmkCompileMode.PDFLATEX_PDF
        "xelatex" -> LatexmkCompileMode.XELATEX_PDF
        "lualatex" -> LatexmkCompileMode.LUALATEX_PDF
        "latex" -> LatexmkCompileMode.LATEX_DVI
        "tectonic", "arara" -> null
        else -> null
    }
}

private fun compileModeFromLatexmkFlags(flags: List<String>): LatexmkCompileMode {
    if (flags.any { it == "-pdflatex" || it.startsWith("-pdflatex=") }) {
        return LatexmkCompileMode.CUSTOM
    }
    if (flags.contains("-lualatex")) {
        return LatexmkCompileMode.LUALATEX_PDF
    }
    if (flags.contains("-xelatex")) {
        return if (flags.contains("-xdv")) LatexmkCompileMode.XELATEX_XDV else LatexmkCompileMode.XELATEX_PDF
    }
    if (flags.contains("-latex")) {
        if (flags.contains("-ps")) return LatexmkCompileMode.LATEX_PS
        if (flags.contains("-dvi")) return LatexmkCompileMode.LATEX_DVI
    }

    return LatexmkCompileMode.PDFLATEX_PDF
}

private fun String.normalizedExecutable(): String {
    val fileName = substringAfterLast('/').substringAfterLast('\\')
    return fileName
        .lowercase()
        .removeSuffix(".exe")
        .removeSuffix(".cmd")
        .removeSuffix(".bin")
        .removeSuffix(".bat")
}
