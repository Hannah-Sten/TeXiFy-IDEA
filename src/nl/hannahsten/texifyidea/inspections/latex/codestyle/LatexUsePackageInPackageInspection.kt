package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Pattern

class LatexUsePackageInPackageInspection : TexifyRegexInspection(
    inspectionDisplayName = "Use of \\usepackage{...} instead of \\RequirePackage{...}",
    inspectionId = "UsePackageInPackage",
    errorMessage = { "Use \\RequirePackage{...} instead of \\usepackage{...}" },
    // The \usepackage syntax is \\usepackage[<options>]{<package name>}[<version info>].
    pattern = Pattern.compile("\\\\usepackage(\\[[^]]*])?\\{([^}]*)}(\\[[^]]*])?"),
    quickFixName = { "Replace with \\RequirePackage" },
    replacement = { matcher, _ -> "\\RequirePackage${matcher.group(1) ?: ""}{${matcher.group(2)}}${matcher.group(3) ?: ""}" },
    cancelIf = { _, psiFile ->
        // Cancel if the usepackage was found outside a class or style file.
        psiFile.virtualFile?.extension !in setOf(ClassFileType.defaultExtension, StyleFileType.defaultExtension)
    }
)