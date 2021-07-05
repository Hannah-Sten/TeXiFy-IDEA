package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.ImageUtil
import nl.hannahsten.texifyidea.ui.ImagePanel
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.addLabeledComponent
import nl.hannahsten.texifyidea.util.formatAsFileName
import org.apache.commons.io.FilenameUtils
import org.jsoup.Jsoup
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * @author Hannah Schellekens
 */
class SaveImageFromClipboardDialog(

        /**
         * The current project.
         */
        val project: Project,

        /**
         * The transferable data from the clipboard.
         * Must support the image data flavor [DataFlavor.imageFlavor].
         */
        private val transferable: Transferable,

        /**
         * The function to execute when the dialog is succesfully closed.
         * Does not execute when cancelled.
         */
        onOkFunction: (SaveImageFromClipboardDialog) -> Unit
) : DialogWrapper(true) {

    companion object {

        fun supportsImage(transferable: Transferable): Boolean {
            return transferable.isDataFlavorSupported(DataFlavor.imageFlavor) && transferable.getTransferData(DataFlavor.imageFlavor) is BufferedImage
        }
    }

    /**
     * The image data from the clipboard. Only supports BufferedImages currently, see [supportsImage].
     */
    private val image = transferable.getTransferData(DataFlavor.imageFlavor) as BufferedImage

    /**
     * The width of the image in pixels.
     */
    private val imageWidth = image.width

    /**
     * The height of the image in pixels.
     */
    private val imageHeight = image.height

    /**
     * The image format of the original image, if known.
     */
    private var imageFormat: ImageFormat? = null

    /**
     * The name of the original image, if known.
     */
    private var imageName: String? = null

    /**
     * The image file that was saved, `null` when no image was saved.
     */
    var savedImage: File? = null
        private set

    /**
     * Shows a preview of the image to paste.
     */
    private val imgPreview = ImagePanel().apply {
        val widthRatio = 1.0.coerceAtMost(500.0 / imageWidth)
        val heightRatio = 1.0.coerceAtMost(300.0 / imageHeight)
        val ratio = widthRatio.coerceAtLeast(heightRatio)

        val scaled = image.getScaledInstance((imageWidth * ratio).toInt(), (imageHeight * ratio).toInt(), Image.SCALE_SMOOTH)
        setImage(ImageUtil.toBufferedImage(scaled))
    }

    /**
     * Stores the folder where the image is stored in.
     */
    private val txtResourceFolder = TextFieldWithBrowseButton().apply {
        // Use the resource folder by default. If there is no "resources" folder, assume that a non "src/source" folder
        // is a resource folder.
        val roots = ProjectRootManager.getInstance(project).contentSourceRoots
        text = roots.firstOrNull { it.nameWithoutExtension == "resources" }?.path
                ?: roots.firstOrNull { it.nameWithoutExtension.matches(Regex("(src|sources?)", RegexOption.IGNORE_CASE)) }?.path
                        ?: roots.firstOrNull()?.path ?: ""

        addBrowseFolderListener(TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Select Resource Folder...")
        ))
    }

    /**
     * Stores the file name for the image without extension.
     */
    private val txtImageName = JBTextField()

    /**
     * Stores the format of the created image.
     */
    private val cboxImageFormat = ComboBox(ImageFormat.values()).apply {
        selectedItem = ImageFormat.PNG
    }

    init {
        require(transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) { "Image flavor not supported." }

        // Fill in meta data.
        findMetaData()
        imageName?.let { txtImageName.text = it }
        imageFormat?.let { cboxImageFormat.selectedItem = it }

        // Setup dialog.
        super.init()
        title = "Save Image from Clipboard"
        myPreferredFocusedComponent = txtImageName

        if (showAndGet()) {
            saveImage()
            onOkFunction(this@SaveImageFromClipboardDialog)
        }
    }

    override fun createCenterPanel() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        minimumSize = Dimension(512, 64)

        addImageMetaPanel()
        addImageDataInputs()
    }

    override fun doOKAction() {
        close(0)
    }

    override fun doValidate() = when {
        txtResourceFolder.text.isBlank() -> {
            ValidationInfo("You must select a destination folder.", txtResourceFolder)
        }
        txtImageName.text.isBlank() -> {
            ValidationInfo("You must enter an image name.")
        }
        txtImageName.text.trim().formatAsFileName().isBlank() -> {
            ValidationInfo("You must enter a valid image name (invalid characters).", txtImageName)
        }
        destinationFile()?.exists() ?: false -> {
            ValidationInfo("This file already exists", txtImageName)
        }
        else -> null
    }

    /**
     * Saves the pasted image.
     */
    private fun saveImage() {
        val imageFormat = cboxImageFormat.selectedItem as? ImageFormat ?: return
        val destination = destinationFile() ?: return

        createDirectories()

        if (ImageIO.write(image, imageFormat.extension, destination)) {
            savedImage = destination
            LocalFileSystem.getInstance().refresh(true)
        }
    }

    /**
     * Get the file where the image must be stored.
     * Also creates the necessary directories.
     */
    private fun destinationFile(): File? {
        val imageFormat = cboxImageFormat.selectedItem as? ImageFormat ?: return null

        val directory = File(txtResourceFolder.text)
        val validImageName = txtImageName.text.trim().formatAsFileName()
        val fileName = "$validImageName.${imageFormat.extension}"
        return File("${directory.path}/$fileName")
    }

    /**
     * Creates all necessary directories, if needed.
     */
    private fun createDirectories() {
        val directory = File(txtResourceFolder.text)
        if (directory.exists().not()) {
            directory.mkdirs()
        }
    }

    /**
     * Adds the panel that shows information about the pasted image.
     */
    private fun JPanel.addImageMetaPanel() = add(JPanel(BorderLayout(16, 16)).apply {
        border = EmptyBorder(16, 16, 16, 16)

        add(imgPreview, BorderLayout.WEST)

        // Info labels.
        add(JBLabel("""<html>
            <table>
                <tr>
                    <td><b>Size:</b> </td>
                    <td>${this@SaveImageFromClipboardDialog.imageWidth} x ${this@SaveImageFromClipboardDialog.imageHeight} pixels</td>
                </tr>
                <tr>
                    <td width="64"><b>Format:</b> </td>
                    <td>${imageFormat ?: "unknown"}</td>
                </tr>
                <tr>
                    <td><b>Name:</b> </td>
                    <td>${imageName ?: "unknown"}</td>
                </tr>
            </table>
        </html>"""))
    })

    /**
     * Adds the user input controls to enter the image information.
     */
    private fun JPanel.addImageDataInputs() {
        val labelWidth = 128
        addLabeledComponent(txtResourceFolder, "Resource folder:", labelWidth)
        addLabeledComponent(txtImageName, "Image name:", labelWidth)
        addLabeledComponent(cboxImageFormat, "Image format:", labelWidth)
    }

    /**
     * Checks if there is HTML data present on the clipboard, and extract meta data of the image from this
     * data when present.
     */
    private fun findMetaData() {
        // Check if there is HTML image metadata present.
        if (transferable.isDataFlavorSupported(DataFlavor.fragmentHtmlFlavor).not()) return

        // Parse clipboard data.
        val clipboardData = transferable.getTransferData(DataFlavor.fragmentHtmlFlavor) as String
        val htmlFragment = Clipboard.extractHtmlFragmentFromClipboard(clipboardData)
        val html = Jsoup.parse(htmlFragment)
        val image = html.select("img").firstOrNull() ?: return

        // Handle data.
        val source = image.attr("src") ?: return
        imageFormat = ImageFormat.imageFormatFromExtension(FilenameUtils.getExtension(source))
        imageName = FilenameUtils.getBaseName(source)
    }

    /**
     * The image formats that are supported by this dialog.
     *
     * @author Hannah Schellekens
     */
    enum class ImageFormat(val extension: String) {

        PNG("png"),
        JPG("jpg");

        override fun toString() = extension

        companion object {

            fun imageFormatFromExtension(extension: String) = when (extension.toLowerCase()) {
                "jpg", "jpeg" -> JPG
                else -> PNG
            }
        }
    }
}