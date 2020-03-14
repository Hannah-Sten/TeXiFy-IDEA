package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView

class LatexOutputListenerTest : BasePlatformTestCase() {
    val logTextLatexmk = """
        latexmk -pdf -file-line-error -interaction=nonstopmode -synctex=1 -output-format=pdf -output-directory=/home/abby/Documents/texify-test/out main.tex
        Latexmk: applying rule 'pdflatex'...
        This is pdfTeX, Version 3.14159265-2.6-1.40.20 (TeX Live 2019) (preloaded format=pdflatex)
         restricted \write18 enabled.
        entering extended mode
        (./main.tex
        LaTeX2e <2019-10-01> patch level 3
        (/home/abby/texlive/2019/texmf-dist/tex/latex/base/article.cls
        Document Class: article 2019/10/25 v1.4k Standard LaTeX document class
        (/home/abby/texlive/2019/texmf-dist/tex/latex/base/size10.clo)Latexmk: This is Latexmk, John Collins, 18 June 2019, version: 4.65.
        Rule 'pdflatex': The following rules & subrules became out-of-date:
              'pdflatex'
        ------------
        Run number 1 of rule 'pdflatex'
        ------------
        ------------
        Running 'pdflatex  -file-line-error -interaction=nonstopmode -synctex=1 -output-format=pdf -recorder -output-directory="/home/abby/Documents/texify-test/out"  "main.tex"'
        ------------
        )
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/biblatex.sty
        (/home/abby/texlive/2019/texmf-dist/tex/latex/pdftexcmds/pdftexcmds.sty
        (/home/abby/texlive/2019/texmf-dist/tex/generic/infwarerr/infwarerr.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/generic/iftex/iftex.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/generic/oberdiek/ltxcmds.sty))
        (/home/abby/texlive/2019/texmf-dist/tex/latex/etoolbox/etoolbox.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/graphics/keyval.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/kvoptions/kvoptions.sty
        (/home/abby/texlive/2019/texmf-dist/tex/generic/oberdiek/kvsetkeys.sty
        (/home/abby/texlive/2019/texmf-dist/tex/generic/oberdiek/etexcmds.sty
        (/home/abby/texlive/2019/texmf-dist/tex/generic/iftex/ifluatex.sty))))
        (/home/abby/texlive/2019/texmf-dist/tex/latex/logreq/logreq.sty
        (/home/abby/texlive/2019/texmf-dist/tex/latex/logreq/logreq.def))
        (/home/abby/texlive/2019/texmf-dist/tex/latex/base/ifthen.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/url/url.sty)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/blx-dm.def)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/blx-compat.def)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/biblatex.def)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/bbx/numeric.bbx
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/bbx/standard.bbx))
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/cbx/numeric.cbx)
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/biblatex.cfg))
        (/home/abby/texlive/2019/texmf-dist/tex/latex/biblatex/lbx/english.lbx)
        (/home/abby/Documents/texify-test/out/main.aux)
        No file main.bbl.
        
        LaTeX Warning: Citation 'DBLP.books.daglib.0076726' on page 1 undefined on inpu
        t line 7.
        
        (./math.tex

        ./math.tex:7: LaTeX Error: Environment align undefined.

        Latexmk: Non-existent bbl file '/home/abby/Documents/texify-test/out/main.bbl'
         No file main.bbl.
        Latexmk: References changed.
        Latexmk: Log file says output to '/home/abby/Documents/texify-test/out/main.pdf'
        Latexmk: Log file says output to '/home/abby/Documents/texify-test/out/main.pdf'
        Latexmk: List of undefined refs and citations:
          Citation 'DBLP.books.daglib.0076726' on page 1 undefined on input line 7
          Reference `fig:bla' on page 1 undefined on input line 10
          Reference `test' on page 1 undefined on input line 4
        Latexmk: Summary of warnings from last run of (pdf)latex:
          Latex failed to resolve 2 reference(s)
          Latex failed to resolve 1 citation(s)
        Collected error summary (may duplicate other messages):
          pdflatex: Command for 'pdflatex' gave return code 1
              Refer to '/home/abby/Documents/texify-test/out/main.log' for details
        Latexmk: Use the -f option to force complete processing,
         unless error was exceeding maximum runs, or warnings treated as errors.
        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.7 \begin{align}
                         

        ./math.tex:9: LaTeX Error: \begin{document} ended by \end{align}.

        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.9 \end{align}
                       

        LaTeX Warning: Reference `fig:bla' on page 1 undefined on input line 10.

        )

        ./main.tex:9: LaTeX Error: Environment align undefined.

        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.9     \begin{align}
                             
        ./main.tex:10: Missing ${'$'} inserted.
        <inserted text> 
                        ${'$'}
        l.10         \pi
                        

        ./main.tex:11: LaTeX Error: \begin{document} ended by \end{align}.

        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.11     \end{align}
                            
        ./main.tex:11: Missing ${'$'} inserted.
        <inserted text> 
                        ${'$'}
        l.11     \end{align}
                            
        (./lipsum.tex

        LaTeX Warning: Reference `test' on page 1 undefined on input line 4.

        (./nested/lipsum-one.tex
        ./nested/lipsum-one.tex:9: Undefined control sequence.
        l.9 \bloop
                  
        ) [1{/home/abby/texlive/2019/texmf-var/fonts/map/pdftex/updmap/pdftex.map}]

        ./lipsum.tex:11: LaTeX Error: Environment lstlisting undefined.

        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.11 \begin{lstlisting}
                               [label={lst:lstlisting}]

        ./lipsum.tex:13: LaTeX Error: \begin{document} ended by \end{lstlisting}.

        See the LaTeX manual or LaTeX Companion for explanation.
        Type  H <return>  for immediate help.
         ...                                              
                                                          
        l.13 \end{lstlisting}
                             
        ) [2] (/home/abby/Documents/texify-test/out/main.aux)

        LaTeX Warning: There were undefined references.


        LaTeX Warning: Label(s) may have changed. Rerun to get cross-references right.


        Package biblatex Warning: Please (re)run Biber on the file:
        (biblatex)                main
        (biblatex)                and rerun LaTeX afterwards.

         )
        (see the transcript file for additional information)</home/abby/texlive/2019/te
        xmf-dist/fonts/type1/public/amsfonts/cm/cmbx10.pfb></home/abby/texlive/2019/tex
        mf-dist/fonts/type1/public/amsfonts/cm/cmbx12.pfb></home/abby/texlive/2019/texm
        f-dist/fonts/type1/public/amsfonts/cm/cmex10.pfb></home/abby/texlive/2019/texmf
        -dist/fonts/type1/public/amsfonts/cm/cmmi10.pfb></home/abby/texlive/2019/texmf-
        dist/fonts/type1/public/amsfonts/cm/cmmi7.pfb></home/abby/texlive/2019/texmf-di
        st/fonts/type1/public/amsfonts/cm/cmr10.pfb></home/abby/texlive/2019/texmf-dist
        /fonts/type1/public/amsfonts/cm/cmr7.pfb>
        Output written on /home/abby/Documents/texify-test/out/main.pdf (2 pages, 73937
         bytes).
        SyncTeX written on /home/abby/Documents/texify-test/out/main.synctex.gz.
        Transcript written on /home/abby/Documents/texify-test/out/main.log.
        === TeX engine is 'pdfTeX'
        Latexmk: Errors, so I did not complete making targets

        Process finished with exit code 12
    """.trimIndent()

    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    fun testMain() {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val listModel = mutableListOf<LatexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project)
        val listener = LatexOutputListener(project, mainFile, listModel, treeView)

        val input = logTextLatexmk.split('\n')
        input.forEach { listener.processNewText(it) }

        val expectedMessages = setOf(
                LatexLogMessage("Citation 'DBLP.books.daglib.0076726' on page 1 undefined", "main.tex", 6, WARNING),
                LatexLogMessage("Environment align undefined.", "math.tex", 7, ERROR),
                LatexLogMessage("\\begin{document} ended by \\end{align}", "math.tex", 9, ERROR),
                LatexLogMessage("Reference `fig:bla' on page 1 undefined", "math.tex", 10, WARNING),
                LatexLogMessage("Environment align undefined.", "main.tex", 9, ERROR),
                LatexLogMessage("Missing $ inserted.", "main.tex", 10, WARNING),
                LatexLogMessage("\\begin{document} ended by \\end{align}.", "main.tex", 11, ERROR),
                LatexLogMessage("Missing $ inserted.", "main.tex", 11, ERROR),
                LatexLogMessage("Reference `test' on page 1 undefined", "lipsum.tex", 4, WARNING),
                LatexLogMessage("Environment lstlisting undefined.", "lipsum.tex", 11, ERROR),
                LatexLogMessage("\\begin{document} ended by \\end{lstlisting}.", "lipsum.tex", 13, ERROR),
                LatexLogMessage("Undefined control sequence.", "lipsum-one.tex", 9, ERROR),
                LatexLogMessage("There were undefined references.", "main.tex", null, WARNING),
                LatexLogMessage("Label(s) may have changed. Rerun to get cross-references right.", "main.tex", null, WARNING)
        )

        assertEquals(expectedMessages, listModel.toSet())
    }
}