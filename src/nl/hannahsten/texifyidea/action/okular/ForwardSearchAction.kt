package nl.hannahsten.texifyidea.action.okular

import nl.hannahsten.texifyidea.action.ForwardSearchActionBase
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer

/**
 * Starts a forward search action in Okular.
 *
 * Note: this is only available on Linux.
 *
 * @author Abby Berkers
 */
open class ForwardSearchAction : ForwardSearchActionBase(InternalPdfViewer.OKULAR)