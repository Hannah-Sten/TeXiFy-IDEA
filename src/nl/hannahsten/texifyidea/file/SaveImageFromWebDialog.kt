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
import nl.hannahsten.texifyidea.util.addLabeledComponent
import nl.hannahsten.texifyidea.util.formatAsFileName
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * @author Hannah Schellekens
 */
class SaveImageFromWebDialog(

    /**
     * The current project.
     */
    val project: Project,

    /**
     * The transferable data from the clipboard.
     * Must support the image data flavor [DataFlavor.imageFlavor].
     */
    private val image: BufferedImage,

    /**
     * The function to execute when the dialog is succesfully closed.
     * Does not execute when cancelled.
     */
    onOkFunction: (SaveImageFromWebDialog) -> Unit
) : DialogWrapper(true) {

    companion object {

        // Default or lasted used resource folder.
        private var resourceFolder: String? = null
    }

    init {
        if (resourceFolder == null) {
            // Use the resource folder by default. If there is no "resources" folder, assume that a non "src/source" folder
            // is a resource folder.
            resourceFolder = ProjectRootManager.getInstance(project).contentSourceRoots.let { roots ->
                roots.firstOrNull { it.nameWithoutExtension == "resources" }?.path
                    ?: roots.firstOrNull {
                        it.nameWithoutExtension.matches(
                            Regex(
                                "(src|sources?)",
                                RegexOption.IGNORE_CASE
                            )
                        )
                    }?.path
                    ?: roots.firstOrNull()?.path ?: ""
            }
        }
    }

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

        val scaled =
            image.getScaledInstance((imageWidth * ratio).toInt(), (imageHeight * ratio).toInt(), Image.SCALE_SMOOTH)
        setImage(ImageUtil.toBufferedImage(scaled))
    }

    /**
     * Stores the folder where the image is stored in.
     */
    private val txtResourceFolder = TextFieldWithBrowseButton().apply {
        // resourceFolder is only null before the class is initialised
        text = resourceFolder!!

        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Select Resource Folder...")
            )
        )
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
        // Fill in meta data.
        findMetaData()
        imageName?.let { txtImageName.text = it }
        imageFormat?.let { cboxImageFormat.selectedItem = it }

        // Setup dialog.
        super.init()
        title = "Save Image from Web"
        myPreferredFocusedComponent = txtImageName

        if (showAndGet()) {
            saveImage()
            onOkFunction(this@SaveImageFromWebDialog)
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

        // Store user-entered text for next time
        resourceFolder = txtResourceFolder.text
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
    private fun JPanel.addImageMetaPanel() = add(
        JPanel(BorderLayout(16, 16)).apply {
            border = EmptyBorder(16, 16, 16, 16)

            add(imgPreview, BorderLayout.WEST)

            // Info labels.
            add(
                JBLabel(
                    """<html>
            <table>
                <tr>
                    <td><b>Size:</b> </td>
                    <td>${this@SaveImageFromWebDialog.imageWidth} x ${this@SaveImageFromWebDialog.imageHeight} pixels</td>
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
        </html>"""
                )
            )
        }
    )

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

            fun imageFormatFromExtension(extension: String) = when (extension.lowercase(Locale.getDefault())) {
                "jpg", "jpeg" -> JPG
                else -> PNG
            }
        }
    }
}