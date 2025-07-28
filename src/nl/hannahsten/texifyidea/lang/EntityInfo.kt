package nl.hannahsten.texifyidea.lang

interface EntityInfo {
    val name: String
    val dependency: String

    val fqName: String
        get() = if (dependency.isEmpty()) name else "$dependency.$name"
}