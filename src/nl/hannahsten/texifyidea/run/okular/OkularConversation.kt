package nl.hannahsten.texifyidea.run.okular

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.runCommand

fun isOkularAvailable() : Boolean {
    // Only support Evince on Linux, although it can be installed on other systems like Mac
    if (!SystemInfo.isLinux) {
        return false
    }

    // Find out whether Okular is installed and in PATH, otherwise we can't use it
    val output = "which okular".runCommand()
    return output?.contains("/okular") ?: false
}