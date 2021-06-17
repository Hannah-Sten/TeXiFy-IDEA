package nl.hannahsten.texifyidea.run.macro

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.vfs.VirtualFile

/** Output directory of the LaTeX run configuration. */
val OUTPUT_DIR: DataKey<VirtualFile> = DataKey.create("outputDirectory")