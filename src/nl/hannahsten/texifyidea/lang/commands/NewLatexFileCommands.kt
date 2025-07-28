package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.*

object NewLatexFileCommands : PredefinedCommandSet() {

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

        // Other miscellaneous file inputs, e.g., from glossaries.
        packageOf("glossaries")
        "loadglsentries".cmd("glossariesfile".required(LatexContexts.SingleTexFile)) {
            "Load glossary entries from a file"
        }

        packageOf("minted")
        "inputminted".cmd(
            LArgument.required("language", LatexContexts.MintedFuntimeLand),
            LArgument.required("sourcefile", LatexContexts.SingleFile),
        ) {
            "Input a source file with syntax highlighting"
        }
    }
}