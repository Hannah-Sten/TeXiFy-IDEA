package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.remotelibraries.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.ZoteroLibrary

object CredentialAttributes {
    object Zotero {
        val userAttributes = createCredentialsAttributes(ZoteroLibrary.NAME)
    }

    object Mendeley {
        val tokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-token")

        val refreshTokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-refresh-token")

    }
}