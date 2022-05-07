package nl.hannahsten.texifyidea.bibreferencemanagers

abstract class ReferenceManager {
    abstract fun getCollection(): Set<String>
}