package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.LatexMathCommand.*

/**
 * @author Hannah Schellekens
 */
object SymbolCategories {

    /**
     * Maps each category to the symbols that are in the category.
     *
     * The categories are ordered, as are the symbols per category.
     */
    val categories: Map<SymbolCategory, List<SymbolUiEntry>> = LinkedHashMap<SymbolCategory, List<SymbolUiEntry>>().apply {

        createCategory("Operators") {
            add(DryUiEntry(description = "plus sign", "+", "misc_plus.png", "+", true))
            add(DryUiEntry(description = "minus sign", "-", "misc_minus.png", "-", true))
            add(PLUS_MINUS)
            add(MINUS_PLUS)
            add(TIMES)
            add(DIV)
            add(ASTERISK)
            add(STAR)
            add(CIRCLE)
            add(BULLET)
            add(DIVIDEONTIMES)
            add(LTIMES)
            add(RTIMES)
            add(CDOT)
            add(DOT_PLUS)
            add(LEFT_THREE_TIMES)
            add(RIGHT_THREE_TIMES)
            add(DryUiEntry(description = "factorial", "!", "misc_factorial.png", "!", true))
            add(AMALGAMATION)
            add(CIRCLED_TIMES)
            add(CIRCLED_PLUS)
            add(CIRCLED_MINUS)
            add(CIRCLED_SLASH)
            add(CIRCLED_DOT)
            add(CIRCLED_CIRCLE)
            add(CIRCLED_DASH)
            add(CIRCLED_ASTERISK)
            add(BIG_CIRCLE)
            add(BOXED_DOT)
            add(BOXED_MINUS)
            add(BOXED_PLUS)
            add(BOXED_TIMES)
            add(DIAMOND)
            add(BIG_TRIANGLE_UP)
            add(TRIANGLE_LEFT)
            add(TRIANGLE_RIGHT)
            add(LHD)
            add(RHD)
            add(UN_LHD)
            add(UN_RHD)
            add(UNION)
            add(INTERSECTION)
            add(MULTISET_UNION)
            add(DOUBLE_UNION)
            add(DOUBLE_INTERSECTION)
            add(WREATH_PRODUCT)
            add(SET_MINUS)
            add(SMALL_SET_MINUS)
            add(SQUARE_CAP)
            add(SQUARE_CUP)
            add(NOT_SIGN)
            add(INVERSED_NOT_SIGN)
            add(WEDGE)
            add(VEE)
            add(WEDGE_BAR)
            add(VEE_BAR)
            add(DOUBLE_BAR_WEDGE)
            add(CURLY_WEDGE)
            add(CURLY_VEE)
            add(DAGGER)
            add(DOUBLE_DAGGER)
            add(INTERCALATE)
            add(N_ARY_INTERSECTION)
            add(N_ARY_UNION)
            add(N_ARY_UNION_WITH_PLUS)
            add(N_ARY_SQUARE_UNION)
            add(N_ARY_PRODUCT)
            add(N_ARY_COPRODUCT)
            add(BIG_WEDGE)
            add(BIG_VEE)
            add(BIG_CIRCLED_DOT)
            add(BIG_CIRCLED_PLUS)
            add(BIG_CIRCLED_TIMES)
            add(SUM)
            add(INTEGRAL)
            add(CONTOUR_INTEGRAL)
            add(DOUBLE_INTEGRAL)
            add(TRIPLE_INTEGRAL)
            add(QUADRUPLE_INTEGRAL)
            add(DOTS_INTEGRAL)
        }

        createCategory("Math functions") {
            add(INVERSE_COSINE)
            add(INVERSE_SINE)
            add(INVERSE_TANGENT)
            add(ARGUMENT)
            add(COSINE)
            add(HYPERBOLIC_COSINE)
            add(COTANGENT)
            add(HYPERBOLIC_COTANGENT)
            add(COSECANT)
            add(DEGREES)
            add(DERMINANT)
            add(DIMENSION)
            add(EXPONENTIAL)
            add(GREATEST_COMMON_DIVISOR)
            add(HOMOMORPHISM)
            add(INFINUM)
            add(KERNEL)
            add(BASE_2_LOGARITHM)
            add(LIMIT)
            add(LIMIT_INFERIOR)
            add(LIMIT_SUPERIOR)
            add(NATURAL_LOGARITHM)
            add(LOGARITHM)
            add(MAXIMUM)
            add(MINIMUM)
            add(PROBABILITY)
            add(INVERSE_LIMIT)
            add(SECANT)
            add(SINE)
            add(HYPERBOLIC_SINE)
            add(SUPREMUM)
            add(TANGENT)
            add(HBOLICTANGENT)
            add(LIMIT_SUPERIOR_VARIANT)
            add(LIMIT_INFERIOR_VARIANT)
            add(DIRECT_LIMIT_VARIANT)
            add(INVERSE_LIMIT_VARIANT)
        }

        createCategory("Relations") {
            add(BOWTIE)
            add(JOIN)
            add(PROPORTIONAL_TO)
            add(PROPORTIONAL_TO_SYMBOL)
            add(WASYSYM_PROPTO)
            add(MULTIMAP)
            add(PITCHFORK)
            add(THEREFORE)
            add(BECAUSE)
            add(DryUiEntry(description = "equals sign", "=", "misc_equals.png", "=", true))
            add(NOT_EQUAL)
            add(EQUIVALENT)
            add(APPROX)
            add(TILDE_OPERATOR)
            add(NOT_SIM)
            add(SIM_EQUALS)
            add(BACKWARDS_SIM_EQUALS)
            add(APPROX_EQUALS)
            add(CONG)
            add(NOT_CONG)
            add(COLON_EQUALSS)
            add(EQUALSS_COLON)
            add(COLON_EQUALS)
            add(EQUALS_COLON)
            add(COLON_APPROX)
            add(COLON_SIM)
            add(DOUBLE_COLON)
            add(DOUBLE_COLON_EQUALSS)
            add(EQUALSS_DOUBLE_COLON)
            add(DOUBLE_COLON_EQUALS)
            add(EQUALS_DOUBLE_COLON)
            add(DOUBLE_COLON_APPROX)
            add(DOUBLE_COLON_SIM)
            add(CIRCLE_EQUALS)
            add(TRIANGLE_EQUALS)
            add(EQUALS_CIRCLE)
            add(BUMP_EQUALS)
            add(DOUBLE_BUMP_EQUALS)
            add(DOT_EQUALS_DOT)
            add(RISING_DOTS_EQUALS)
            add(FALLING_DOTS_EQUALS)
            add(DOT_EQUALS)
            add(SMILE)
            add(FROWN)
            add(ASYMP)
            add(SMALL_FROWN)
            add(SMALL_SMILE)
            add(BETWEEN)
            add(PRECEDES)
            add(SUCCEEDS)
            add(NOT_PRECEEDS)
            add(NOT_SUCCEEDS)
            add(PRECEDES_OR_EQUAL)
            add(SUCCEEDS_OR_EQUALS)
            add(NOT_PRECEDES_OR_EQUALS)
            add(NOT_SUCCEEDS_OR_EQUALS)
            add(CURLY_PRECEDES_OR_EQUALS)
            add(CURLY_SUCCEEDS_OR_EQUALS)
            add(CURLY_EQUALS_PRECEDES)
            add(CURLY_EQUALS_SUCCEEDS)
            add(PRECEDES_SIM)
            add(SUCCEEDS_SIM)
            add(PRECEDES_NOT_SIM)
            add(SUCCEEDS_NOT_SIM)
            add(PRECEDES_APPROX)
            add(SUCCEEDS_APPROX)
            add(PRECEDES_NOT_APPROX)
            add(SUCCEEDS_NOT_APPROX)
            add(PERPENDICULAR)
            add(RIGHT_TACK)
            add(LEFT_TACK)
            add(NOT_RIGHT_TACK)
            add(FORCES)
            add(TRIPLE_RIGHT_TACK)
            add(MODELS)
            add(VERTICAL_DOUBLE_DASH_RIGHT)
            add(NOT_VERTICAL_DOUBLE_DASH_RIGHT)
            add(NOT_DOUBLE_VERTICAL_DOUBLE_DASH_RIGHT)
            add(MID)
            add(NOT_MID)
            add(PARALLEL)
            add(NOT_PARALLEL)
            add(MID_SHORT)
            add(NOT_MID_SHORT)
            add(NOT_PARALLEL_SHORT)
            add(DryUiEntry(description = "less than", "<", "misc_lesser.png", "<", true))
            add(DryUiEntry(description = "greater than", ">", "misc_greater.png", ">", true))
            add(NOT_LESS_THAN)
            add(NOT_GREATER_THAN)
            add(LESS_THAN_DOT)
            add(GREATER_THAN_DOT)
            add(DOUBLE_LESS_THAN)
            add(DOUBLE_GREATER_THAN)
            add(LESS_LESS_LESS)
            add(GREATER_GREATER_GREATER)
            add(LESS_THAN_EQUAL)
            add(GREATER_THAN_EQUAL)
            add(LESS_THAN_NOT_EQUAL)
            add(GREATER_THAN_NOT_EQUAL)
            add(NOT_LESS_THAN)
            add(NOT_GREATER_THAN)
            add(LESS_THAN_EQUALL)
            add(GREATER_THAN_EQUALL)
            add(LESS_NOT_EQUAL)
            add(GREATER_NOT_EQUAL)
            add(LESS_THAN_VERTICAL_NOT_EQUALS)
            add(GREATER_THAN_VERTICAL_NOT_EQUALS)
            add(NOT_LESS_THAN_EQUALL)
            add(NOT_GREATER_THAN_EQUALL)
            add(LESS_THAN_EQUALS_SLANT)
            add(GREATER_THAN_EQUALS_SLANT)
            add(NOT_LESS_THAN_EQUALS_SLANT)
            add(NOT_GREATER_THAN_EQUALS_SLANT)
            add(EQUALS_SLANT_LESS_THAN)
            add(EQUALS_SLANT_GREATER_THAN)
            add(LESS_GREATER)
            add(GREATER_LESS)
            add(LESS_EQUALS_GREATER)
            add(GREATER_EQUALS_LESSER)
            add(LESS_EQUALSS_GREATER)
            add(GREATER_EQUALSS_LESSER)
            add(LESS_SIM)
            add(GREATER_SIM)
            add(LESS_NOT_SIM)
            add(GREATER_NOT_SIM)
            add(LESS_APPROX)
            add(GREATER_APPROX)
            add(LESS_NOT_APPROX)
            add(GREATER_NOT_APPROX)
            add(TRIANGLE_LEFT_VARIATION)
            add(TRIANGLE_RIGHT_VARIATION)
            add(NOT_TRIANGLE_LEFT)
            add(NOT_TRIANGLE_RIGHT)
            add(TRIANGLE_LEFT_EQUALS)
            add(TRIANGLE_RIGHT_EQUALS)
            add(TRIANGLE_LEFT_EQUALS_SLANT)
            add(TRIANGLE_RIGHT_EQUALS_SLANT)
            add(NOT_TRIANGLE_LEFT_EQUALS)
            add(NOT_TRIANGLE_RIGHT_EQUALS)
            add(NOT_TRIANGLE_LEFT_EQUALS_SLANT)
            add(NOT_TRIANGLE_RIGHT_SLANT)
            add(BLACK_TRIANGLE_LEFT)
            add(BLACK_TRIANGLE_RIGHT)
            add(SUBSET)
            add(SUPERSET)
            add(SUBSET_EQUALS)
            add(SUPERSET_EQUALS)
            add(SUBSET_NOT_EQUALS)
            add(SUPERSET_NOT_EQUALS)
            add(SUBSET_NOT_EQUALS_VARIATION)
            add(SUPERSET_NOT_EQUALS_VARIATION)
            add(NOT_SUBSET_EQUALS)
            add(NOT_SUPERSET_EQUALS)
            add(SUBSET_EQUALSS)
            add(SUPERSET_EQUALSS)
            add(SUBSET_NOT_EQUALSS)
            add(SUPERSET_NOT_EQUALSS)
            add(NOT_SUBSET_EQUALSS)
            add(NOT_SUPERSET_EQUALSS)
            add(SUBSET_PLUS)
            add(SUBSET_PLUS_EQUALS)
            add(SUPERSET_PLUS)
            add(SUPERSET_PLUS_EQUALS)
            add(REVERSED_EPSILON)
            add(DOUBLE_SUBSET)
            add(DOUBLE_SUPERSET)
            add(SQUARE_SUBSET)
            add(SQUARE_SUPERSET)
            add(SQUARE_SUBSET_EQUALS)
            add(SQUARE_SUPERSET_EQUALS)
            add(IN_PLUS)
            add(REVERSED_IN_PLUS)
        }

        createCategory("Arrows") {

        }

        createCategory("Delimiters") {

        }

        createCategory("Greek") {

        }

        createCategory("Misc. math") {

        }

        createCategory("Text") {

        }

        createCategory("Misc. symbols") {

        }
    }

    /**
     * The list of all registered categories.
     * Also contains the ALL category.
     */
    val categoryList: List<SymbolCategory> = listOf(SymbolCategory.ALL) + categories.map { (category, _) -> category }

    /**
     * Flat map of all registered symbols, in order.
     */
    val symbolList: List<SymbolUiEntry> = categories.flatMap { it.value }

    /**
     * Get the operators that are in the given category.
     * The category [SymbolCategory.ALL] returns all available symbols.
     */
    operator fun get(category: SymbolCategory): List<SymbolUiEntry> = if (category == SymbolCategory.ALL) {
        symbolList
    }
    else categories[category] ?: emptyList()

    /**
     * Adds a UI entry for the given command to the entry list.
     * For the parameters see [CommandUiEntry].
     */
    private fun MutableList<SymbolUiEntry>.add(
            command: LatexCommand,
            latex: String? = null,
            fileName: String? = null,
            description: String? = null,
            image: String? = null
    ) = add(CommandUiEntry(command, latex, fileName, description, image))

    /**
     * Adds a new category to the map and initializes the symbols.
     */
    private fun MutableMap<SymbolCategory, List<SymbolUiEntry>>.createCategory(
            name: String,
            description: String = name,
            symbolInitializer: MutableList<SymbolUiEntry>.() -> Unit
    ) {
        val category = SymbolCategory(name, description)
        this[category] = ArrayList<SymbolUiEntry>().apply {
            symbolInitializer()
        }
    }
}