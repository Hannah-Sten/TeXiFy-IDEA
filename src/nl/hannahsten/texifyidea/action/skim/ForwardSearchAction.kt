package nl.hannahsten.texifyidea.action.skim

import nl.hannahsten.texifyidea.action.ForwardSearchActionBase
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

/**
 * Starts a forward search action in Skim.
 *
 * Note: this is only available on MacOS.
 *
 * @author Stephan Sundermann
 */
open class ForwardSearchAction : ForwardSearchActionBase(PdfViewer.SKIM)
