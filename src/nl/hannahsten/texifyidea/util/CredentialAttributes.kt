package nl.hannahsten.texifyidea.util

import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary

object CredentialAttributes {
    object Zotero {
        val userAttributes = createCredentialsAttributes(ZoteroLibrary.NAME)
    }

    object Mendeley {
        val tokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-token")

        val refreshTokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-refresh-token")

    }
}