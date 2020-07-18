package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMessage
import nl.hannahsten.texifyidea.run.latex.logtab.LatexOutputListener
import nl.hannahsten.texifyidea.run.latex.logtab.ui.LatexCompileMessageTreeView

class LatexQuickRunLogParserTest : BasePlatformTestCase() {
    /**
     * Useful regex for matching braces: \(([^(^)]+)\)
     */

    override fun getTestDataPath(): String {
        return "test/resources/run"
    }

    fun testFileStack() {
        val text = """
This is pdfTeX, Version 3.14159265-2.6-1.40.17    7 JAN 2017 14:50
entering extended mode
 restricted \write18 enabled.
 %&-line parsing enabled.
**./index.tex
(./index.tex
LaTeX2e <2016/03/31> patch level 3
Babel <3.9r> and hyphenation patterns for 11 language loaded.




Package hyperref Message: Driver : hpdftex.
















 


.................................................
. LaTeX info: "xparse/define-command"
. 
. Defining command \XXXint with sig. 'm m m m' on line 244.
.................................................
.................................................
. LaTeX info: "xparse/define-command"
. 
. Defining command \ddashint with sig. '' on line 245.
.................................................
.................................................
. LaTeX info: "xparse/define-command"
. 
. Defining command \intbar with sig. '' on line 246.
.................................................
.................................................
. LaTeX info: "xparse/define-command"
. 
. Defining command \canonical with sig. '' on line 257.
.................................................
\c@theorem=\count180
 
\openout1 = `index.aux'.

LaTeX Font Info:    Checking defaults for OML/cmm/m/it on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for T1/cmr/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for OT1/cmr/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for OMS/cmsy/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for OMX/cmex/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for U/cmr/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.
LaTeX Font Info:    Checking defaults for PD1/pdf/m/n on input line 288.
LaTeX Font Info:    ... okay on input line 288.


\AtBeginShipoutBox=\box59
Package hyperref Info: Link coloring ON on input line 288.


LaTeX Info: Redefining \ref on input line 288.
LaTeX Info: Redefining \pageref on input line 288.
LaTeX Info: Redefining \nameref on input line 288.

 
\@outlinefile=\write5
\openout5 = `index.out'.

LaTeX Info: Redefining \microtypecontext on input line 288.
Package microtype Info: Generating PDF output.
Package microtype Info: Character protrusion enabled .
Package microtype Info: Using default protrusion set `alltext'.
Package microtype Info: Automatic font expansion enabled ,
             stretch: 20, shrink: 20, step: 1, non-selected.
Package microtype Info: Using default expansion set `basictext'.
Package microtype Info: No adjustment of tracking.
Package microtype Info: No adjustment of interword spacing.
Package microtype Info: No adjustment of character kerning.


LaTeX Font Info:    Try loading font information for U+lasy on input line 288.


LaTeX Font Info:    Try loading font information for U+msa on input line 288.



LaTeX Font Info:    Try loading font information for U+msb on input line 288.



LaTeX Font Info:    Try loading font information for U+esint on input line 288.



Package xypdf Info: Line width: 0.39998pt on input line 288.

ABD: EveryShipout initializing macros
Overfull \hbox  in paragraph at lines 305--305
 [][] 
 []

Package microtype Info: Loading generic settings for font family
             `cmtt' .
             For optimal results, create family-specific settings.
             See the microtype manual for details.

Overfull \hbox  in paragraph at lines 308--308
[][] 
 []


\tf@toc=\write6
\openout6 = `index.toc'.


Underfull \hbox  in paragraph at lines 326--326
[]\OT1/cmtt/m/n/10 Bas - About the paper: Tobi and I extracted the minimal
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 setting to get to the integrability result. We encountered
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 the problem that ${'$'}[][]\OT1/cmr/m/n/10  ${'$'} \OT1/cmtt/m/n/10 is known to be a Fr^^Sechet--Lie
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 group only for ${'$'}\OML/cmm/m/it/10 k \OT1/cmr/m/n/10  = 1${'$'} 
\OT1/cmtt/m/n/10 and ${'$'}\OML/cmm/m/it/10 k \OT1/cmr/m/n/10  = \OML/cmm/m/it/
10 n \OMS/cmsy/m/n/10 ^^@ \OT1/cmr/m/n/10  1${'$'}\OT1/cmtt/m/n/10 . We bypasse
d this by
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 looking at Fr^^Sechet-Lie groups ${'$'}\OML/cmm/m/it/10 G${'$'} \OT1/cmt
t/m/n/10 that occur as subgroups of
 []


Underfull \hbox  in paragraph at lines 326--326
[][]\OT1/cmr/m/n/10  ${'$'}\OT1/cm
tt/m/n/10 .  In the last section, I had
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 to work with ${'$'}[]\OT1/cmr/m/n/10  (\OML/cmm/m/it/10 M; h; 
\OT1/cmr/m/n/10  [^^H])${'$'} \OT1/cmtt/m/n/10 rather than the much more natura
l
 []


Underfull \hbox  in paragraph at lines 326--326
[]\OT1/cmr/m/n/10  ${'$'} \OT1/cmt
t/m/n/10 because Theorem B.1 requires ${'$'}\OMS/cmsy/m/n/10 M${'$'} \OT1/cmtt/m/n/10 to 
be connected.
 []


Underfull \hbox  in paragraph at lines 326--326
\OT1/cmtt/m/n/10 For this reason, I work on the connected component of ${'$'}\OT1/cm
r/m/n/10  ^^H${'$'} \OT1/cmtt/m/n/10 in
 []

[1

{c:/texlive/2016/texmf-var/fonts/map/pdftex/updmap/pdftex.map}]
Underfull \hbox  in paragraph at lines 330--330
[]\OT1/cmtt/m/n/10 Question from Bas to Karl-Hermann : Do you
 []


Underfull \hbox  in paragraph at lines 330--330
\OT1/cmtt/m/n/10 think we can lift this connectedness restriction? If yes,
 []


Underfull \hbox  in paragraph at lines 330--330
\OT1/cmtt/m/n/10 let's extend Theorem B.1 and remove all the ${'$'}\OT1/cmr/m/n/10  [^^H]${'$'}\OT1/cmtt/m/n/10 's in ${'$'}\OMS/cmsy/m/n/10 x\OT1/cmr/m/n/10  5${'$'}\O
T1/cmtt/m/n/10 . If
 []


Underfull \hbox  in paragraph at lines 335--335
[]\OT1/cmtt/m/n/10 Question from Bas to all: how do you guys feel about removin
g
 []


Underfull \hbox  in paragraph at lines 335--335
\OT1/cmtt/m/n/10 the TOC because it's such a short paper? . T: I like the ToC. But it occupied to much space in
 []


Underfull \hbox  in paragraph at lines 335--335
\OT1/cmtt/m/n/10 my opinion so that I styled it in a more condensed form, what
 []


Underfull \hbox  in paragraph at lines 340--340
[]\OT1/cmtt/m/n/10 Bas - About the intro: I propose to present the results in
 []


Underfull \hbox  in paragraph at lines 340--340
\OMS/cmsy/m/n/10 x\OT1/cmr/m/n/10  5${'$'} \OT1/cmtt/m/n/10 as the main point o
f this paper, with attention to the
 []


Underfull \hbox  in paragraph at lines 340--340
\OT1/cmtt/m/n/10 examples in ${'$'}\OMS/cmsy/m/n/10 x\OT1/cmr/m/n/10  6${'$'}\OT1/cm
tt/m/n/10 , and the definition of the central extension
 []


Underfull \hbox  in paragraph at lines 340--340
\OT1/cmtt/m/n/10 in ${'$'}\OMS/cmsy/m/n/10 x\OT1/cmr/m/n/10  3\OML/cmm/m/it/10 
:\OT1/cmr/m/n/10  3${'$'}\OT1/cmtt/m/n/10 . We should definitely say that we us
e differential
 []

[2]
Overfull \hbox  in paragraph at lines 395--399
[]\OT1/cmr/m/n/10  It con-tains the co-ho-mol-ogy group ${'$'}\OML/cmm/m/it/10 
H[]\OT1/cmr/m/n/10   [] [] \OMS
/cmsy/m/n/10 ^^R []\OT1/cmr/m/n/10  ${'$'}
 []

[3]
Package hyperref Info: bookmark level for unknown proposition defaults to 0 on 
input line 465.
 <xymatrix 5x2 451> [4]
LaTeX Font Info:    Try loading font information for U+euf on input line 503.



Package hyperref Info: bookmark level for unknown remark defaults to 0 on input
 line 506.
 [5] [6]
Package hyperref Info: bookmark level for unknown example defaults to 0 on inpu
t line 645.

Overfull \hbox  in paragraph at lines 658--658
[]\OT1/cmr/bx/n/10 Proposition 3.8. [][] 
 []


Overfull \hbox  in paragraph at lines 668--672
[]\OT1/cmr/m/n/10  For a dif-fer-en-tial char-ac-ter ${'$'}\OML/cmm/m/it/10 h${'$'} 
\OT1/cmr/m/n/10  with cur-va-ture ${'$'}\OML/cmm/m/it/10 !${'$'}\OT1/cmr/m/n/10  , the smooth map ${'$'}[][] [] []  []
 \OMS/cmsy/m/n/10 !
 []

[7]
Underfull \hbox  in paragraph at lines 695--695
[]\OT1/cmtt/m/n/10 T: I would move this section to be after section 5. Here,
 []


Underfull \hbox  in paragraph at lines 695--695
\OT1/cmtt/m/n/10 at this point, the discussed central extension appears from
 []


Underfull \hbox  in paragraph at lines 695--695
\OT1/cmtt/m/n/10 nowhere. Section 5 gives us a certain class of cocycles that
 []


Underfull \hbox  in paragraph at lines 695--695
\OT1/cmtt/m/n/10 can be integrated and then we proceed to characterize the type

 []

[8]
Underfull \hbox  in paragraph at lines 783--783
[]\OT1/cmtt/m/n/10 T: I had trouble to understand the previous remark and
 []


Underfull \hbox  in paragraph at lines 783--783
\OT1/cmtt/m/n/10 tried to work out the details. As it stands, I need more
 []

[9]
LaTeX Font Info:    Try loading font information for OMS+cmr on input line 841.

 
LaTeX Font Info:    Font shape `OMS/cmr/m/it' in size <10> not available
              Font shape `OMS/cmsy/m/n' tried instead on input line 841.
 [10] <xymatrix 3x2
325> <xymatrix 3x2 325> [11]
Package hyperref Info: bookmark level for unknown theorem defaults to 0 on inpu
t line 915.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `math shift' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `\subseteq' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `\new@ifnextchar' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `subscript' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `\new@ifnextchar' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `\omega' on input line 922.


Package hyperref Warning: Token not allowed in a PDF string :
                removing `math shift' on input line 922.


Underfull \hbox  in paragraph at lines 924--924
[]\OT1/cmtt/m/n/10 T: I changed this section a bit to also include actions of
 []


Underfull \hbox  in paragraph at lines 924--924
\OT1/cmtt/m/n/10 current groups ${'$'}\OML/cmm/m/it/10 C[]\OT1/cmr/m/n/10  ${'$'}\OT1/cmtt/m/n/10 . This extension is 
necessary to have
 []


Underfull \hbox  in paragraph at lines 924--924
\OT1/cmtt/m/n/10 the Kac-Moody group as an example. Not sure if we should
 []


Underfull \hbox  in paragraph at lines 933--933
[]\OT1/cmtt/m/n/10 T: Previously, there was simply ${'$'}[]\OT1/cmr/m/n/10  ${'$'} \OT1/cmtt/m/n/10 on the right
 []


Underfull \hbox  in paragraph at lines 933--933
\OT1/cmtt/m/n/10 side of the extension, but the diffeos have to preserve the
 []

[12]
Overfull \hbox  in paragraph at lines 950--959
\OT1/cmr/m/n/10  tions ${'$'}\OML/cmm/m/it/10 A [] []  []\OT1/cmr/m/n/10  
(\OML/cmm/m/it/10 M; h; \OT1/cmr/m/n/10  [^^H]) \OMS/cmsy/m/n/10 ! []\OT1/
cmr/m/n/10  ${'$'} and ${'$'}\OML/cmm/m/it/10 B [] []  []\OT1/cmr/m/n/10
  (\OML/cmm/m/it/10 S; g; \OT1/cmr/m/n/10  [^^H]) \OMS/cmsy/m/n/10 ! 
[]\OT1/cmr/m/n/10  ${'$'}. 
 []


Underfull \hbox  in paragraph at lines 993--993
[]\OT1/cmtt/m/n/10 T: ${'$'}\OML/cmm/m/it/10 G${'$'} \OT1/cmtt/m/n/10 and ${'$'}\OML/cmm/m/it/
10 H${'$'} \OT1/cmtt/m/n/10 don't have to be subgroups. We only need group
 []

[13]
Package hyperref Info: bookmark level for unknown corollary defaults to 0 on in
put line 1080.
 [14]
Overfull \hbox  in paragraph at lines 1135--1135
[]\OT1/cmr/bx/n/12 Central ex-ten-sion of the group of sym-plec-to-mor-phisms 
 []

[15]
Underfull \hbox  in paragraph at lines 1154--1154
[]\OT1/cmtt/m/n/10 T: Can we integrate Roger cocycle by choosing ${'$'}\OML/cmm/m/it
/10 S${'$'} \OT1/cmtt/m/n/10 and ${'$'}\OML/cmm/m/it/10 ^^L${'$'} \OT1/cmtt/m/n/10 in a
 []


Overfull \hbox  in paragraph at lines 1174--1174
[]\OT1/cmr/bx/n/12 Central ex-ten-sions of au-to-mor-phism groups of ${'$'}\OML/cmm/
m/it/12 G${'$'}\OT1/cmr/bx/n/12 -structures 
 []


Package hyperref Warning: Token not allowed in a PDF string :
                removing `math shift' on input line 1174.


LaTeX Warning: Citation `Hitchin' on page 16 undefined on input line 1183.

[16]

LaTeX Warning: Citation `Joyce1996' on page 17 undefined on input line 1197.

! Undefined control sequence.
l.1219 		\todoCheck
                   
The control sequence at the end of the top line
of your error message was never \def'ed. If you have
misspelled it , type `I' and the correct
spelling . Otherwise just continue,
and I'll forget about whatever was undefined.

! Undefined control sequence.
l.1229 ...canonical \) in \ is \, see~\cite[Theorem~...
The control sequence at the end of the top line
of your error message was never \def'ed. If you have
misspelled it , type `I' and the correct
spelling . Otherwise just continue,
and I'll forget about whatever was undefined.


LaTeX Warning: Citation `KatzShnider2008' on page 17 undefined on input line 12
29.

[17]

LaTeX Warning: Citation `Mickelssonâ1987' on page 18 undefined on input line 
1261.


LaTeX Warning: Citation `Brylinski2007' on page 18 undefined on input line 1261
.

[18]
Overfull \hbox  in paragraph at lines 1318--1320
\OT1/cmr/m/n/10  Since ${'$'}\OML/cmm/m/it/10 h${'$'} \OT1/cmr/m/n/10  is in-va
ri-ant un-der thin ho-mo-topies, this ex-pres-sion sim-pli-fies to ${'$'}\OML/cmm/m/
it/10 T[]\OT1/cmr/m/n/10  
 =
 []

[19] [20]
LaTeX Font Info:    Font shape `OMS/cmr/m/n' in size <10> not available
              Font shape `OMS/cmsy/m/n' tried instead on input line 1488.

 [21] [22] [23]
Package hyperref Info: bookmark level for unknown pro defaults to 0 on input li
ne 1623.
 [24]
Overfull \hbox  in paragraph at lines 1672--1677
\OT1/cmr/m/n/10  This con-di-tion fol-lows, as for ${'$'}\U/msb/m/n/10 T${'$'}\OT1/c
mr/m/n/10  -valued char-ac-ters, di-rectly from ${'$'}\OML/cmm/m/it/10 h\OT1/cm
r/m/n/10   = [][]${'$'}.
 []


Overfull \hbox  detected at line 1713
\OT1/cmr/bx/n/10 1 \OMS/cmsy/m/n/10 ! []\OT1/cmr/m/n/10 (\OML/cmm/m/it/10 ^^Y[]
\OT1/cmr/m/n/10 \OML/cmm/m/it/10 ; Z\OT1/cm
r/m/n/10 ) [] \OML/cmm/m/it/10 H[]\OT1/cmr/m/n/10 [] \OMS/cmsy/m/n/10 ! []\OT1/cmr/m/n/10 [] [] [][] \OMS/cmsy/m/n/10 
! \OML/cmm/m/it/10 Z[]\OT1/cmr/m/n/10 [] \OMS/cmsy/m/n/10 ! \OT1/cmr/bx/n/10 0\OML/cmm/m/it
/10 :
 []

[25] [26]
Overfull \hbox  in paragraph at lines 1824--1827
[]\OT1/cmr/m/n/10  If ${'$'}[\OML/cmm/m/it/10 !\OT1/cmr/m/n/10  ]${'$'} is ${'$'}\OM
L/cmm/m/it/10 ^^Y[]\OT1/cmr/m/n/10  ${'$'}-invariant, then we ob-tain an ob-struc-tion class in ${'$'}\OML/cmm/m/it/10 H[
]\OT1/cmr/m/n/10  (\OML/cmm/m/it/10 ^^Y[]\OT1/cmr/m/n/10  \OML/cmm/m/it/10 ; B[]\OT1/cmr/m/n/10  )${'$'}
 []

[27]
Missing character: There is no Ã in font cmr10!
Missing character: There is no ¶ in font cmr10!
Missing character: There is no Ã in font cmr10!
Missing character: There is no ¶ in font cmr10!
Missing character: There is no Ã in font cmr10!
Missing character: There is no ¶ in font cmr10!
 [28] [29] [30]
Package atveryend Info: Empty hook `BeforeClearDocument' on input line 1990.
Package atveryend Info: Empty hook `AfterLastShipout' on input line 1990.
 
Package atveryend Info: Executing hook `AtVeryEndDocument' on input line 1990.
Package atveryend Info: Executing hook `AtEndAfterFileList' on input line 1990.

Package rerunfilecheck Info: File `index.out' has not changed.
             Checksum: 3C6F5B6E3EDB9A0988C7AD257A2216A1;1433.


LaTeX Warning: There were undefined references.

Package atveryend Info: Empty hook `AtVeryVeryEnd' on input line 1990.
 ) 
Here is how much of TeX's memory you used:
 31800 strings out of 494838
 604025 string characters out of 6177329
 725486 words of memory out of 5000000
 34284 multiletter control sequences out of 15000+600000
 26056 words of font info for 168 fonts, out of 8000000 for 9000
 36 hyphenation exceptions out of 8191
 61i,19n,114p,10414b,910s stack positions out of 5000i,500n,10000p,200000b,80000s
pdfTeX warning : name{section.B} has been referenced but does not exist
, replaced by a fixed one

pdfTeX warning : name{section.A} has been referenced but does not exist, 
replaced by a fixed one

 <c:/Users/Tobi/.texlive2016/texmf-var/fonts/pk/ljfour/public/esint/esint10.600
pk><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmbx10.pfb><c:/te
xlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmbx12.pfb><c:/texlive/201
6/texmf-dist/fonts/type1/public/amsfonts/cm/cmbx7.pfb><c:/texlive/2016/texmf-di
st/fonts/type1/public/amsfonts/cm/cmbx9.pfb><c:/texlive/2016/texmf-dist/fonts/t
ype1/public/amsfonts/cm/cmex10.pfb><c:/texlive/2016/texmf-dist/fonts/type1/publ
ic/amsfonts/cmextra/cmex7.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/am
sfonts/cmextra/cmex8.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfont
s/cm/cmmi10.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmmi
12.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmmi5.pfb><c:
/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmmi6.pfb><c:/texlive/2
016/texmf-dist/fonts/type1/public/amsfonts/cm/cmmi7.pfb><c:/texlive/2016/texmf-
dist/fonts/type1/public/amsfonts/cm/cmmi8.pfb><c:/texlive/2016/texmf-dist/fonts
/type1/public/amsfonts/cm/cmr10.pfb><c:/texlive/2016/texmf-dist/fonts/type1/pub
lic/amsfonts/cm/cmr12.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfon
ts/cm/cmr17.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmr5
.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmr6.pfb><c:/te
xlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmr7.pfb><c:/texlive/2016/
texmf-dist/fonts/type1/public/amsfonts/cm/cmr8.pfb><c:/texlive/2016/texmf-dist/
fonts/type1/public/amsfonts/cm/cmsy10.pfb><c:/texlive/2016/texmf-dist/fonts/typ
e1/public/amsfonts/cm/cmsy5.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/
amsfonts/cm/cmsy7.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/c
m/cmti10.pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmtt10.
pfb><c:/texlive/2016/texmf-dist/fonts/type1/public/amsfonts/cm/cmtt9.pfb><c:/te
xlive/2016/texmf-dist/fonts/type1/public/amsfonts/euler/eufm10.pfb><c:/texlive/
2016/texmf-dist/fonts/type1/public/amsfonts/euler/eufm7.pfb><c:/texlive/2016/te
xmf-dist/fonts/type1/public/amsfonts/symbols/msam10.pfb><c:/texlive/2016/texmf-
dist/fonts/type1/public/amsfonts/symbols/msbm10.pfb><c:/texlive/2016/texmf-dist
/fonts/type1/public/amsfonts/symbols/msbm7.pfb><c:/texlive/2016/texmf-dist/font
s/type1/public/xypic/xyatip10.pfb><c:/texlive/2016/texmf-dist/fonts/type1/publi
c/xypic/xybtip10.pfb>
Output written on index.pdf .
PDF statistics:
 672 PDF objects out of 1000 
 579 compressed objects within 6 object streams
 192 named destinations out of 1000 
 20125 words of extra memory for PDF output out of 20736 


    """.trimIndent()

        runLogParser(text).forEach {
            println(it)
            println()
        }
    }

    private fun runLogParser(inputText: String): List<LatexLogMessage> {
        val srcRoot = myFixture.copyDirectoryToProject("./", "./")
        val project = myFixture.project
        val mainFile = srcRoot.findFileByRelativePath("main.tex")
        val latexMessageList = mutableListOf<LatexLogMessage>()
        val bibtexMessageList = mutableListOf<BibtexLogMessage>()
        val treeView = LatexCompileMessageTreeView(project, latexMessageList)
        val listener = LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView)

        val input = inputText.split('\n')
        input.forEach { listener.processNewText(it) }

        return latexMessageList.toList()
    }
}