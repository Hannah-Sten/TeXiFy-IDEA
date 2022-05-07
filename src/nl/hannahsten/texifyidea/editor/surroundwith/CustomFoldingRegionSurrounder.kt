package nl.hannahsten.texifyidea.editor.surroundwith

class CustomFoldingRegionSurrounder : LatexSurrounder("%! region\n", "\n%! endregion", displayBefore = "region", displayAfter = "endregion")