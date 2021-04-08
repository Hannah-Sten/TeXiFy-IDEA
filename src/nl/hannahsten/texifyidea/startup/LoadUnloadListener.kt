package nl.hannahsten.texifyidea.startup

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor

class LoadUnloadListener : DynamicPluginListener {

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        AnalyzeMenuRegistration().unload()
        // Apparently it's not needed to unload these anymore? Were a problem in the past.
//        ControlTracker.unload()
//        ShiftTracker.unload()
//        runBlocking { EvinceInverseSearchListener.unload() }
        super.beforePluginUnload(pluginDescriptor, isUpdate)
    }
}