package org.gnome.evince

import org.freedesktop.dbus.interfaces.DBusInterface

/**
 * This interface can be used to call methods on the dbus.
 *
 * @author Thomas Schouten
 */
@Suppress("FunctionName")
interface Daemon : DBusInterface {

    /**
     * Find a certain document.
     * This seems to be unable to handle spaces in a path: the pdf will open but the process owner will not be returned.
     *
     * @param uri Path to a pdf file, prepended with file://
     * @param spawn Whether to spawn Evince or not.
     * @return The name owner of the evince process for the given document URI.
     */
    fun FindDocument(uri: String, spawn: Boolean?): String
}