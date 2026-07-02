package nl.hannahsten.texifyidea.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.text.MessageFormat

class TexifyBundleMessageFormatTest {

    private val defaultBundle = Path.of("resources/messages/TexifyBundle.properties")
    private val localizedBundles = listOf(
        Path.of("resources/messages/TexifyBundle_zh.properties"),
    )
    private val bundles = listOf(defaultBundle) + localizedBundles

    @Test
    fun localizedBundlesHaveSamePropertiesAsDefaultBundle() {
        val expectedKeys = readPropertyKeys(defaultBundle)
        localizedBundles.forEach { bundle ->
            assertEquals(
                "Expected $bundle to define the same properties, in the same order, as $defaultBundle",
                expectedKeys,
                readPropertyKeys(bundle)
            )
        }
    }

    private fun readPropertyKeys(bundle: Path): List<String> =
        Files.readAllLines(bundle).mapNotNull { line ->
            if (line.isBlank() || line.startsWith("#") || !line.contains("=")) return@mapNotNull null
            line.substringBefore('=').trim()
        }

    @Test
    fun noAccidentalEmptyPlaceholders() {
        bundles.forEach { bundle ->
            Files.readAllLines(bundle).forEachIndexed { index, line ->
                if (line.isBlank() || line.startsWith("#") || !line.contains("=")) return@forEachIndexed
                val value = line.substringAfter('=')
                assertTrue(
                    "Found invalid placeholder pattern ''{}'' in $bundle at line ${index + 1}",
                    !value.contains("''{}''")
                )
                assertTrue(
                    "Found invalid placeholder pattern '''{}''' in $bundle at line ${index + 1}",
                    !value.contains("'''{}'''")
                )
            }
        }
    }

    @Test
    fun messageFormatPlaceholdersAreUsable() {
        val placeholderRegex = Regex("""\{(\d+)}""")
        bundles.forEach { bundle ->
            Files.readAllLines(bundle).forEachIndexed { index, line ->
                if (line.isBlank() || line.startsWith("#") || !line.contains("=")) return@forEachIndexed
                val key = line.substringBefore('=').trim()
                val value = line.substringAfter('=')
                val placeholders = placeholderRegex.findAll(value).map { it.groupValues[1].toInt() }.toList()
                if (placeholders.isEmpty()) return@forEachIndexed

                val maxIndex = placeholders.maxOrNull() ?: return@forEachIndexed
                val args = Array(maxIndex + 1) { i -> "ARG_$i" as Any }
                val formatted = MessageFormat.format(value, *args)

                placeholders.distinct().forEach { p ->
                    assertTrue(
                        "Placeholder {$p} was not substituted for key '$key' in $bundle at line ${index + 1}",
                        formatted.contains("ARG_$p")
                    )
                }
            }
        }
    }
}
