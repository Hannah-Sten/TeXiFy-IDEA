package nl.hannahsten.texifyidea.util

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary

/**
 * This class collects attributes for any sensitive data stored by TeXiFy.
 */
object CredentialAttributes {

    object Zotero {

        val userAttributes = createCredentialsAttributes(ZoteroLibrary.NAME)
    }

    object Mendeley {

        val tokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-token")

        val refreshTokenAttributes = createCredentialsAttributes("${MendeleyLibrary.NAME}-refresh-token")
    }

    fun createCredentialsAttributes(key: String): CredentialAttributes = CredentialAttributes(generateServiceName("TeXiFy", key))
}