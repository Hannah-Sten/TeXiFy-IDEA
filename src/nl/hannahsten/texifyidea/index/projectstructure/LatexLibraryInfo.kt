package nl.hannahsten.texifyidea.index.projectstructure

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.lang.LatexLib

class LatexLibraryInfo(
    val name: LatexLib,
    val location: VirtualFile,
    val files: Set<VirtualFile>,
    /**
     * The set of package names that are directly included in this package, without transitive dependencies and this package itself.
     */
    val directDependencies: Set<String>,
    /**
     * The set of all package names that are included in this package, including transitive dependencies and this package itself.
     */
    val allIncludedPackageNames: Set<String>,
) {

    @Suppress("unused")
    val isPackage: Boolean
        get() = name.isPackageFile

    @Suppress("unused")
    val isClass: Boolean
        get() = name.isClassFile

    override fun toString(): String = "PackageInfo(name='$name', location=${location.path}, files=${files.size})"
}