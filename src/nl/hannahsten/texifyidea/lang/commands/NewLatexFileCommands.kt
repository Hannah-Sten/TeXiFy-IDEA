package nl.hannahsten.texifyidea.lang.commands


import nl.hannahsten.texifyidea.lang.*

object NewLatexFileCommands : LatexCommandSet() {

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

        "addbibresource".cmd(required("bibliographyfile", LatexContexts.SingleBibFile)) {
            "Add a bibliography resource file"
        }

        "bibliography".cmd(required("bibliographyfile", LatexContexts.MultipleBibFiles)) {
            "Specify bibliography files (comma-separated)"
        }


        // Graphics-related.
        packageOf("graphicx")
        "includegraphics".cmd(
            optional("key-val-list", LatexContexts.Literal),
            required("imagefile", PICTURE_FILE)
        ) {
            "Include a graphics file"
        }

        "graphicspath".cmd(
            required("foldername", LatexContexts.Folder)  // Folder path, no specific ext.
        ) {
            "Set the graphics search path"
        }

        // SVG package.
        packageOf("svg")
        "includesvg".cmd(
            optional("options"),
            required("svg file", SVG_FILE)
        ) {
            "Include an SVG file"
        }

        "svgpath".cmd(required("foldername", LatexContexts.Folder)) {
            "Set the SVG search path"
        }

        // Standalone package.
        packageOf("standalone")
        "includestandalone".cmd(
            optional("mode"),
            required("filename", STANDALONE_FILE)
        ) {
            "Include a standalone TeX or graphics file"
        }

        // Import package commands.
        packageOf("import")
        "import".cmd(
            required("absolute path", LatexContexts.Folder),  // Folder-like.
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Import a file from an absolute path"
        }

        "includefrom".cmd(
            required("absolute path", LatexContexts.Folder),
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Include from an absolute path"
        }

        "inputfrom".cmd(
            required("absolute path", LatexContexts.Folder),
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Input from an absolute path"
        }

        "subimport".cmd(
            required("relative path", LatexContexts.Folder),
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Subimport from a relative path"
        }

        "subincludefrom".cmd(
            required("relative path", LatexContexts.Folder),
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Subinclude from a relative path"
        }

        "subinputfrom".cmd(
            required("relative path", LatexContexts.Folder),
            required("filename", LatexContexts.SingleTexFile)
        ) {
            "Subinput from a relative path"
        }

        // Subfiles package.
        packageOf("subfiles")
        "subfile".cmd(required("sourcefile", LatexContexts.SingleTexFile)) {
            "Include a subfile"
        }

        "subfileinclude".cmd(required("sourcefile", LatexContexts.SingleTexFile)) {
            "Include a subfile with page break"
        }

        "subfix".cmd(required("file", LatexContexts.SingleTexFile)) {
            "Fix subfile paths"
        }

        // Other miscellaneous file inputs, e.g., from glossaries.
        packageOf("glossaries")
        "loadglsentries".cmd(required("glossariesfile", LatexContexts.SingleTexFile)) {
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