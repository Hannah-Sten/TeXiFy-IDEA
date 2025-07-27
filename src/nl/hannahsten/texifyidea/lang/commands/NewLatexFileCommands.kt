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

        "addbibresource".cmd(required("bibliographyfile", LatexContexts.SINGLE_BIB_FILE)) {
            "Add a bibliography resource file"
        }

        "bibliography".cmd(required("bibliographyfile", LatexContexts.MULTIPLE_BIB_FILES)) {
            "Specify bibliography files (comma-separated)"
        }


        // Graphics-related.
        packageOf("graphicx")
        "includegraphics".cmd(
            optional("key-val-list", LatexContexts.LITERAL),
            required("imagefile", PICTURE_FILE)
        ) {
            "Include a graphics file"
        }

        "graphicspath".cmd(
            required("foldername", LatexContexts.FOLDER)  // Folder path, no specific ext.
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

        "svgpath".cmd(required("foldername", LatexContexts.FOLDER)) {
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
            required("absolute path", LatexContexts.FOLDER),  // Folder-like.
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Import a file from an absolute path"
        }

        "includefrom".cmd(
            required("absolute path", LatexContexts.FOLDER),
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Include from an absolute path"
        }

        "inputfrom".cmd(
            required("absolute path", LatexContexts.FOLDER),
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Input from an absolute path"
        }

        "subimport".cmd(
            required("relative path", LatexContexts.FOLDER),
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Subimport from a relative path"
        }

        "subincludefrom".cmd(
            required("relative path", LatexContexts.FOLDER),
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Subinclude from a relative path"
        }

        "subinputfrom".cmd(
            required("relative path", LatexContexts.FOLDER),
            required("filename", LatexContexts.SINGLE_TEX_FILE)
        ) {
            "Subinput from a relative path"
        }

        // Subfiles package.
        packageOf("subfiles")
        "subfile".cmd(required("sourcefile", LatexContexts.SINGLE_TEX_FILE)) {
            "Include a subfile"
        }

        "subfileinclude".cmd(required("sourcefile", LatexContexts.SINGLE_TEX_FILE)) {
            "Include a subfile with page break"
        }

        "subfix".cmd(required("file", LatexContexts.SINGLE_TEX_FILE)) {
            "Fix subfile paths"
        }

        // Other miscellaneous file inputs, e.g., from glossaries.
        packageOf("glossaries")
        "loadglsentries".cmd(required("glossariesfile", LatexContexts.SINGLE_TEX_FILE)) {
            "Load glossary entries from a file"
        }

        packageOf("minted")
        "inputminted".cmd(
            LArgument.required("language", LatexContexts.MINTED_FUNTIME_LAND),
            LArgument.required("sourcefile", LatexContexts.SINGLE_FILE),
        ) {
            "Input a source file with syntax highlighting"
        }

    }

}