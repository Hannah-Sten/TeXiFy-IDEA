package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessageType
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.ERROR
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessageType.WARNING
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView

/**
 * Tests for log messages spanning multiple lines.
 * For messages over at most two lines, see [LatexMessageExtractorTest].
 */
class LatexOutputListenerTest : BasePlatformTestCase() {
    private val logTextLatexmk = """
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
        (/home/abby/Documents/texify-test/out/main.aux
        LaTeX Warning: Label `mylabel' multiply defined.
        )
        
        (/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty
        /home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:104: Package font
        enc Error: Encoding file `15enc.def' not found.
        (fontenc)                You might have misspelt the name of the encoding.
        /home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:105: Font T1/cmr/
        m/n/10=ecrm1000 at 10.0pt not loadable: Metric (TFM) file not found.
        )
        No file main.bbl.
        
        
        ./main.tex:5: LaTeX Error: Encoding scheme `15' unknown.
        
        ! LaTeX Error: Cannot determine size of graphic in figures/background-black-cat
        .jpg (no BoundingBox).

        
        LaTeX Warning: Citation 'DBLP.books.daglib.0076726' on page 1 undefined on inpu
        t line 7.
        
        (./math.tex

        ./math.tex:7: LaTeX Error: Environment align undefined.
        
        Overfull \hbox (252.50682pt too wide) in paragraph at lines 5--6
        [][]

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
        
        Loose \hbox (badness 0) in paragraph at lines 9--12
        \OT1/cmr/m/n/10 The badness of this line is 1000.

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

    private fun testLog(log: String, expectedMessages: Set<LatexLogMessage> = setOf(), expectedBibMessages: Set<BibtexLogMessage> = setOf()) {
        val project = myFixture.project
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project, latexMessageList, bibtexMessageList)
        val listener = LatexOutputListener(project, null, latexMessageList, bibtexMessageList, treeView)

        val input = log.split('\n')
        input.forEach { line ->
            listener.processNewText(line + "\n")
        }

        assertEquals(expectedMessages, latexMessageList.toSet())
        assertEquals(expectedBibMessages, bibtexMessageList.toSet())
    }

    fun testFullLog() {
        val expectedMessages = setOf(
                LatexLogMessage("Label `mylabel' multiply defined.", "/home/abby/Documents/texify-test/out/main.aux", -1, WARNING),
                LatexLogMessage("fontenc: Encoding file `15enc.def' not found. You might have misspelt the name of the encoding.", "/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty", 104, ERROR),
                LatexLogMessage("Font T1/cmr/m/n/10=ecrm1000 at 10.0pt not loadable: Metric (TFM) file not found.", "/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty", 105, ERROR),
                LatexLogMessage("No file main.bbl.", "./main.tex", -1, WARNING),
                LatexLogMessage("Encoding scheme `15' unknown.", "./main.tex", 5, ERROR),
                LatexLogMessage("Cannot determine size of graphic in figures/background-black-cat.jpg (no BoundingBox).", "./main.tex", -1, ERROR),
                LatexLogMessage("Citation 'DBLP.books.daglib.0076726' undefined", "./main.tex", 7, WARNING),
                LatexLogMessage("Environment align undefined.", "./math.tex", 7, ERROR),
                LatexLogMessage("Overfull \\hbox (252.50682pt too wide) in paragraph at lines 5--6", "./math.tex", 5, WARNING),
                LatexLogMessage("\\begin{document} ended by \\end{align}.", "./math.tex", 9, ERROR),
                LatexLogMessage("Reference `fig:bla' undefined", "./math.tex", 10, WARNING),
                LatexLogMessage("Environment align undefined.", "./main.tex", 9, ERROR),
                LatexLogMessage("Missing $ inserted.", "./main.tex", 10, ERROR),
                LatexLogMessage("\\begin{document} ended by \\end{align}.", "./main.tex", 11, ERROR),
                LatexLogMessage("Missing $ inserted.", "./main.tex", 11, ERROR),
                LatexLogMessage("Reference `test' undefined", "./lipsum.tex", 4, WARNING),
                LatexLogMessage("Undefined control sequence. \\bloop", "./nested/lipsum-one.tex", 9, ERROR),
                LatexLogMessage("Environment lstlisting undefined.", "./lipsum.tex", 11, ERROR),
                LatexLogMessage("\\begin{document} ended by \\end{lstlisting}.", "./lipsum.tex", 13, ERROR),
                LatexLogMessage("Loose \\hbox (badness 0) in paragraph at lines 9--12", "./main.tex", 9, WARNING),
                LatexLogMessage("There were undefined references.", "./main.tex", -1, WARNING),
                LatexLogMessage("Label(s) may have changed. Rerun to get cross-references right.", "./main.tex", -1, WARNING),
                LatexLogMessage("biblatex: Please (re)run Biber on the file: main and rerun LaTeX afterwards.", "./main.tex", -1, WARNING)
        )

        testLog(logTextLatexmk, expectedMessages)
    }

    fun `test You have requested, on line n, version d of m`() {
        val log = """
            
            LaTeX Warning: You have requested, on input line 5, version
                           `9999/99/99' of package test998,
                           but only version
                           `2020/04/08'
                           is available.
            
            [1{/home/thomas/texlive/2019/texmf-var/fonts/map/pdftex/updmap/pdftex.map}]               
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("You have requested, on input line 5, version `9999/99/99' of package test998, but only version `2020/04/08' is available.", null, 5, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test usepackage before documentclass`() {
        val log = """
            LaTeX2e <2019-10-01> patch level 3
            
            ./errors.tex:1: LaTeX Error: \usepackage before \documentclass.
            
            See the LaTeX manual or LaTeX Companion for explanation.
            Type  H <return>  for immediate help.
             ...            
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("\\usepackage before \\documentclass.", "./errors.tex", 1, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test fontenc encoding file not found`() {
        val log = """
                    
        (/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty
        /home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:104: Package font
        enc Error: Encoding file `15enc.def' not found.
        (fontenc)                You might have misspelt the name of the encoding.
        /home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:105: Font T1/cmr/
        m/n/10=ecrm1000 at 10.0pt not loadable: Metric (TFM) file not found.
        )
        No file main.bbl.
        
        
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("fontenc: Encoding file `15enc.def' not found. You might have misspelt the name of the encoding.", "/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty", 104, ERROR),
            LatexLogMessage("Font T1/cmr/m/n/10=ecrm1000 at 10.0pt not loadable: Metric (TFM) file not found.", "/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty", 105, ERROR),
            LatexLogMessage("No file main.bbl.", null, -1, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test biblatex warning`() {
        val log = """
        (./math.tex
        

        Package biblatex Warning: Please (re)run Biber on the file:
        (biblatex)                main
        (biblatex)                and rerun LaTeX afterwards.

         )
        (see the transcript file for additional information)</home/abby/texlive/2019/te
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("biblatex: Please (re)run Biber on the file: main and rerun LaTeX afterwards.", "./math.tex", -1, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test fontspec errors`() {
        val log = """
            ./errors.tex:10: Improper `at' size (0.0pt), replaced by 10pt.
            <to be read again> 
            relax 
            l.10 
            
               
            ...exmf-dist/tex/luatex/luaotfload/luaotfload-auxiliary.lua:702: attempt to ind
            ex a nil value (local 'fontdata')
            stack traceback:
                ...exmf-dist/tex/luatex/luaotfload/luaotfload-auxiliary.lua:702: in field 'get
            _math_dimension'
                .../texlive/2020/texmf-dist/tex/latex/fontspec/fontspec.lua:75: in field 'math
            fontdimen'
                [\directlua]:1: in main chunk.
            lua_now:e #1->__lua_now:n {#1}
                                          
            l.10   
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("Improper `at' size (0.0pt), replaced by 10pt.", "./errors.tex", 10, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test latexmk bibtex warning`() {
        val log = """
            Latexmk: applying rule 'bibtex bibtex-mwe'...
            For rule 'bibtex bibtex-mwe', running '&run_bibtex(  )' ...
            This is BibTeX, Version 0.99d (TeX Live 2020)
            The top-level auxiliary file: bibtex-mwe.aux
            The style file: plain.bst
            Database file #1: references.bib
            Warning--I'm ignoring knuth1990's extra "author" field
            --line 5 of file references.bib
            (There was 1 warning)
            Latexmk: All targets (/home/thomas/GitRepos/random-tex/src/bibtex-mwe.pdf) are up-to-date
            
            Process finished with exit code 0
        """.trimIndent()

        val expectedMessages = setOf(
            BibtexLogMessage("I'm ignoring knuth1990's extra \"author\" field", "references.bib", 5, BibtexLogMessageType.WARNING)
        )

        testLog(log, expectedBibMessages = expectedMessages)
    }

    fun `test datetime2 language module not installed`() {
        val log = """
            (/home/thomas/texlive/2018/texmf-dist/tex/latex/datetime2/datetime2.sty
            (/home/thomas/texlive/2018/texmf-dist/tex/latex/tracklang/tracklang.sty
            (/home/thomas/texlive/2018/texmf-dist/tex/generic/tracklang/tracklang.tex))
            
            Package datetime2 Warning: Date-Time Language Module `british' not installed on
             input line 1913.
            
            ) (/home/thomas/texmf/tex/latex/zref/zref-savepos.sty
            (/home/thomas/texmf/tex/latex/zref/zref-base.sty
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("datetime2: Date-Time Language Module `british' not installed", "/home/thomas/texlive/2018/texmf-dist/tex/latex/datetime2/datetime2.sty", 1913, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test overfull hbox`() {
        val log = """
            (./development-workflow.tex
            


            [13]
            Overfull \hbox (2.5471pt too wide) in paragraph at lines 122--126
            \T1/phv/m/n/10 (-20) databricks De-vOps repo, then in that di-rec-tory run \T1/
            cmtt/m/n/10 databricks workspace export_dir / notebooks/
            
            LaTeX Warning: Reference `sec:to-copy-data-from-tests-sql-servers-to-the-blabla
            blabla-blob-storage-using-the-data-factory' on page 14 undefined on input line 
            134.

            
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("Overfull \\hbox (2.5471pt too wide) in paragraph at lines 122--126", "./development-workflow.tex", 122, WARNING),
            LatexLogMessage("Reference `sec:to-copy-data-from-tests-sql-servers-to-the-blablablabla-blob-storage-using-the-data-factory' on page 14 undefined", "./development-workflow.tex", 134, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test Font shape undefined`() {
        val log = """
            (/home/thomas/GitRepos/thisisatestfile-this-isatestfile-/out/UMD00000000000000_
            test-file-test-file-t.toc
            
            LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
            (Font)              using `T1/phv/m/it' instead on input line 43.

        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("Font shape `T1/phv/m/scit' undefined, using `T1/phv/m/it' instead", "/home/thomas/GitRepos/thisisatestfile-this-isatestfile-/out/UMD00000000000000_test-file-test-file-t.toc", 43, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test Encoding file not found`() {
        val log = """
            (/home/thomas/texlive/2018/texmf-dist/tex/latex/base/fontenc.sty
            
            /home/thomas/texlive/2018/texmf-dist/tex/latex/base/fontenc.sty:111: Package fo
            ntenc Error: Encoding file `ly1enc.def' not found.
            (fontenc)                You might have misspelt the name of the encoding.
            
            See the fontenc package documentation for explanation.
            Type  H <return>  for immediate help.
             ...                                              
                                                              
            l.111 \ProcessOptions*

        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("fontenc: Encoding file `ly1enc.def' not found. You might have misspelt the name of the encoding.", "/home/thomas/texlive/2018/texmf-dist/tex/latex/base/fontenc.sty", 111, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test No file`() {
        val log = """
            (./main.tex
            
            (/home/thomas/GitRepos/random-tex/out/main.aux
            (/home/thomas/GitRepos/random-tex/out/notexists.aux))
            No file notexists.tex.
            (/home/thomas/GitRepos/random-tex/out/main.aux
            (/home/thomas/GitRepos/random-tex/out/notexists.aux)) )
            No pages of output.
            Transcript written on /home/thomas/GitRepos/random-tex/out/main.log.
            
            Process finished with exit code 0
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("No file notexists.tex.", "./main.tex", -1, WARNING),
            LatexLogMessage("No pages of output.", null, -1, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test directlua unexpected symbol`() {
        val log = """
            (./main.tex
            
            (/home/thomas/texlive/2018/texmf-dist/tex/latex/base/ts1cmr.fd)[\directlua]:1: 
            unexpected symbol near '3'.
            l.4     \directlua{3 = x}

        """.trimIndent()

        val expectedMessages = setOf(
            // todo possible improvement: detecting line 4
            LatexLogMessage("unexpected symbol near '3'.", "./main.tex", -1, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test FiXme notes`() {
        val log = """
            (./main.tex
            

            FiXme Warning: 'this is a warning' on input line 7.
            
            
            FiXme Error: 'this is an error' on input line 8.
            
            
            FiXme Fatal Error: 'this kills me' on input line 9.
            
            
            FiXme Summary: Number of notes: 1,
            (FiXme)        Number of warnings: 1,
            (FiXme)        Number of errors: 1,
            (FiXme)        Number of fatal errors: 1,
            (FiXme)        Total: 4.
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("FiXme: this is a warning", "./main.tex", 7, WARNING),
            LatexLogMessage("FiXme: this is an error", "./main.tex", 8, ERROR),
            LatexLogMessage("FiXme: this kills me", "./main.tex", 9, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test Unknown option babel`() {
        val log = """
            
            /home/thomas/texlive/2018/texmf-dist/tex/generic/babel/babel.sty:1036: Package 
            babel Error: Unknown option `brazil'. Either you misspelled it
            (babel)                or the language definition file brazil.ldf was not found
            .
            
            See the babel package documentation for explanation.
            Type  H <return>  for immediate help.
             ...                                              
                                                              
            l.1036 \ProcessOptions*
                     
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("babel: Unknown option `brazil'. Either you misspelled it or the language definition file brazil.ldf was not found.", "/home/thomas/texlive/2018/texmf-dist/tex/generic/babel/babel.sty", 1036, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test Fatal error occurred`() {
        val log = """
            <inserted text> 
            \fi 
            <*> main.tex
                     
            ! Emergency stop.
            <*> main.tex
                     
             549 words of node memory still in use:
               4 hlist, 1 vlist, 2 rule, 1 mark, 1 local_par, 1 dir, 4 glue, 3 kern, 3 glyp
            h, 7 attribute, 62 glue_spec, 7 attribute_list, 1 temp, 1 if_stack nodes
               avail lists: 2:4,3:2,4:2,5:2,7:2
            !  ==> Fatal error occurred, no output PDF file produced!
            Transcript written on main.log.
            
            Process finished with exit code 1
                     
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("Emergency stop.", null, -1, ERROR),
            LatexLogMessage("==> Fatal error occurred, no output PDF file produced!", null, -1, ERROR)
        )

        testLog(log, expectedMessages)
    }

    fun `test pdfTeX warning`() {
        val log = """
            (./main.tex
            
            (/home/thomas/GitRepos/random-tex/out/main.out) [1{/home/thomas/texlive/2020/te
            xmf-var/fonts/map/pdftex/updmap/pdftex.map}]
            (/home/thomas/GitRepos/random-tex/out/main.aux) )pdfTeX warning (dest): name{su
            mmary} has been referenced but does not exist, replaced by a fixed one
            
            </home/thomas/texlive/2020/texmf-dist/fonts/type1/public/amsfonts/cm/cmr10.pfb>
            Output written on /home/thomas/GitRepos/random-tex/out/main.pdf (1 page, 12113 
            bytes).
                     
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("name{summary} has been referenced but does not exist, replaced by a fixed one", null, -1, WARNING)
        )

        testLog(log, expectedMessages)
    }

    fun `test babel unknown language`() {
        val log = """
            s
            (C:/Users/thoscho/GitRepos/test/latex-templates/auxil\test.aux
            
            Package babel Warning: Unknown language `english'. Very likely you
            (babel)                requested it in a previous run. Expect some
            (babel)                wrong results in this run, which should vanish
            (babel)                in the next one. Reported on input line 21.
            
            ) ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\t1phv.f
            d"
            
            LaTeX Warning: Font shape declaration has incorrect series value `mc'.
                           It should not contain an `m'! Please correct it.
                           Found on input line 20.
            
            
            ) ABD: EveryShipout initializing macros
            *geometry* driver: auto-detecting
            *geometry* detected driver: pdftex

                     
        """.trimIndent()

        val expectedMessages = setOf(
            LatexLogMessage("babel: Unknown language `english'. Very likely you requested it in a previous run. Expect some wrong results in this run, which should vanish in the next one.", "C:/Users/thoscho/GitRepos/test/latex-templates/auxil\\test.aux", 21, WARNING),
            LatexLogMessage("Font shape declaration has incorrect series value `mc'. It should not contain an `m'! Please correct it.", "C:\\Users\\thoscho\\AppData\\Local\\Programs\\MiKTeX 2.9\\tex/latex/psnfss\\t1phv.fd", 20, WARNING)
        )

        testLog(log, expectedMessages)
    }
}