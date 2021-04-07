package nl.hannahsten.texifyidea.startup

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import kotlinx.coroutines.runBlocking
import nl.hannahsten.texifyidea.editor.ControlTracker
import nl.hannahsten.texifyidea.editor.ShiftTracker
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceInverseSearchListener

class LoadUnloadListener : DynamicPluginListener {

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        super.beforePluginUnload(pluginDescriptor, isUpdate)
        AnalyzeMenuRegistration().unload()
        ControlTracker.unload()
        ShiftTracker.unload()
        runBlocking { EvinceInverseSearchListener.unload() }
    }
}