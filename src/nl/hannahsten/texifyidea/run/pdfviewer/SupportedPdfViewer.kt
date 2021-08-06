package nl.hannahsten.texifyidea.run.pdfviewer

import nl.hannahsten.texifyidea.run.executable.SupportedExecutable

/**
 * A PDF viewer for which we support basic behaviour like forward search, which is implemented by both
 * [ExternalPdfViewer] and [InternalPdfViewer].
 * This is conform [nl.hannahsten.texifyidea.run.compiler.SupportedCompiler] to use in [nl.hannahsten.texifyidea.run.ui.compiler.ExecutableEditor].
 */
interface SupportedPdfViewer : SupportedExecutable, PdfViewer