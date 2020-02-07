package nl.hannahsten.texifyidea.lang

class RequiredPicturePathArgument(name : String, vararg extension: String) : RequiredFileArgument(name, *extension)