package nl.hannahsten.texifyidea.startup

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor

class LoadUnloadListener : DynamicPluginListener {

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        super.beforePluginUnload(pluginDescriptor, isUpdate)
        AnalyzeMenuRegistration().unload()
//        ControlTracker.unload()
//        ShiftTracker.unload()
//        runBlocking { EvinceInverseSearchListener.unload() }
    }
}