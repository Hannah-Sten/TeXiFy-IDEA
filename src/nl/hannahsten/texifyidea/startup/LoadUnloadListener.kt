package nl.hannahsten.texifyidea.startup

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceInverseSearchListener

class LoadUnloadListener : DynamicPluginListener {

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        runBlocking { EvinceInverseSearchListener.unload() }
        super.beforePluginUnload(pluginDescriptor, isUpdate)
    }
}