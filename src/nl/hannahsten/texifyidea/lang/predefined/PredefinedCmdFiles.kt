package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required

object PredefinedCmdFiles : PredefinedCommandSet() {

    private val singleTexFile = SimpleFileInputContext(
        "file.tex", isCommaSeparated = false, supportedExtensions = setOf("tex")
    )

    private val classArgument = required(
        "class",
        setOf(
            LatexContexts.ClassName,
            SimpleFileInputContext(
                "file.cls", isCommaSeparated = false, supportedExtensions = setOf("cls")
            )
        )
    )
    private val packageArg = required(
        "package",
        setOf(
            LatexContexts.PackageNames,
            SimpleFileInputContext(
                "files.sty", isCommaSeparated = true, supportedExtensions = setOf("sty")
            )
        )
    )
    private val texFileArg = required("tex file", singleTexFile)
    private val relativeSingleTexFileArg = required(
        "filename",
        SimpleFileInputContext(
            "relative.file.tex", isCommaSeparated = false, supportedExtensions = setOf("tex"),
            isAbsolutePathSupported = false
        )
    )
    private val relativeMultipleTexFilesArg = required(
        "files",
        SimpleFileInputContext(
            "relative.files.tex", isCommaSeparated = true, supportedExtensions = setOf("tex"),
            isAbsolutePathSupported = false
        )
    )

    val cmdDocumentClass: LSemanticCommand

    val basicFileInputCommands: List<LSemanticCommand> = buildCommands {
        // Most file inputs are in preamble, but can be adjusted per command if needed.
        val name = required("name", LatexContexts.Identifier)
        underContext(LatexContexts.Preamble) {
            cmdDocumentClass = "documentclass".cmd(
                "options".optional(LatexContexts.Literal),
                classArgument
            ) {
                "Declare the document class"
            }

            "usepackage".cmd(
                "options".optional(LatexContexts.Literal),
                packageArg,
            ) {
                "Load a LaTeX package"
            }
            "LoadClass".cmd(
                "options".optional(LatexContexts.Literal),
                classArgument
            ) {
                "Load a class file"
            }
            "LoadClassWithOptions".cmd(classArgument)

            "ProvidesClass".cmd(name)
            "ProvidesPackage".cmd(name)
            "RequirePackage".cmd("options".optional, packageArg)

            "includeonly".cmd(relativeMultipleTexFilesArg) {
                "Specify which files to include (comma-separated)"
            }

            "addtoluatexpath".cmd("paths".required(LatexContexts.Folder)) {
                "Add a relative path to the LaTeX search path"
            }

            "externaldocument".cmd("prefix".optional, relativeSingleTexFileArg)
        }

        // Include and input commands.
        "include".cmd(relativeSingleTexFileArg) {
            "Include a TeX file (page break before)"
        }

        "input".cmd(texFileArg) {
            "Input a TeX file (no page break)"
        }
    }

    // Predefine additional file input contexts if needed, based on common file types.
    // These can be moved to LatexContext.kt if they are reusable across multiple command sets.
    private val GRAPHICS_EXTENSIONS = setOf(
        "pdf", "png", "jpg", "mps", "jpeg", "jbig2", "jb2",
        "PDF", "PNG", "JPG", "JPEG", "JBIG2", "JB2", "eps", "bmp", "gif", "tiff"
    )
    private val PICTURE_FILE = SimpleFileInputContext(
        "file.picture",
        isCommaSeparated = false,
        supportedExtensions = GRAPHICS_EXTENSIONS
    )

    private val SVG_FILE = SimpleFileInputContext(
        "file.svg",
        isCommaSeparated = false,
        supportedExtensions = setOf("svg")
    )

    private val relativeStandalone = SimpleFileInputContext(
        "file.standalone",
        isCommaSeparated = false,
        supportedExtensions = setOf("tex") + GRAPHICS_EXTENSIONS,
        isAbsolutePathSupported = false,
    )

    val commands = buildCommands {

        underBase {
            "bibliography".cmd("bibliographyfile".required(LatexContexts.MultipleBibFiles)) {
                "Specify bibliography files (comma-separated)"
            }
        }

        // Bibliography-related file inputs.
        underPackage(LatexLib.BIBLATEX) {
            "addbibresource".cmd("bibliographyfile".required(LatexContexts.SingleBibFile)) {
                "Add a bibliography resource file"
            }
        }

        underPackage(LatexLib.STANDALONE) {
            "includestandalone".cmd(
                "mode".optional,
                "filename".required(relativeStandalone)
            ) {
                "Include a standalone TeX or graphics file"
            }
        }

        // Other miscellaneous file inputs, e.g., from glossaries.
        packageOf("glossaries")
        "loadglsentries".cmd("glossariesfile".required(singleTexFile)) {
            "Load glossary entries from a file"
        }

        packageOf("minted")
        "inputminted".cmd(
            required("language", LatexContexts.MintedFuntimeLand),
            required("sourcefile", LatexContexts.SingleFile),
        ) {
            "Input a source file with syntax highlighting"
        }
    }

    val importAbsolute = buildCommands {
        // Import package commands.
        packageOf("import")
        "import".cmd(
            "absolute path".required(LatexContexts.Folder), // Folder-like.
            relativeSingleTexFileArg
        ) {
            "Import a file from an absolute path"
        }

        "includefrom".cmd(
            "absolute path".required(LatexContexts.Folder),
            relativeSingleTexFileArg
        ) {
            "Include from an absolute path"
        }

        "inputfrom".cmd(
            "absolute path".required(LatexContexts.Folder),
            relativeSingleTexFileArg
        ) {
            "Input from an absolute path"
        }
    }

    val importRelative = buildCommands {
        packageOf("import")
        "subimport".cmd(
            "relative path".required(LatexContexts.Folder),
            relativeSingleTexFileArg
        ) {
            "Subimport from a relative path"
        }

        "subincludefrom".cmd(
            "relative path".required(LatexContexts.Folder),
            relativeSingleTexFileArg
        ) {
            "Subinclude from a relative path"
        }

        "subinputfrom".cmd(
            "relative path".required(LatexContexts.Folder),
            relativeSingleTexFileArg
        ) {
            "Subinput from a relative path"
        }
    }

    val subfix = buildCommands {
        // Subfiles package.
        packageOf("subfiles")
        "subfile".cmd("sourcefile".required(singleTexFile)) {
            "Include a subfile"
        }

        "subfileinclude".cmd("sourcefile".required(singleTexFile)) {
            "Include a subfile with page break"
        }

        "subfix".cmd("file".required(singleTexFile)) {
            "Fix subfile paths"
        }
    }

    val graphicsRelated = buildCommands {
        // Graphics-related.
        packageOf("graphicx")
        "includegraphics".cmd(
            "key-val-list".optional(LatexContexts.Literal),
            "imagefile".required(setOf(PICTURE_FILE, LatexContexts.PicturePath))
        ) {
            "Include a graphics file"
        }

        "graphicspath".cmd(
            "foldername".required(LatexContexts.Folder) // Folder path, no specific ext.
        ) {
            "Set the graphics search path"
        }

        // SVG package.
        packageOf("svg")
        "includesvg".cmd(
            "options".optional,
            "svg file".required(SVG_FILE)
        ) {
            "Include an SVG file"
        }

        "svgpath".cmd("foldername".required(LatexContexts.Folder)) {
            "Set the SVG search path"
        }

        packageOf("tikzit")
        "tikzfig".cmd(relativeSingleTexFileArg)
        "ctikzfig".cmd(relativeSingleTexFileArg)
    }
}