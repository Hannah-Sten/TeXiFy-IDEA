package org.gnome.evince

import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.messages.DBusSignal
import org.freedesktop.dbus.types.UInt32

/**
 * Interface to communicate with an Evince Window over DBus, such as syncing in both directions
 *
 * @author Tim Klocke
 */
@Suppress("FunctionName")
@DBusInterfaceName("org.gnome.evince.Window")
interface Window : DBusInterface {

    /**
     * Sync the position of Evince to the corresponding position.
     *
     * @param sourceFile Path to the tex file, prepended with file://
     * @param sourcePoint Position in the document to jump to.
     * @param timestamp Timestamp
     * @return
     */
    fun SyncView(sourceFile: String?, sourcePoint: SyncViewSourcePointStruct?, timestamp: UInt32?)

    /**
     * Signal to sync the position of the editor to the specified position.
     *
     * @param path Path to the DBusObject
     * @param sourceFile Path to the tex file, prepended with file://
     * @param sourcePoint Position in the document to jump to.
     * @param timestamp Timestamp
     * @return
     */
    class SyncSource(
        path: String?,
        val sourceFile: String,
        val sourcePoint: SyncViewSourcePointStruct,
        timestamp: UInt32
    ) : DBusSignal(path, sourceFile, sourcePoint, timestamp)
}
