package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.ui.LatexCompileMessageTreeView

class LatexQuickRunLogParserTest : BasePlatformTestCase() {


    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    fun testFileStack() {
        val header = """
pdflatex -file-line-error -interaction=nonstopmode -synctex=1 -output-format=pdf -output-directory=C:/Users/thoscho/GitRepos/prodrive/prodrive-data-science-platform-dsd/out -aux-directory=C:/Users/thoscho/GitRepos/prodrive/prodrive-data-science-platform-dsd/auxil -include-directory=C:/Users/thoscho/GitRepos/prodrive/prodrive-data-science-platform-dsd/src UMD6001197392Rxx_data-science-platform.tex
This is pdfTeX, Version 3.14159265-2.6-1.40.21 
entering extended mode
(UMD6001197392Rxx_data-science-platform.tex
LaTeX2e <2020-02-02> patch level 5
L3 programming layer <2020-03-06> (prodrive/prodrive.cls
Document Class: prodrive/prodrive 2019/12/19 Prodrive class
""".trimIndent()
        val styFiles = """
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/utilities\pg
ffor.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/utilities\pg
frcs.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfutil-common.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfutil-common-lists.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfutil-latex.def"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/ms\everyshi.sty"
))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfrcs.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf\pgf.revisi
on.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/utilities\pg
fkeys.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfkeys.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfkeysfiltered.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/math\pgfmath
.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
th.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thcalc.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thutil.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thparser.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.basic.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.trigonometric.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.random.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.comparison.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.base.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.round.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.misc.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfunctions.integerarithmetics.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
thfloat.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgffor.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
th.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\report.cls"
Document Class: report 2019/12/20 v1.4l Standard LaTeX document class
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\size10.clo"
)) 
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/geometry\geometr
y.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics\keyval.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/iftex\ifvtex.s
ty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/iftex\iftex.st
y"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/geometry\geometr
y.cfg")) (prodrive/package/pdlanguage.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/babel\babel.st
y"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/babel\switch.d
ef")""".trimIndent()
        val moreStyFiles = """
*************************************
* Local config file bblopts.cfg used
*

("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/arabi\bblopts.cf
g")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/babel-english\br
itish.ldf"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/babel-english\en
glish.ldf"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/babel\babel.de
f"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/babel\txtbabel
.def")))))) (prodrive/package/pdtitlepage.sty (prodrive/package/pdattribute.sty

("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime\datetim
e.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/etoolbox\etoolbo
x.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fmtcount\fmtcoun
t.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\ifthen.sty"
)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/xkeyval\xkeyval.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/xkeyval\xkeyva
l.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/xkeyval\xkvuti
ls.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fmtcount\fcprefi
x.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fmtcount\fcnumpa
rser.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsmath\amsgen.s
ty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fmtcount\fc-engl
ish.def")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fmtcount\fc-brit
ish.def"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime\datetim
e-defaults.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime\dt-brit
ish.def")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/fancyhdr\fancyhd
r.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics\graphic
x.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics\graphic
s.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics\trig.st
y")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics-cfg\gra
phics.cfg")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics-def\pdf
tex.def")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/frontendlaye
r\tikz.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/basiclayer\p
gf.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/basiclayer\p
gfcore.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/systemlayer\
pgfsys.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgfsys.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
pgfkeys.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgf.cfg")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgfsys-pdftex.def"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgfsys-common-pdf.def")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgfsyssoftpath.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/systemlaye
r\pgfsysprotocol.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/xcolor\xcolor.st
y"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics-cfg\col
or.cfg"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcore.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
th.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfin
t.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorepoints.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorepathconstruct.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorepathusage.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorescopes.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoregraphicstate.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoretransformations.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorequick.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoreobjects.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorepathprocessing.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorearrows.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoreshade.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoreimage.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoreexternal.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorelayers.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcoretransparency.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorepatterns.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/basiclayer
\pgfcorerdf.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/modules\pg
fmoduleshapes.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/modules\pg
fmoduleplot.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/compatibilit
y\pgfcomp-version-0-65.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/compatibilit
y\pgfcomp-version-1-18.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz\tikz.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries\
pgflibraryplothandlers.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/modules\pg
fmodulematrix.code.tex")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibrarytopaths.code.tex"))))
(prodrive/package/pdheader.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/lastpage\lastpag
e.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/lineno\lineno.st
y"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/imakeidx\imakeid
x.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/iftex\ifxetex.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/iftex\ifluatex
.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tools\multicol.s
ty"))  (prodrive/package/pdappendix.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/titlesec\titlese
c.sty")) (prodrive/package/pdbibliography.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/l3packages/xpars
e\xparse.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/l3kernel\expl3.s
ty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/l3backend\l3back
end-pdfmode.def")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex\biblate
x.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pdftexcmds\pdfte
xcmds.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/infwarerr\infw
arerr.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/ltxcmds\ltxcmd
s.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/kvoptions\kvopti
ons.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/kvsetkeys\kvse
tkeys.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/logreq\logreq.st
y"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/logreq\logreq.de
f")) ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/url\url.sty
")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex\blx-dm.
def") 
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex\blx-com
pat.def")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex\biblate
x.def") (prodrive/biblatex/pd-biblatex-style.bbx
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex/bbx\sta
ndard.bbx")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex/bbx\num
eric.bbx"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex/cbx\num
eric.cbx")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex\biblate
x.cfg"))) 
(prodrive/package/pddistributionlist.sty (prodrive/package/pdtabular.sty
(prodrive/package/pdtable.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/booktabs\booktab
s.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/caption\caption.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/caption\caption3
.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tools\longtable.
sty")))) (prodrive/package/pddocumenthistory.sty
(prodrive/package/pdtabularx.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tools\tabularx.s
ty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tools\array.sty"
))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/environ\environ.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/trimspaces\trims
paces.sty")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/xifthen\xifthen.
sty"


("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/ifmtarg\ifmtarg.
sty"))) (prodrive/package/pdfigure.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/float\float.sty"
)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.geometric.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.geometric.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.misc.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.misc.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.symbols.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.symbols.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.arrows.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.arrows.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.callouts.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.callouts.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryshapes.multipart.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries/
shapes\pgflibraryshapes.multipart.code.tex")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibrarydecorations.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/modules\pg
fmoduledecorations.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibraryarrows.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries\
pgflibraryarrows.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibrarychains.code.tex"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibrarypositioning.code.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/frontendla
yer/tikz/libraries\tikzlibrarycalc.code.tex")) (prodrive/package/pdfunction.sty

("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\listing
s.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstmisc
.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\listing
s.cfg"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/colortbl\colortb
l.sty")) (prodrive/package/pdfont.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\fontenc.sty
")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\helvet.st
y")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/upquote\upquote.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\textcomp.st
y"))) (prodrive/package/pdheading.sty) (prodrive/package/pdissue.sty)
(prodrive/package/pdlandscape.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pdflscape\pdflsc
ape.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/graphics\lscape.
sty"))) (prodrive/package/pdlinenumber.sty) (prodrive/package/pdlist.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/enumitem\enumite
m.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsfonts\amssymb
.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsfonts\amsfont
s.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/mnsymbol\MnSymbo
l.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsmath\amsmath.
sty"
For additional information on amsmath, use the `?' option.

("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsmath\amstext.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsmath\amsbsy.s
ty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsmath\amsopn.s
ty")))) (prodrive/package/pdlisting.sty) (prodrive/package/pdlongtable.sty
(prodrive/package/pdtool.sty)) (prodrive/package/pdmarking.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/soul\soul.sty"))
 (prodrive/package/pdreference.sty (prodrive/package/pdtext.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/ulem\ulem.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\microt
ype.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\microt
ype-pdftex.def")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\microt
ype.cfg")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pdfcomment\pdfco
mment.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime2\dateti
me2.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tracklang\trackl
ang.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/tracklang\trac
klang.tex"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime2-englis
h\datetime2-en-GB.ldf"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/datetime2-englis
h\datetime2-english-base.ldf")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/zref\zref-savepo
s.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/zref\zref-base.s
ty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/kvdefinekeys\k
vdefinekeys.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/etexcmds\etexc
mds.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/auxhook\auxhook.
sty")))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/refcount\refcoun
t.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/marginnote\margi
nnote.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/iftex\ifpdf.st
y")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/soulpos\soulpos.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hyperref\hyperre
f.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pdfescape\pdfe
scape.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hycolor\hycolor.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/letltxmacro\letl
txmacro.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hyperref\pd1enc.
def")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/intcalc\intcal
c.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hyperref\puenc.d
ef")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/stringenc\stri
ngenc.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/bitset\bitset.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/bigintcalc\big
intcalc.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/atbegshi\atbeg
shi.sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hyperref\hpdftex
.def"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/atveryend\atvery
end.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/rerunfilecheck\r
erunfilecheck.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/uniquecounter\
uniquecounter.sty"))))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/cleveref\clevere
f.sty")) (prodrive/package/pdreq.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/marvosym\marvosy
m.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hanging\hanging.
sty")) (prodrive/package/pdtest.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/xstring\xstring.
sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/xstring\xstrin
g.tex"))) (prodrive/package/pdtextlabel.sty) (prodrive/package/pdtoc.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/silence\silence.
sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/tocloft\tocloft.
sty"))) (prodrive/package/pdhwqrd.sty
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/framed\framed.st
y")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/capt-of\capt-of.
sty"))
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amscls\amsthm.st
y")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/libraries\
pgflibraryarrows.meta.code.tex") (contents/links.tex)
""".trimIndent()
        val startWarnings = """
Package biblatex Warning: 'babel/polyglossia' detected but 'csquotes' missing.
(biblatex)                Loading 'csquotes' recommended.


Package biblatex Warning: Conflicting options.
(biblatex)                'date=iso' requires 'seconds=true'.
(biblatex)                Setting 'seconds=true' on input line 54.


(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.aux)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\t1phv.fd"

LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 20.


LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 23.


LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 26.


LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 46.

) ABD: EveryShipout initializing macros
*geometry* driver: auto-detecting
*geometry* detected driver: pdftex
""".trimIndent()
        val evenMoreStyFiles = """
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/context/base/mkii\supp
-pdf.mkii"
[Loading MPS to PDF converter (version 2006.09.02).]
)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/epstopdf-pkg\eps
topdf-base.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex/lbx\bri
tish.lbx"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/biblatex/lbx\eng
lish.lbx"))
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.bbl)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/caption\ltcaptio
n.sty")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/hyperref\nameref
.sty"
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/gettitlestring
\gettitlestring.sty"))
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.out)
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.out)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\mt-cmr
.cfg")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsfonts\umsa.fd
")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\mt-msa
.cfg")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/amsfonts\umsb.fd
")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/microtype\mt-msb
.cfg")
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/stringenc\se-p
dfdoc.def") [1{C:/Users/thoscho/AppData/Local/MiKTeX/2.9/pdftex/config/pdftex.m
ap} <./prodrive/fig/prodrive_logo.pdf>]""".trimIndent()
        val rest = """
Overfull \hbox (15.0pt too wide) in paragraph at lines 63--64
[][] 

Overfull \hbox (15.0pt too wide) in paragraph at lines 70--71
[][] 
[2]
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\t1cmtt.fd")

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []

Overfull \hbox (2.12653pt too wide) in paragraph at lines 72--72
[] []
[3]
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.toc

LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 27.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 45.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 47.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 59.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 61.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 75.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 77.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 79.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 81.


LaTeX Font Warning: Font shape `T1/phv/m/scit' undefined
(Font)              using `T1/phv/m/it' instead on input line 83.

) [4]
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.lof) [5]
Chapter 1.
(contents/introduction.tex) [6]
Chapter 2.
(contents/naming-conventions.tex) [7]
Chapter 3.
(contents/definitions.tex [8]) [9]
Chapter 4.
(contents/data-flow-overview.tex) [10] [11 <./figures/etl-process.pdf>]
Chapter 5.
""".trimIndent()
        val one = """
(development-workflow.tex
Overfull \hbox (37.83125pt too wide) in paragraph at lines 16--18
\T1/cmtt/m/n/10 ProdriveAdvancedAnalytics / prodrive-[]data-[]science-[]platfor
m / _release ? _a = releases & definitionId =

Overfull \hbox (44.47421pt too wide) in paragraph at lines 32--33
[]\T1/phv/m/n/10 (-20) Our git re-pos-it-or-ies are hos-ted at Azure De-vOps, a
t [][]${'$'}\T1/cmtt/m/n/10 https : / / dev . azure . com / ProdriveAdvancedAnalytic
s /

Overfull \hbox (121.35228pt too wide) in paragraph at lines 32--33
\T1/cmtt/m/n/10 prodrive-[]data-[]science-[]platform / _git / pt-[]pdsp-[]dataf
actory${'$'}[][] \T1/phv/m/n/10 (-20) and [][]${'$'}\T1/cmtt/m/n/10 https : / / dev . azu
re . com / ProdriveAdvancedAnalytics /
[12] (contents/working-in-databricks.tex
Overfull \hbox (19.82806pt too wide) in paragraph at lines 3--5
\T1/phv/m/n/10 (-20) Git: Not linked, click Link, provide the De-vOps url [][]${'$'}
\T1/cmtt/m/n/10 https : / / dev . azure . com / ProdriveAdvancedAnalytics /
[13]
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstlang
1.sty")
Overfull \hbox (9.10867pt too wide) in paragraph at lines 35--52
\T1/phv/m/n/10 (-20) We use the re-Struc-tured-Text doc-string style, see [][]${'$'}
\T1/cmtt/m/n/10 https : / / www . python . org / dev / peps / pep-[]0287/${'$'}[][]\
T1/phv/m/n/10 (-20) ,
[14])) [15]
Chapter 6.
(data-factory-and-databricks.tex
Overfull \hbox (9.04805pt too wide) in paragraph at lines 26--27
[]\T1/phv/m/n/10 (-20) Choose a name like [][]\T1/cmtt/m/n/10 servername_databa
sename []\T1/phv/m/n/10 (-20) (e.g. [][]\T1/cmtt/m/n/10 prinsdb02_Area[]\T1/phv
/m/n/10 (-20) ), in which [][]\T1/cmtt/m/n/10 servername

Overfull \hbox (12.36533pt too wide) in paragraph at lines 29--30
[]\T1/phv/m/n/10 (-20) Choose Win-dows Au-then-tic-a-tion, provide the user nam
e au-qua-pdsp-datafactory@Prodrive.nl,

Overfull \hbox (18.44496pt too wide) in paragraph at lines 29--30
\T1/phv/m/n/10 (-20) se-lect Azure Key Vault, se-lect the linked ser-vice and a
s secret name [][]\T1/cmtt/m/n/10 au-qua-pdsp-datafactory[]\T1/phv/m/n/10 (-20)
 . 
""".trimIndent()
        val two = """
Overfull \hbox (24.80043pt too wide) in paragraph at lines 32--36
[][]\T1/cmtt/m/n/10 adf_publish []\T1/phv/m/n/10 (-20) branch, or guessed from 
json of the re-source (e.g. [][]\T1/cmtt/m/n/10 prinsdb02_Area_connectionString
[]\T1/phv/m/n/10 (-20) ).
[16]
Overfull \hbox (28.26276pt too wide) in paragraph at lines 69--72
\T1/phv/m/n/10 (-20) for re-mov-ing un-der-scores, e.g. [][]\T1/cmtt/m/n/10 Man
ufacturingHistory_ReadOnly []\T1/phv/m/n/10 (-20) can be [][]\T1/cmtt/m/n/10 Ma
nufacturingHistory
[17]
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/base\ts1cmtt.fd"
)
("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\ts1phv.fd
"

LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 17.


LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 20.


LaTeX Warning: Font shape declaration has incorrect series value `mc'.
               It should not contain an `m'! Please correct it.
               Found on input line 34.
""".trimIndent()
        val three = """
) [18]) [19]
Chapter 7.
(contents/power-bi.tex
Overfull \hbox (38.62149pt too wide) in paragraph at lines 27--30
[] []\T1/phv/b/n/10 De-ploy-ment[] \T1/phv/m/n/10 (-20) A Share-Point list has 
been cre-ated for Power BI de-ploy-ments: [][]${'$'}\T1/cmtt/m/n/10 https : / / prod
rivetechnologies .

Overfull \hbox (4.5765pt too wide) in paragraph at lines 27--30
\T1/cmtt/m/n/10 sharepoint . com / sites / AdvancedAnalytics504 / Lists / Deplo
yment % 20Power % 20BI / AllItems . aspx${'$'}[][]
) [20]
Chapter 8.
(contents/troubleshooting.tex
Overfull \hbox (12.71466pt too wide) in paragraph at lines 19--21
\T1/phv/m/n/10 (-20) When you get an ex-cep-tion like \T1/cmtt/m/n/10 FileNotFo
undException: dbfs:/user/hive/warehouse/sap.db/mbew

Overfull \hbox (33.41058pt too wide) in paragraph at lines 30--33
\T1/phv/m/n/10 (-20) Full er-ror mes-sage: \T1/cmtt/m/n/10 Unable to negotiate 
with xx.xxx.xxx.xxx port 22: no matching key exchange

Overfull \hbox (18.79771pt too wide) in paragraph at lines 30--33
\T1/cmtt/m/n/10 method found. Their offer: diffie-hellman-group1-sha1,diffie-he
llman-group14-sha1\T1/phv/m/n/10 (-20) . This
) 
AED: lastpage setting LastPage
[21]""".trimIndent()
        val four = """
(C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\auxil\UM
D6001197392Rxx_data-science-platform.aux) """.trimIndent()
        val five = """)"""
        val six = """
(see the transcript file for additional information) <C:\Users\thoscho\AppData\
Local\MiKTeX\2.9\fonts/pk/ljfour/jknappen/ec/dpi600\tctt1000.pk>{C:/Users/thosc
ho/AppData/Local/Programs/MiKTeX 2.9/fonts/enc/dvips/mnsymbol/MnSymbolA.enc}{C:
/Users/thoscho/AppData/Local/Programs/MiKTeX 2.9/fonts/enc/dvips/mnsymbol/MnSym
bolC.enc} <C:\Users\thoscho\AppData\Local\MiKTeX\2.9\fonts/pk/ljfour/jknappen/e
c/dpi600\ectt1000.pk>{C:/Users/thoscho/AppData/Local/Programs/MiKTeX 2.9/fonts/
enc/dvips/base/8r.enc}<C:/Users/thoscho/AppData/Local/Programs/MiKTeX 2.9/fonts
/type1/public/mnsymbol/MnSymbol10.pfb><C:/Users/thoscho/AppData/Local/Programs/
MiKTeX 2.9/fonts/type1/urw/helvetic/uhvb8a.pfb><C:/Users/thoscho/AppData/Local/
Programs/MiKTeX 2.9/fonts/type1/urw/helvetic/uhvr8a.pfb><C:/Users/thoscho/AppDa
ta/Local/Programs/MiKTeX 2.9/fonts/type1/urw/helvetic/uhvro8a.pfb>
Output written on C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-plat
form-dsd\out\UMD6001197392Rxx_data-science-platform.pdf (21 pages, 254079 bytes
).""".trimIndent()
        val nine = """
SyncTeX written on C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-platform-dsd\out\UMD6001197392Rxx_data-science-platform.synctex.gz.
Transcript written on C:/Users\thoscho\GitRepos\prodrive\prodrive-data-science-
platform-dsd\auxil\UMD6001197392Rxx_data-science-platform.log.

Process finished with exit code 0

    """.trimIndent()

        val messages = runLogParser(header + styFiles + moreStyFiles + startWarnings + evenMoreStyFiles + rest + one + two + three + four + five + six + nine)

    }

    private fun runLogParser(inputText: String): List<LatexLogMessage> {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<LatexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project)
        val listener = LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView)

        val input = inputText.split('\n')
        input.forEach { listener.processNewText(it) }

        return latexMessageList.toList()
    }
}