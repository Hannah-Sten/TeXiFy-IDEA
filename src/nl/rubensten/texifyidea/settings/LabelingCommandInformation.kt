package nl.rubensten.texifyidea.settings

data class LabelingCommandInformation(var commandName: String, var position: Int, var labelPrevCmd: Boolean) {

    companion object {
    
        /**
         * Needed to get bck the object from the string in the xml file
         */
        @JvmStatic fun fromString(string: CharSequence): LabelingCommandInformation {
            val parts = string.split(";")
            if (parts.size != 3) {
                throw IllegalArgumentException("String must contain exactly three parts")
            }
            val position = parts[1].toIntOrNull() ?: error("Second part must be type Int")
            val labelPrevCmd = parts[2].toBoolean()
            return LabelingCommandInformation(parts.first(), position, labelPrevCmd)
        }
    }

    /**
     * Needed to save the information to the xml file
     */
    fun toSerializableString() = "$commandName;$position;$labelPrevCmd"
}
