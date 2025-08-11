package nl.hannahsten.texifyidea.lang.predefined

import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.lang.LArgument.Companion.required

object PredefinedCmdFiles : PredefinedCommandSet() {

    private val classArgument = required("class", LatexContexts.ClassName)
    private val packageArg = required("package", LatexContexts.PackageNames)
    private val texFileArg = required("tex file", LatexContexts.SingleTexFile)

    val basicFileInputCommands: List<LSemanticCommand> = buildCommands {
        // Most file inputs are in preamble, but can be adjusted per command if needed.
        val name = required("name", LatexContexts.Identifier)
        underContext(LatexContexts.Preamble) {
            // TODO
            "documentclass".cmd(
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

            "includeonly".cmd("tex files".required(LatexContexts.MultipleTexFiles)) {
                "Specify which files to include (comma-separated)"
            }

            "addtoluatexpath".cmd("paths".required(LatexContexts.Folder)) {
                "Add a relative path to the LaTeX search path"
            }
        }

        // Include and input commands.
        "include".cmd(texFileArg) {
            "Include a TeX file (page break before)"
        }

        "input".cmd(texFileArg) {
            "Input a TeX file (no page break)"
        }
    }

    // Predefine additional file input contexts if needed, based on common file types.
    // These can be moved to NewLang.kt if they are reusable across multiple command sets.
    private val GRAPHICS_EXTENSIONS = setOf("pdf", "jpg", "jpeg", "png", "eps", "bmp", "gif", "tiff")
    private val PICTURE_FILE = LFileInputContext(
        "file.picture",
        isCommaSeparated = false,
        supportedExtensions = GRAPHICS_EXTENSIONS
    )

    private val SVG_FILE = LFileInputContext(
        "file.svg",
        isCommaSeparated = false,
        supportedExtensions = setOf("svg")
    )

    private val STANDALONE_FILE = LFileInputContext(
        "file.standalone",
        isCommaSeparated = false,
        supportedExtensions = setOf("tex") + GRAPHICS_EXTENSIONS
    )

    val commands = buildCommands {

        // Bibliography-related file inputs.
        packageOf("biblatex")

        "addbibresource".cmd("bibliographyfile".required(LatexContexts.SingleBibFile)) {
            "Add a bibliography resource file"
        }

        "bibliography".cmd("bibliographyfile".required(LatexContexts.MultipleBibFiles)) {
            "Specify bibliography files (comma-separated)"
        }

        // Graphics-related.
        packageOf("graphicx")
        "includegraphics".cmd(
            "key-val-list".optional(LatexContexts.Literal),
            "imagefile".required(PICTURE_FILE)
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

        // Standalone package.
        packageOf("standalone")
        "includestandalone".cmd(
            "mode".optional,
            "filename".required(STANDALONE_FILE)
        ) {
            "Include a standalone TeX or graphics file"
        }

        // Other miscellaneous file inputs, e.g., from glossaries.
        packageOf("glossaries")
        "loadglsentries".cmd("glossariesfile".required(LatexContexts.SingleTexFile)) {
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

    val importRelative = buildCommands {
        // Import package commands.
        packageOf("import")
        "import".cmd(
            "absolute path".required(LatexContexts.Folder), // Folder-like.
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Import a file from an absolute path"
        }

        "includefrom".cmd(
            "absolute path".required(LatexContexts.Folder),
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Include from an absolute path"
        }

        "inputfrom".cmd(
            "absolute path".required(LatexContexts.Folder),
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Input from an absolute path"
        }
    }

    val importAbsolute = buildCommands {
        packageOf("import")
        "subimport".cmd(
            "relative path".required(LatexContexts.Folder),
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Subimport from a relative path"
        }

        "subincludefrom".cmd(
            "relative path".required(LatexContexts.Folder),
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Subinclude from a relative path"
        }

        "subinputfrom".cmd(
            "relative path".required(LatexContexts.Folder),
            "filename".required(LatexContexts.SingleTexFile)
        ) {
            "Subinput from a relative path"
        }
    }

    val subfix = buildCommands {
        // Subfiles package.
        packageOf("subfiles")
        "subfile".cmd("sourcefile".required(LatexContexts.SingleTexFile)) {
            "Include a subfile"
        }

        "subfileinclude".cmd("sourcefile".required(LatexContexts.SingleTexFile)) {
            "Include a subfile with page break"
        }

        "subfix".cmd("file".required(LatexContexts.SingleTexFile)) {
            "Fix subfile paths"
        }
    }
}