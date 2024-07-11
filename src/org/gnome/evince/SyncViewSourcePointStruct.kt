package org.gnome.evince

import org.freedesktop.dbus.Struct
import org.freedesktop.dbus.annotations.Position

/**
 * Nested Object in DBus communication with evince, this specifies the position in the document
 *
 * @author Tim Klocke
 */
class SyncViewSourcePointStruct(
    @field:Position(0) val line: Int,
    @field:Position(1) val column: Int
) : Struct()
