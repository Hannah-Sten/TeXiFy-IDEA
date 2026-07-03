package nl.hannahsten.texifyidea.inspections

import nl.hannahsten.texifyidea.TexifyBundle

internal fun localizedSuppressionFamilyName(keySuffix: String, defaultText: String, vararg params: Any): String =
    when (keySuffix) {
        "suppress.file" -> TexifyBundle.message("quickfix.suppress.file", *params)
        "suppress.environment" -> TexifyBundle.message("quickfix.suppress.environment", *params)
        "suppress.math.environment" -> TexifyBundle.message("quickfix.suppress.math.environment")
        "suppress.command" -> TexifyBundle.message("quickfix.suppress.command", *params)
        "suppress.group" -> TexifyBundle.message("quickfix.suppress.group")
        else -> defaultText
    }
