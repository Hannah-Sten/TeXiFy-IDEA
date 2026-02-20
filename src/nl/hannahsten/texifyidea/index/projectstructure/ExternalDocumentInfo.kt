package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.vfs.VirtualFile

data class ExternalDocumentInfo(
    val labelPrefix: String,
    val files: Set<VirtualFile>,
)