package nl.hannahsten.texifyidea.run.macro

import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacroManager
import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import java.awt.Component

/**
 * Consider the given path, and replace an absolute part of it by the ProjectFileDirMacro if possible.
 */
fun insertMacro(path: String, component: Component): String {
    // For better readability for the user, replace absolute path with project path when known
    val baseDir = PlatformDataKeys.PROJECT_FILE_DIRECTORY.getData(DataManager.getInstance().getDataContext(component))
    return if (baseDir != null && baseDir.path in path) {
        path.replace(baseDir.path, "\$${ProjectFileDirMacro().name}\$")
    }
    else {
        path
    }
}

val relevantDatakeys = arrayOf(
    CommonDataKeys.PROJECT,
    PlatformDataKeys.PROJECT_FILE_DIRECTORY,
    LangDataKeys.MODULE,
    CommonDataKeys.PSI_FILE
)

/**
 * Insert macros into the pathWithMacro, and resolved macros in expandedPath.
 * We need to do some extra work here to make available the macro which resolves to the directory of the main file.
 *
 * @param givenText: If not provided, assume component is LabeledComponent<TextFieldWithBrowseButton> and get text from there.
 * @return expandedPath, pathWithMacro
 */
fun sortOutMacros(component: Component, runConfig: LatexRunConfiguration, givenText: String? = null): Pair<String?, String?> {
    val text = givenText ?: ((component as LabeledComponent<*>).component as TextFieldWithBrowseButton).text
    val pathWithMacro = insertMacro(text, component)

    // Inject data into context
    // This makes available macros such as $FileDir$ which will point to the directory of the main file
    // Unfortunately, the preview will not be correct because it's taking for some reason the open FileEditor (yes, of the open file) component for the context, which is not necessarily the right file. See MacroManager#getCorrectContext
    val parentContext = DataManager.getInstance().getDataContext(component)
    // todo check keys and whether this works
    val reallyCorrectContext = SimpleDataContext.builder().addAll(parentContext, *relevantDatakeys)
        .add(CommonDataKeys.VIRTUAL_FILE, runConfig.options.mainFile.resolve())
        .add(OUTPUT_DIR, runConfig.options.outputPath.resolve())
        .build()

    // Resolve macro using our own context with corrected file
    val expandedPath = MacroManager.getInstance().expandMacrosInString(pathWithMacro, true, reallyCorrectContext)
    return Pair(expandedPath, pathWithMacro)
}