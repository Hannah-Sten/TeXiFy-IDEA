package nl.hannahsten.texifyidea.remotelibraries.state

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.runReadAction
import com.intellij.util.xmlb.Converter
import nl.hannahsten.texifyidea.psi.BibtexEntry

/**
 * Converter to serialize the library map from and to a string. We use Jackson to convert the map from and to XML. This
 * XML will be stored in a `value="<insert xml here>"` tag inside IntelliJs XML, so we end up with some form of nested XML. Fun.
 */
class LibraryStateConverter : Converter<Map<String, LibraryState>>() {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class LibraryWrapper(
        val displayName: String = "",
        val libraryType: Class<*> = Any::class.java,
        val bibtex: BibItems = BibItems()
    )

    data class BibItems(val items: List<BibtexEntry> = emptyList())

    override fun toString(value: Map<String, LibraryState>): String? {
        val module = JacksonXmlModule()

        module.addSerializer(BibItems::class.java, object : JsonSerializer<BibItems>() {
            override fun serialize(p0: BibItems, p1: JsonGenerator, p2: SerializerProvider) {
                runReadAction { p1.writeString(BibtexEntryListConverter().toString(p0.items)) }
            }
        })

        return XmlMapper(module)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(value.mapValues { LibraryWrapper(it.value.displayName, it.value.libraryType, BibItems(it.value.entries)) })
    }

    override fun fromString(value: String): Map<String, LibraryState> {
        val module = JacksonXmlModule()

        module.addDeserializer(BibItems::class.java, object : JsonDeserializer<BibItems>() {
            override fun deserialize(p0: JsonParser, p1: DeserializationContext): BibItems {
                    return runReadAction { BibItems(BibtexEntryListConverter().fromString(p0.text)) }
            }
        })

        return XmlMapper(module)
            .readValue<Map<String, LibraryWrapper>>(value)
            .mapValues { LibraryState(it.value.displayName, it.value.libraryType, it.value.bibtex.items) }
    }
}