package nl.hannahsten.texifyidea.util.text

import nl.hannahsten.texifyidea.util.capitalizeFirst
import java.util.regex.Pattern
import kotlin.random.Random

/**
 * Generates random filler/dummy text based on a template.
 *
 * The generator uses a template input file in the following format:
 *
 * ```
 * KEY1
 * value1
 * value2
 * value3
 * ..
 * valueN
 *
 * KEY2
 * value1
 * ..
 * valueN
 *
 * TEXT
 * sentence1
 * sentence2
 * ..
 * sentenceN
 *
 * ```
 * Keys used in sentences (as `$KEY1`) will be substituted by a random value.
 *
 * @author Hannah Schellekens
 */
open class TexifyIpsumGenerator(

    /**
     * minimum amount of paragraphs..maximum amount of paragraphs.
     * Definitive amount is randomly determined.
     */
    private val numberOfParagraphs: IntRange = 3..7,

    /**
     * minimum amount of sentences in a paragraph..maximum amount of sentences in a paragraph.
     * Definitive amount is randomly determined for each paragraph.
     */
    private val numberOfSentences: IntRange = 2..20,

    /**
     * The random object to use to generate randomness.
     */
    val random: Random = Random(Random.nextInt()),

    /**
     * What ipsum template to use.
     */
    val ipsum: Ipsum = Ipsum.TEXIFY_IDEA_IPSUM
) {

    /**
     * Maps each KEY to the list of options.
     * Only options with the TEXT key can contain $TEMPLATE keys.
     */
    private val template: Map<String, List<String>> by lazy {
        val input = ipsum.generateInput()
        // Whether the newlines are Windows or Unix style depends on which system the plugin was built, so catch both
        val parts = input.split(Pattern.compile("(\\r?\\n){2,}"))

        parts.associate { templatePart ->
            val lines = templatePart.lines()
            val key = lines.firstOrNull() ?: error("Invalid template format, no lines found")
            key to lines.drop(1)
        }
    }

    /**
     * All available template keys.
     */
    private val templateKeys: Set<String> by lazy {
        template.keys - setOf("TEXT")
    }

    /**
     * All available sentences in the template.
     */
    private val sentences: List<String> by lazy {
        template["TEXT"] ?: error("No TEXT key found.")
    }

    /**
     * Generates a list of paragraphs (list of sentences).
     */
    fun generate(): List<List<String>> {
        val paragraphCount = numberOfParagraphs.random(random)
        return (1..paragraphCount).map {
            generateParagraph()
        }
    }

    /**
     * Generates a random paragraph with a random size in range [numberOfSentences].
     */
    private fun generateParagraph(): List<String> {
        val sentenceCount = numberOfSentences.random(random)
        return (1..sentenceCount).map {
            sentences.random(random).applyTemplates().capitalizeFirst()
        }
    }

    /**
     * Substitutes all templates with random subtitutions.
     */
    private fun String.applyTemplates(): String {
        var result = this
        templateKeys.forEach { key ->
            val substitutions = template[key] ?: error("Key '$key' not present in template.")
            while (result.contains("$$key")) {
                result = result.replaceFirst("$$key", substitutions.random(random))
            }
        }
        return result
    }
}