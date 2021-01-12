package nl.hannahsten.texifyidea.file.listeners

import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import nl.hannahsten.texifyidea.util.files.ReferencedFileSetService
import nl.hannahsten.texifyidea.util.magic.FileMagic

class VfsChangeListener : BulkFileListener {

    override fun after(events: MutableList<out VFileEvent>) {
        // Drop cache when files are added/deleted
        events.filter { event -> !event.isFromSave && event.file?.extension in FileMagic.fileTypes.map { it.defaultExtension } }
            .forEach {
                if (it.file != null) {
                    // We drop all caches because that is faster than figuring out which cached values need to be updated
                    ReferencedFileSetService.getInstance().dropAllCaches()
                }
            }
        super.after(events)
    }
}