# Errors and warnings

This is a list of most of the errors and warnings that are in Appendix B of _The Latex Companion_ [mittelbach2004](#mittelbach2004), plus some more we encountered while error/warning hunting.
For each error/warning, we give a minimal working example, and the relevant part of the output log.
For more info on a message, see [mittelbach2004](#mittelbach2004).
Used by us for testing purposes, written down here for future reference.

The logs here are when using `-file-line-error` flag when compiling.

## LaTeX Errors

### calc: `<character>' invalid at this point.

```latex
\docmentclass{article}

\usepackage{calc}

\newcounter{error}
\setcounter{error}{3}
\setcounter{error}{\value{error} & 2}
```

```
./errors.tex:7: Package calc Error: `&' invalid at this point.
```

### amsfonts: <command> allowed only in math mode
```latex
\documentclass{article}

\usepackage{amsfonts}

\begin{document}
    \mathbb{A}
\end{document}
```

```
./errors.tex:6: LaTeX Error: \mathbb allowed only in math mode.
```

### <name> undefined
```latex
\documentclass{article}

\renewcommand{\bloop}{nothing}

\begin{document}
    \bloop
\end{document}
```

```
./errors.tex:3: LaTeX Error: Command \bloop undefined.
```

### \< in mid line
```latex
\documentclass{article}

\begin{document}
    \begin{tabbing}
        One \= Two \\
        1 \> \< 2 \\
    \end{tabbing}
\end{document}
```

```
./errors.tex:6: LaTeX Error: \< in mid line.
```

### A <Box> was supposed to be here
```latex
\documentclass{article}

\newcommand{\mybox}

\begin{document}
    \sbox{\mybox}{0}
\end{document}
```

```
./errors.tex:4: A <box> was supposed to be here.
```

### textcomp: Accent <command> not provided by font family <name>
```latex
\documentclass{article}

\usepackage{mathpazo}
\usepackage{textcomp}

\begin{document}
    \textuparrow
\end{document}
```

```
./errors.tex:7: Package textcomp Error: Symbol \textuparrow not provided by
(textcomp)                font family ppl in TS1 encoding.
(textcomp)                Default family used instead.
```

### Argument of <command> has an extra }
```latex
\documentclass{article}

\begin{document}
    \fbox}
\end{document}
```

```
./errors.tex:4: Argument of \fbox  has an extra }.
```

### Bad \line or \vector argument
```latex
\documentclass{article}

\begin{document}
    \line(1,1){-1}
\end{document}
```

```
./errors.tex:4: LaTeX Error: Bad \line or \vector argument.
```

### Bad math environment delimiter
```latex
\documentclass{article}

\begin{document}
    \]
\end{document}
```

```
./errors.tex:4: LaTeX Error: Bad math environment delimiter.
```

### Bad register code (<code>)
```latex
\documentclass{article}

\begin{document}
hallo
\sbox{\textwidth}{box}
\end{document}
```

```
./errors.tex:5: Bad register code (23592960).
```

### \begin{<env>} ended by \end{<other env>}
```latex
\documentclass{article}

\begin{document}
    \end{centering}
\end{document}
```

```
./errors.tex:4: LaTeX Error: \begin{document} ended by \end{centering}.
```

### amsmath: \begin{split} won't work here
```latex
\documentclass{article}

\usepackage{amsmath}

\begin{document}
    \begin{split}
        \pi
    \end{split}
\end{document}
```

```
./errors.tex:8: Package amsmath Error: \begin{split} won't work here.
```

### Can be used only in preamble
```latex
\documentclass{article}

\begin{document}
    \begin{document}
    \end{document}
\end{document}
```

```
./errors.tex:4: LaTeX Error: Can be used only in preamble.
```

### Cannot be used in preamble
```latex
\documentclass{article}

\nocite{magic}

\begin{document}
\end{document}
```

```
./errors.tex:3: LaTeX Error: Cannot be used in preamble.
```

### graphicx/graphics: Cannot determine size of graphic in <file>
Compile with pdflatex and `-output-format=dvi`

```latex
\documentclass{article}

\usepackage{graphicx}

\begin{document}
    \includegraphics{figures/background-black-cat.jpg}
\end{document}
```

```
./errors.tex:6: LaTeX Error: Cannot determine size of graphic in figures/backgr
ound-black-cat.jpg (no BoundingBox).
```

### graphicx/graphics: Cannot include graphics of type: <ext>
```latex
\documentclass{article}

\usepackage{graphicx}

\begin{document}
    \includegraphics[type=tex]{figures/background-black-cat.jpg}
\end{document}
```

```
./errors.tex:6: LaTeX Error: Can not include graphics of type: tex.
```

### \caption outside float
```latex
\documentclass{article}

\begin{document}
    \caption{This is illegal.}
\end{document}
```

```
./errors.tex:4: LaTeX Error: \caption outside float.
```

### Command <name> already defined
```latex
\documentclass{article}

\newcommand{\documentclass}{hahaha}

\begin{document}
\end{document}
```

```
./errors.tex:3: LaTeX Error: Command \documentclass already defined.
```

### Command <name> not defined as a math alphabet
```latex
\documentclass{article}

\SetMathAlphabet{\methit}{normal}{OT1}{ppl}{m}{it}

\begin{document}
    $x$
\end{document}
```

```
./errors.tex:3: LaTeX Error: Command `\methit' not defined as a math alphabet.
```

### Counter too large
```latex
\documentclass{article}

\renewcommand{\theequation}{\alph{equation}}
\setcounter{equation}{27}

\begin{document}
    \theequation
\end{document}
```

```
./errors.tex:6: LaTeX Error: Counter too large.
```

### Dimension too large
```latex
\documentclass{article}

\begin{document}
    \rule{16384pt}{2pt}
\end{document}
```

```
./errors.tex:5: Dimension too large.
```

### amsmath: \displaybreak cannot be applied here
```latex
\documentclass{article}

\usepackage{amsmath}

\begin{document}
    \[
        \begin{aligned}
            x \displaybreak y
        \end{aligned}
    \]
\end{document}
```

```
./errors.tex:8: Package amsmath Error: \displaybreak cannot be applied here.
```

### graphicx/graphics: Division by 0
```latex
\documentclass{article}

\usepackage{graphicx}

\begin{document}
    \includegraphics[angle=-90,height=3cm]{figures/background-black-cat.jpg}
\end{document}
```

```
./errors.tex:6: Package graphics Error: Division by 0.
```

### Double subscript
```latex
\documentclass{article}

\begin{document}
    $x_i_2$
\end{document}
```

```
./errors.tex:4: Double subscript.
l.4     $x_i_
             2$
```

### Encoding scheme <name> unknown
```latex
\documentclass{article}

\usepackage[15]{fontenc}

\begin{document}
    text
\end{document}
```

```
/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:104: Package font
enc Error: Encoding file `15enc.def' not found.
(fontenc)                You might have misspelt the name of the encoding.

/home/abby/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:105: LaTeX Error:
 Encoding scheme `15' unknown.

./errors.tex:5: LaTeX Error: Encoding scheme `15' unknown.
```

### Environment <name> undefined
```latex
\documentclass{article}

\renewenvironment{bla}{a}{b}

\begin{document}
\end{document}
```

```
./errors.tex:3: LaTeX Error: Environment bla undefined.
```

### amsmath: Erroneous nesting of equation structures
```latex
\documentclass{article}

\usepackage{amsmath}

\begin{document}
    \begin{align}
        \begin{align}
            x
        \end{align}
    \end{align}
\end{document}
```

```
./errors.tex:10: Package amsmath Error: Erroneous nesting of equation structure
s;
(amsmath)                trying to recover with `aligned'.
```

### Extra alignment tab has been changed to \cr
```latex
\documentclass{article}

\begin{document}
    \begin{tabular}{2}
        1 & 2 & 3 \\
    \end{tabular}
\end{document}
```

```
./errors.tex:5: Extra alignment tab has been changed to \cr.
<recently read> \endtemplate

l.5         1 & 2 &
                    3 \\
```

### Extra \endgroup
```latex
\documentclass{article}

\begin{document}
    \end{centering}
\end{document}
```

```
./errors.tex:4: Extra \endgroup.
```

### Extra \or
```latex
\documentclass{article}

\or
```

```
./main.tex:3: Extra \or.
l.3 \or
```

### Extra \right
```latex
\documentclass{article}

\begin{document}
    $(\right)$
\end{document}
```

```
./main.tex:4: Extra \right.
l.4     $(\right)
                 $
```

### Extra }, or forgotten $
```latex
\documentclass{article}

\begin{document}
    $x}$
\end{document}
```

```
./main.tex:4: Extra }, or forgotten $.
l.4     $x}
           $
```

### Extra }, or forgotten \endgroup
```latex
\documentclass{article}

\begin{document}
    \begin{center}
        text}
    \end{center}
\end{document}
```

```
./main.tex:5: Extra }, or forgotten \endgroup.
l.5         text}
```

### File `<name>' not found
```latex
\documentclass{article}

\begin{document}
    \input{fakenews.tex}
\end{document}
```

```
! LaTeX Error: File `fakenews.tex' not found.

Type X to quit or <RETURN> to proceed,
or enter new name. (Default extension: tex)

Enter file name:
./main.tex:4: Emergency stop.
<read *>

l.4     \input{fakenews.tex}

./main.tex:4:  ==> Fatal error occurred, no output PDF file produced!
```

### Float(s) lost
```latex
\documentclass{article}
\usepackage{graphicx}

\begin{document}
    \footnote{\begin{figure}
                  \includegraphics{figures/cat.tikz}
    \end{figure}}
\end{document}
```

```
./main.tex:8: LaTeX Error: Float(s) lost.
```

### Font family <cdp>+<family> unknown
```latex
\documentclass{article}

\DeclareFontShape{T1}{bla}{}{}{}{}
```

```
./main.tex:3: LaTeX Error: Font family `T1+bla' unknown.
```

### Font <name> not found



### Font <internal-name>=<external-name> not loadable: Metric (TFM) file not found

```latex
% To reproduce, tlmgr remove collection-fontsrecommended
\documentclass{article}
\usepackage[T1]{fontenc}
\begin{document}
    Text.
\end{document}
```

```
kpathsea: Running mktextfm ecrm1000
/home/user/texlive/2019/texmf-dist/web2c/mktexnam: Could not map source abbreviation  for ecrm1000.
/home/user/texlive/2019/texmf-dist/web2c/mktexnam: Need to update ?
mktextfm: Running mf-nowin -progname=mf \mode:=ljfour; mag:=1; nonstopmode; input ecrm1000
This is METAFONT, Version 2.7182818 (TeX Live 2019) (preloaded base=mf)

kpathsea: Running mktexmf ecrm1000

! I can't find file `ecrm1000'.
<*> ...ljfour; mag:=1; nonstopmode; input ecrm1000

Please type another input file name
! Emergency stop.
<*> ...ljfour; mag:=1; nonstopmode; input ecrm1000

Transcript written on mfput.log.
grep: ecrm1000.log: No such file or directory
mktextfm: `mf-nowin -progname=mf \mode:=ljfour; mag:=1; nonstopmode; input ecrm1000' failed to make ecrm1000.tfm.
kpathsea: Appending font creation commands to missfont.log.

/home/user/texlive/2019/texmf-dist/tex/latex/base/fontenc.sty:105: Font T1/cm
r/m/n/10=ecrm1000 at 10.0pt not loadable: Metric (TFM) file not found.
<to be read again>
                   relax
l.105 \fontencoding\encodingdefault\selectfont
```

### Font <internal-name>=<external> not loaded: Not enough room left



### Font shape <font shape> not found


### I can't find file `<name>'
From the LaTeX Companion:

> LaTeX normally uses the error message "File `&lt;name>' not found", which supports various user actions. However, depending on the package coding, you may get the current error instead.

It seemed easier to reproduce using just TeX instead of LaTeX.

```latex
\input fake.tex
\bye
```

```
! I can't find file `fake.tex'.
l.1 \input fake.tex
```

### I can't write on file `<name>'
Make the `main.aux` file read only with `chattr +i main.aux`, then compile as usual.

```
./main.tex:3: I can't write on file `main.aux'.
\document ...ate \openout \@mainaux \jobname .aux
                                                  \immediate \write \@mainau...
l.3 \begin{document}
```

### Illegal character in array arg
```latex
\documentclass{article}

\begin{document}
    \begin{tabular}{c!{--}}
        3&4
    \end{tabular}
\end{document}
```

```
./main.tex:4: LaTeX Error: Illegal character in array arg.
```

### Illegal parameter number in definition of <command>
```latex
\documentclass{article}

\newcommand{\breakstuff}{#1}
```

```
./main.tex:3: Illegal parameter number in definition of \breakstuff.
<to be read again>
                   1
l.3 \newcommand{\breakstuff}{#1}
```

### Illegal unit of measure (pt inserted)
```latex
\documentclass{article}

\begin{document}
    \rule{1}{3}
\end{document}
```

```
./main.tex:4: Illegal unit of measure (pt inserted).
<to be read again>
                   \relax
l.4     \rule{1}{3}
```

### Improper argument for math accent:
According to [amsmath](#amsmath), the following example should trigger this error.
However, it triggers a bunch of other errors...
```latex
\documentclass{article}

\usepackage{amsmath}

\begin{document}
    $\tilde k_{\lambda_j} = P_{\tilde \mathcal{M}}$
\end{document}
```

```
./main.tex:7: Argument of \math@egroup has an extra }.
<inserted text>
                \par
l.7 ...  \tilde k_{\lambda_j} = P_{\tilde \mathcal
                                                  {M}}
```

Expected output [amsmath](#amsmath):
```
! Package amsmath Error: Improper argument for math accent:
(amsmath)                Extra braces must be added to
(amsmath)                prevent wrong output.

See the amsmath package documentation for explanation.
Type  H <return>  for immediate help.
...

l.415 \tilde k_{\lambda_j} = P_{\tilde \mathcal
                                               {M}}
```

### Improper discretionary list
```latex
\documentclass{article}

\discretionary

\begin{document}
    hi
\end{document}
```

```
./main.tex:3: LaTeX Error: Missing \begin{document}.

./main.tex:7: Improper discretionary list.
<inserted text> }
```

### Improper \hyphenation (will be flushed.)
```latex
\documentclass{article}

\hyphenation{bl\"oop-floop-gloop}
```

```
./main.tex:3: Improper \hyphenation will be flushed.
\leavevmode ->\unhbox
                      \voidb@x
l.3 \hyphenation{bl\"o
                      op-floop-gloop}
```

### Improper \prevdepth
```latex
\documentclass{article}

\discretionary

\begin{document}
    hi
\end{document}
```

```
./main.tex:7: Improper \prevdepth.
\newpage ...everypar {}\fi \par \ifdim \prevdepth
                                                  >\z@ \vskip -\ifdim \prevd...
l.7 \end{document}
```

### Improper \spacefactor
```latex
\documentclass{article}

\begin{document}
    \showthe\spacefactor
\end{document}
```

```
./main.tex:4: Improper \spacefactor.
l.4     \showthe\spacefactor
```

### \include cannot be nested
```latex
\documentclass{article}

\begin{document}
    \include{main}
\end{document}
```

```
./main.tex:4: LaTeX Error: \include cannot be nested.
```

### Incompatible list can't be unboxed
```latex
\setbox0\vbox{}\unhbox0
```

```
./main.tex:4: Incompatible list can't be unboxed.
l.4     \setbox0\vbox{}\unhbox0
```

### Incomplete <conditional>; all text was ignored after line <number>
```latex
\documentclass{article}

\newcommand{\x}{3}

\begin{document}
    \ifnum\x=3 tada \else bloop
\end{document}
```

```
! Incomplete \ifnum; all text was ignored after line 6.
<inserted text>
                \fi
```

### Infinite glue shrinkage found in <somewhere>
```latex
\documentclass{article}

\begin{document}
    \hspace{0pt minus 1fil}
\end{document}
```

```
./main.tex:5: Infinite glue shrinkage found in a paragraph.
```

### amsmath: Invalid use of <command>
```latex
\documentclass{article}

\usepackage{amsmath}

\begin{document}
    \intertext{tada}
\end{document}
```

```
./main.tex:6: Package amsmath Error: Invalid use of \intertext.
```

### babel: Language definition file <language>.ldf not found
```latex
\documentclass{article}

\usepackage[bla]{babel}

\begin{document}

\end{document}
```

```
/home/abby/texlive/2019/texmf-dist/tex/generic/babel/babel.sty:554: Package bab
el Error: Unknown option `bla'. Either you misspelled it
(babel)                or the language definition file bla.ldf was not found.
```

### Limit controls must follow a math operator
```latex
\documentclass{article}

\begin{document}
    \limits
\end{document}
```

```
./main.tex:4: Missing $ inserted.
<inserted text>
                $
l.4     \limits

./main.tex:4: Limit controls must follow a math operator.
```

### \LoadClass in package file



### Lonely \item--perhaps a missing list environment

```latex
\documentclass{article}
\begin{document}
    \item
\end{document}
```

```

./errors.tex:4: LaTeX Error: Lonely \item--perhaps a missing list environment.

See the LaTeX manual or LaTeX Companion for explanation.
Type  H <return>  for immediate help.
 ...

l.4 \end
        {document}
```

### Math alphabet identifier <id> is undefined in math version <name>


```
./Untitled.tex:50: LaTeX Error: Math alphabet identifier \mathrm is undefined in math version `GFS'.

See the LaTeX manual or LaTeX Companion for explanation.
```

### Math version <name> is not defined

```latex
\documentclass{article}
\mathversion{GFS}
\begin{document}
    Text.
\end{document}
```

```
./errors.tex:2: LaTeX Error: Math version `GFS' is not defined.

See the LaTeX manual or LaTeX Companion for explanation.
Type  H <return>  for immediate help.
 ...

l.2 \mathversion{GFS}
```

### Misplaced alignment tab character &
Alternate forms: Misplaced \cr, \crcr, \noalign, \omit

```latex
\documentclass{article}
\begin{document}
    &
\end{document}
```

```
./errors.tex:3: Misplaced alignment tab character &.
l.3     &
```

### Missing \begin{document}

```latex
\documentclass{article}
Text.
\begin{document}
    Text.
\end{document}
```

```
./errors.tex:2: LaTeX Error: Missing \begin{document}.

See the LaTeX manual or LaTeX Companion for explanation.
Type  H <return>  for immediate help.
 ...

l.2 T
   ext.
```

### Missing control sequence inserted

```latex
\documentclass{article}
\newcommand t

\begin{document}
    Text.
\end{document}
```

```
./errors.tex:3: Missing control sequence inserted.
<inserted text>
\inaccessible
l.3
```

### Missing \cr inserted

```
./errors.tex:34: Missing \cr inserted.
<inserted text>
\cr
l.34         \end{tabularx}
```

### Missing delimiter (. inserted)

```latex
\documentclass{article}
\begin{document}
    \left
\end{document}
```

```
./errors.tex:4: Missing delimiter (. inserted).
<to be read again>
\let
l.4 \end{document}
```

### Missing \endcsname inserted

```latex
\documentclass[11pt]{article}
\newenvironment{Bl\"ode}
\begin{document}
    Main.
\end{document}
```

```
./main.tex:3: Missing \endcsname inserted.
```

### Missing number, treated as zero

```latex
\documentclass[11pt]{article}
\begin{document}
    Main.
    \value{page}
\end{document}
```

```
No file main.aux.
./main.tex:5: Missing number, treated as zero.
<to be read again>
                   \let
l.5 \end{document}
```

### Missing p-arg in array arg
### Missing @-exp in array arg
### Missing # inserted in alignment preamble.

```latex
\documentclass[11pt]{article}
\begin{document}
    \begin{tabular}{p}
        a
    \end{tabular}
\end{document}
```

```

./main.tex:3: LaTeX Error: Missing p-arg in array arg.

See the LaTeX manual or LaTeX Companion for explanation.
Type  H <return>  for immediate help.
 ...

l.3     \begin{tabular}{p}

./main.tex:3: Missing # inserted in alignment preamble.
<to be read again>
                   \cr
l.3     \begin{tabular}{p}


```

### Missing = inserted for \ifnum
### Missing = inserted for \ifdim

```latex
./main.tex:4: Missing = inserted for \ifnum.
<to be read again>
                   \let
l.4 \end{document}
```

```
./main.tex:4: Missing = inserted for \ifnum.
<to be read again>
                   \let
l.4 \end{document}
```

### Missing $ inserted {#missing-dollar}

```latex
\documentclass[11pt]{article}
\begin{document}
    _
\end{document}
```

```
./main.tex:3: Missing $ inserted.
<inserted text>
                $
l.3     _

./main.tex:4: Missing { inserted.
<to be read again>
                   \let
l.4 \end{document}


```

### Missing \endgroup inserted

```
./main.tex:7: Missing \endgroup inserted.
<inserted text>
                \endgroup
```

### Missing \right. inserted
### Missing } inserted {#missing-close-brace}
### Missing { inserted {#missing-open-brace}

```latex
\documentclass[11pt]{article}
\begin{document}
    \left(
\end{document}
```

```
./main.tex:4: Missing \right. inserted.
<inserted text>
                \right .
l.4 \end{document}
```

### Multiple \label's: label <label> will be lost
### Multiple tag

```latex
\documentclass[11pt]{article}
\usepackage{amsmath}
\begin{document}
    \begin{align}
    \label{33}
        \label{33}
    \end{align}
\end{document}
```

```

./main.tex:7: Package amsmath Error: Multiple \label's: label '33' will be lost
.

See the amsmath package documentation for explanation.
Type  H <return>  for immediate help.
 ...

l.7     \end{align}
```

### No counter '<name>' defined

```latex
\documentclass[11pt]{article}
\begin{document}
    \setcounter{name}
\end{document}
```

```
./main.tex:4: LaTeX Error: No counter 'name' defined.
```

### No Cyrillic encoding definition files were found



### No declaration for shape <font shape>



### No driver specified



### No room for a new <register>



### No \title given

```latex
\documentclass[11pt]{article}
\begin{document}
    \maketitle
\end{document}
```

```
./main.tex:3: LaTeX Error: No \title given.
```

## Errors not in the LaTeX Companion?

### !pdfTeX error: pdflatex (file <file>): cannot open <type> file for reading

```latex
\documentclass{article}
\usepackage[urw-garamond]{mathdesign}
\usepackage[T1]{fontenc}

\begin{document}
    Text.
\end{document}
```

```
!pdfTeX error: pdflatex (file ugmr8a.pfb): cannot open Type 1 font file for rea
ding
 ==> Fatal error occurred, no output PDF file produced!
```

### (other warnings from the LaTeX Companion omitted)

## LaTeX Warnings

### Citation `<key>' on page <number> undefined

```latex
\documentclass{article}

\begin{document}
    \cite{key}
\end{document}
```

```
LaTeX Warning: Citation `key' on page 1 undefined on input line 4.

LaTeX Warning: There were undefined references.
```

### Command <name> invalid in math mode

```latex
\documentclass{article}

\begin{document}
    $ö$
\end{document}
```

```

LaTeX Warning: Command \" invalid in math mode on input line 4.

./errors.tex:4: Please use \mathaccent for accents in math mode.
\add@accent ...@spacefactor \spacefactor }\accent
                                                  #1 #2\egroup \spacefactor ...
l.4     $ö
           $
./errors.tex:4: You can't use `\spacefactor' in math mode.
\add@accent ...}\accent #1 #2\egroup \spacefactor
                                                  \accent@spacefactor
l.4     $ö
           $
```

### Empty `thebibliography' environment

```latex
\documentclass{article}

\begin{document}
    \begin{thebibliography}{}
    \end{thebibliography}
\end{document}
```

```
LaTeX Warning: Empty `thebibliography' environment on input line 5.
```

### (\end occurred inside a group at level <number)

```latex
\documentclass{article}

\begin{document}
    {
\end{document}
```

```
(\end occurred inside a group at level 1)

### simple group (level 1) entered at line 4 ({)
### bottom level
```

### (\end occurred when <condition> on line <line number> was incomplete)

```latex
\documentclass{article}

\begin{document}
    \include{included}
\end{document}
```

```latex
\end{document}
```

```
(\end occurred when \iftrue on line 4 was incomplete)
(\end occurred when \ifnum on line 4 was incomplete)
```

### File `<name>' already exists on the system. Not generating it from this source

```latex
\documentclass{article}
\begin{document}
    \begin{filecontents}{included.tex}
    \end{filecontents}
\end{document}
```

```
LaTeX Warning: File `included.tex' already exists on the system.
               Not generating it from this source.
```

### Float too large for page by <value>

```latex
\documentclass{article}
\usepackage{graphicx}
\begin{document}
    \begin{figure}
        \begin{center}
            \includegraphics[width=\textwidth]{fig.pdf}
            \caption[Short caption]{Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text Long text}
        \end{center}
    \end{figure}
\end{document}
```

```
LaTeX Warning: Float too large for page by 5.92273pt on input line 9.
```

### Font shape <font shape> in size <size> not available

```latex
\documentclass{article}
\begin{document}
    \fontsize{42pt}{50pt}
    Text.
\end{document}
```

```
LaTeX Font Warning: Font shape `OT1/cmr/m/n' in size <42> not available
(Font)              size <24.88> substituted on input line 5.

LaTeX Font Warning: Size substitutions with differences
(Font)              up to 17.12pt have occurred.
```

### Font shape <font shape> undefined. Using `<other shape>' instead

```latex
\documentclass{article}
\begin{document}
    \fontseries{b}\ttfamily Text.
\end{document}
```

```
LaTeX Font Warning: Font shape `OT1/cmtt/b/n' undefined
(Font)              using `OT1/cmtt/m/n' instead on input line 3.

LaTeX Font Warning: Some font shapes were not available, defaults substituted.
```

### amsmath: Foreign command <command>; \frac or \genfrac should be used instead

```latex
\documentclass{article}
\usepackage{amsmath}
\begin{document}
    $\primfrac{}{}$
\end{document}
```

```
Package amsmath Warning: Foreign command \;
(amsmath)                \frac or \genfrac should be used instead
(amsmath)                 on input line 4.
```

### Form feed has been converted to Blank Line

```latex
\documentclass{article}
\begin{document}
    \begin{filecontents}{filecontents.tex}

    \end{filecontents}
\end{document}
```

```
LaTeX Warning: Writing file `./filecontents.tex'.


LaTeX Warning: Writing text `    ' before \end{filecontents}
               as last line of filecontents.tex on input line 5.


LaTeX Warning: Form Feed has been converted to Blank Line.
```

### `h' float specifier changed to `ht'

```latex
\documentclass{article}
\usepackage{graphicx}
\begin{document}
    \begin{figure}[h]
        \includegraphics{fig.pdf}
    \end{figure}
\end{document}
```

```
Overfull \hbox (252.50682pt too wide) in paragraph at lines 5--6
[][]

LaTeX Warning: Float too large for page by 295.04504pt on input line 6.


LaTeX Warning: `h' float specifier changed to `ht'.
```

### Ignoring text `<text>' after \end{<env>}

```latex
\begin{filecontents}{filecontents2.tex}
\end{filecontents} Text.
\documentclass{article}
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: Writing file `./filecontents2.tex'.


LaTeX Warning: Ignoring text ` Text.' after \end{filecontents} on input line 2.
```

### Label `<key>' multiply defined

```latex
\documentclass{article}
\begin{document}
    Text.
    \label{mylabel}
    Text.
    \label{mylabel}
\end{document}
```

```
LaTeX Warning: Label `mylabel' multiply defined.

LaTeX Warning: There were multiply-defined labels.
```

### Label(s) may have changed. Rerun to get cross-references right

```latex
% https://tex.stackexchange.com/a/169245/98850
\documentclass{article}

\makeatletter

\begin{document}
\providecommand\r@foo{{1}{1}}
\edef\@currentlabel{.\expandafter\@firstoftwo\r@foo}
\label{foo}
a

\end{document}
```

```
LaTeX Warning: Label(s) may have changed. Rerun to get cross-references right.
```

### Loose \hbox (badness <number>) <somewhere>

```latex
% https://tex.stackexchange.com/q/496596/98850
\documentclass{article}

\begin{document}
    \hbadness=-1 % to report the badness
    \spaceskip.3333em \rightskip0pt plus20pt % allow only 20pt of stretchability
    \def\text{The badness of this line is 1000.}
    \setbox0=\hbox{\text}
    \hsize=\wd0 \advance\hsize by 0.1pt \noindent\text\break
    \end
    Text.
\end{document}
```

```
Loose \hbox (badness 0) in paragraph at lines 9--12
\OT1/cmr/m/n/10 The badness of this line is 1000.
```

### Marginpar on page <number> moved

```latex
\documentclass{article}

\begin{document}
    \marginpar{Text.} \marginpar{Text.}
\end{document}
```

```
LaTeX Warning: Marginpar on page 1 moved.


LaTeX Warning: Marginpar on page 1 moved.
```

### Missing character: There is no <char> in font <name>!

```latex
\documentclass{article}
\tracingonline1
\begin{document}
    \symbol{1}
\end{document}
```

```
Missing character: There is no ^^A in font [lmroman10-regular]:mapping=tex-text
;!
```

### No \author given

```latex
\documentclass{article}
\title{}
\begin{document}
    \maketitle
\end{document}
```

```
LaTeX Warning: No \author given.
```

### No auxiliary output files

```latex
\documentclass{article}
\nofiles
\begin{document}
    Text.
\end{document}
```

```
(./errors.tex
LaTeX2e <2019-10-01> patch level 3
(/home/user/texlive/2019/texmf-dist/tex/latex/base/article.cls
Document Class: article 2019/10/25 v1.4k Standard LaTeX document class
(/home/user/texlive/2019/texmf-dist/tex/latex/base/size10.clo))
No auxiliary output files.

No file errors.aux.
[1] )
```

### No characters defined by input encoding change to <name>



### No file <name>

```latex
% First delete .aux file
\documentclass{article}
\begin{document}
    Text.
\end{document}
```

```
No file errors.aux.
```

### babel: No hyphenation patterns were loaded for the language `<language>'

```latex
\documentclass{article}
\usepackage[german]{babel}
\begin{document}
    Text.
\end{document}
```

```
Package babel Warning: No hyphenation patterns were preloaded for
(babel)                the language `German (trad. orthography)' into the forma
t.
(babel)                Please, configure your TeX system to add them and
(babel)                rebuild the format. Now I will use the patterns
(babel)                preloaded for english instead on input line 58.
```

### babel: No input encoding specified for <language> language

```latex
% Install babel-russian
\documentclass[12pt]{article}

\usepackage[english,russian]{babel}
\usepackage[T1,T2A]{fontenc}
\usepackage[utf8]{inputenc}

\begin{document}

\section{Здравствуйте}

Здравствуйте! Как у вас дела? Меня зовут Калеб. Как вас зовут?

\end{document}
```

```
Package babel Warning: No Cyrillic font encoding has been loaded so far.
(babel)                A font encoding should be declared before babel.
(babel)                Default `T2A' encoding will be loaded  on input line 74.
```

### No positions in optional float specifier. Default added ...

```latex
\documentclass{article}

\begin{document}
    \begin{figure}[]

    \end{figure}
\end{document}
```

```
LaTeX Warning: No positions in optional float specifier.
               Default added (so using `tbp') on input line 5.
```

### textcomp: Oldstyle digits unavailable for family <name>

```latex
\documentclass{article}
\usepackage[warn]{textcomp}
\begin{document}
    \fontfamily{phv}\selectfont Arno \oldstylenums{text}
\end{document}
```

```
Package textcomp Warning: Oldstyle digits unavailable for family phv.
(textcomp)                Lining digits used instead on input line 4.
```

### Optional argument of \twocolumn too tall on page <number>

```latex
\documentclass{article}
\usepackage{lipsum}
\begin{document}
    \twocolumn[\lipsum]
\end{document}
```

```
LaTeX Warning: Optional argument of \twocolumn too tall on page 1.


Overfull \vbox (30.0pt too high) has occurred while \output is active

LaTeX Warning: Text page 1 contains only floats.
```

### \oval, \circle, or \line size unavailable
### Overfull \hbox (<number>pt too wide) <somewhere>
### Overfull \vbox (<number>pt too wide) <somewhere>

```latex
\documentclass{article}
\begin{document}
    \oval(1,1)
\end{document}
```
```
LaTeX Warning: \oval, \circle, or \line size unavailable on input line 4.


Overfull \vbox (2.99998pt too high) detected at line 4

Overfull \vbox (2.99998pt too high) detected at line 4

Overfull \hbox (2.99998pt too wide) detected at line 4


Overfull \hbox (2.99998pt too wide) detected at line 4
```

### Reference `<key>' on page <number> undefined

```latex
\documentclass{article}
\begin{document}
    \ref{test}
\end{document}
```

```
LaTeX Warning: Reference `test' on page 1 undefined on input line 3.
```

### Size substitutions with differences up to <size> have occurred

```latex
\documentclass{article}
\begin{document}
    \fontsize{100}{100} text
\end{document}
```

```
LaTeX Font Warning: Size substitutions with differences
(Font)              up to 75.12pt have occurred.
```

### Some font shapes were not available, defaults substituted

```latex
\documentclass{article}
\usepackage[T1]{fontenc}
\usepackage{amsmath}
\usepackage{stmaryrd}

\begin{document}
    \chapter{Test}
    Font substitution here : $\boldsymbol{\upomega}$
\end{document}
```

```
LaTeX Font Warning: Some font shapes were not available, defaults substituted.
```

### Tab has been converted to Blank Space

```latex
\begin{filecontents}{test}
	Text.
\end{filecontents}
\documentclass{article}

\begin{document}
	Text.
\end{document}
```

```
LaTeX Warning: Writing file `./test'.


LaTeX Warning: Tab has been converted to Blank Space.
```

### Text page <number> contains only floats

```latex
\documentclass{article}
\usepackage{lipsum}
\begin{document}
    \twocolumn[\lipsum]
\end{document}
```

```
LaTeX Warning: Text page 1 contains only floats.
```

### There were multiply-defined labels

```latex
\documentclass{article}
\begin{document}
    Text.
    \label{mylabel}
    Text.
    \label{mylabel}
\end{document}
```

```
LaTeX Warning: There were multiply-defined labels.
```

### There were undefined references.

```latex
\documentclass{article}
\begin{document}
    \ref{test}
\end{document}
```

```
LaTeX Warning: There were undefined references.
```

### Tight \hbox (badness <number>) <somewhere>
### Tight \vbox (badness <number>) <somewhere>

```latex
\documentclass{article}
\hbadness=-1
\begin{document}
    Text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text
\end{document}
```

```
Tight \hbox (badness 0) in paragraph at lines 4--5
[]\OT1/cmr/m/n/10 Text text text text text text text text text text text text t
ext text text text

Tight \hbox (badness 3) in paragraph at lines 4--5
\OT1/cmr/m/n/10 text text text text text text text text text text text text tex
t text text text text
```

### amsmath: Unable to redefine math accent <accent>



### Underfull \hbox (badness <number>) detected at line <line number>



### Underfull \hbox (badness <number>) has occurred while \output is active



### Underfull \hbox (badness <number>) in alignment at lines <line numbers>

```latex
\documentclass{article}
\begin{document}
    \begin{tabular*}{0.9\textwidth}{l}
        \hline
        Test \\
        \hline
    \end{tabular*}
\end{document}
```

```
Underfull \hbox (badness 10000) in alignment at lines 3--7
[]
```

### Underfull \hbox (badness <number>) in paragraph at lines <line numbers>

```latex
\documentclass{article}

\begin{document}
    Text.
    \\
\end{document}
```

```
Underfull \hbox (badness 10000) in paragraph at lines 4--6
```

### Unused global option(s): [<option-list>]

```latex
\documentclass[harf]{article}
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: Unused global option(s):
    [harf].
```

### Writing file `<name>'

```latex
\begin{filecontents}{test423.tex}
    Test.
\end{filecontents}
\documentclass{article}
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: Writing file `./test423.tex'.
```

### Writing text `<text>' before \end{<env>} as last line of <file>

```latex
\begin{filecontents}{test424.tex}
    Test.\end{filecontents}
\documentclass{article}
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: Writing text `    Test.' before \end{filecontents}
               as last line of test424.tex on input line 2.
```

### babel: You have more than once selected the attribute `<attrib>' for language <language>

```latex
\documentclass{article}
\usepackage[british]{babel}
\languageattribute{british}{test,test}
\begin{document}
    Text.
\end{document}
```

```
l.3 \languageattribute{british}{test,test}


Package babel Warning: You have more than once selected the attribute 'test'
(babel)                for language british. Reported on input line 3.
```

### You have requested <package-or-class> `<name>', but the <package-or-class> provides `<alternate-name>'

```latex
\begin{filecontents}{test435.sty}
\ProvidesPackage{Alternate name}
\end{filecontents}

\documentclass{article}
\usepackage{test435}
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: You have requested package `test435',
               but the package provides `Alternate name'.
```

### You have requested release `<date>' of LaTeX, but only release `<old-date>' is available

```latex
\documentclass{article}
\NeedsTeXFormat{LaTeX2e}[9999/99/99]
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: You have requested release `9999/99/99' of LaTeX,
               but only release `2019-10-01' is available.
```

### You have requested, on line <num>, version `<date>' of <name>, but only version `<old-date>' is available

```latex
\begin{filecontents}{test998.sty}
\ProvidesPackage{test998}[2020/04/08]
\end{filecontents}
\documentclass{article}
\usepackage{test998}[9999/99/99]
\begin{document}
    Text.
\end{document}
```

```
LaTeX Warning: You have requested, on input line 5, version
               `9999/99/99' of package test998,
               but only version
               `2020/04/08'
               is available.
```

### pdfTeX warning

```latex
\documentclass{article}
\usepackage{hyperref}

\begin{document}

    \hyperlink{summary}{summary}

\end{document}
```

```
(/home/thomas/GitRepos/random-tex/out/main.aux) )pdfTeX warning (dest): name{su
mmary} has been referenced but does not exist, replaced by a fixed one

</home/thomas/texlive/2020/texmf-dist/fonts/type1/public/amsfonts/cm/cmr10.pfb>
Output written on /home/thomas/GitRepos/random-tex/out/main.pdf (1 page, 12113
bytes).
```

# BibTeX

Test file:

`references.bib`
```bibtex
@Book{knuth1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}
```

The following errors and warnings were extracted from bibtex.web, availabe for example at [http://tug.org/svn/texlive/trunk/Build/source/texk/web2c/bibtex.web?view=markup](http://tug.org/svn/texlive/trunk/Build/source/texk/web2c/bibtex.web?view=markup)

## Errors

### I couldn't open database file <file>
### I couldn't open style file <file>

```latex
\documentclass{article}
\begin{document}
    \cite{knuth19902}.
    \bibliography{references34}
    \bibliographystyle{plain}
\end{document}
```

```
This is BibTeX, Version 0.99d (TeX Live 2020)
The top-level auxiliary file: bibtex-mwe.aux
I couldn't open database file references34.bib
---line 3 of file bibtex-mwe.aux
 : \bibdata{references34
 :                      }
I'm skipping whatever remains of this command
The style file: plain.bst
I found no database files---while reading file bibtex-mwe.aux
Warning--I didn't find a database entry for "knuth19902"
(There were 2 error messages)

Process finished with exit code 2
```

### Sorry---you've exceeded BibTeX's <structure>

```latex
% https://tex.stackexchange.com/questions/460183/bibtex-hash-size-exceeded
```

```
Database file #3: crypto.bib
Sorry---you've exceeded BibTeX's hash size 100000
Aborted at line 291526 of file crypto.bib
(That was a fatal error)
```

### This database file appears more than once:

```latex
\documentclass{article}
\begin{document}
    \cite{knuth19902}.
    \bibliography{references,references}
    \bibliographystyle{plain}
\end{document}
```

```
This database file appears more than once: references.bib
---line 3 of file bibtex-mwe.aux
 : \bibdata{references,references
 :                               }
I'm skipping whatever remains of this command
```

### <reason of confusion>---this can't happen

### Illegal, another \bibdata command
### Illegal, another \bibstyle command

```latex
\documentclass{article}
\begin{document}
    \cite{knuth1990}.
    \bibliography{references}
    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
```

```
Illegal, another \bibdata command---line 4 of file bibtex-mwe.aux
 : \bibdata
 :         {references}
I'm skipping whatever remains of this command
```

### No "}"
This one complains when a command is missing its |right_brace|.

### Stuff after "}"

```latex
% https://tex.stackexchange.com/a/408548/98850
```

```
Stuff after "}"---line 2 of file strange.aux
 : \citation{a{
 :             }b$}
```

### White space in argument

```bibtex
@Book{knuth 1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}
```

```
White space in argument---line 2 of file bibtex-mwe.aux
 : \citation{knuth
 :                 1990}
I'm skipping whatever remains of this command
```

### Case mismatch error between cite keys <key> and <key>

```latex
\documentclass{article}
\begin{document}
    \cite{knuth1990}.
    \cite{Knuth1990}.
    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
```

```
Case mismatch error between cite keys Knuth1990 and knuth1990
---line 3 of file bibtex-mwe.aux
 : \citation{Knuth1990
 :                    }
I'm skipping whatever remains of this command
```

### <file> has a wrong extension

### Already encountered file <file>

```latex
\documentclass{article}
\begin{document}
    \cite{knuth1990}.
    \include{included}
    \include{included}
    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
```

```
Already encountered file included.aux
---line 4 of file bibtex-mwe.aux
 : \@input{included.aux
 :                     }
I'm skipping whatever remains of this command
```

### I couldn't open auxiliary file <file>

```
I couldn't open auxiliary file section.aux
---line 3 of file includetest.aux
 : \@input{section.aux
 :                    }
I'm skipping whatever remains of this command
```

### I found no <type> while reading file <file>

```latex
\documentclass{article}
\begin{document}
    \cite{knuth1990}.
%    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
```

```
The style file: plain.bst
I found no \bibdata command---while reading file bibtex-mwe.aux
Warning--I didn't find a database entry for "knuth1990"
(There was 1 error message)
```

### <char> is missing in command: <command>

```latex
% https://github.com/CarlOrff/apalike-german/issues/1
```

```
"}" is missing in command: macro---line 767 of file apalike-german.bst
 : macro {mar} {"M\"
 :                  {a}rz"}
```

### <function> is already a type "<type>" function name

```latex
% https://tex.stackexchange.com/questions/147607/article-is-already-a-type-wizard-defined-function-name
```

```
article is already a type "wizard-defined" function name
---line 592 of file plainyr_my.bst
 : function {article
 :                  }
Database file #1: Publications.bib
(There was 1 error message)
```

### <something> is an unknown function
### <something> has bad function type

```latex
% https://tex.stackexchange.com/questions/329696/bibtex-error-1-is-an-integra-literal-not-a-string
```

```
The style file: bibtex/harvardUK.bst url: is an unknown function---line 320 of file bibtex/harvardUK.bst
```

### Curse you, wizard, before you recurse me: function <function> is illegal in its own definition

```latex
% https://tex.stackexchange.com/questions/552323/how-to-show-at-most-three-authors-for-any-bibliographic-entry
```

```
The top-level auxiliary file: thesis_main.aux
The style file: utphys_custom_threeAuthors.bst
function is an unknown function---line 415 of file utphys_custom_threeAuthors.bst
format.authors is an unknown function---line 415 of file utphys_custom_threeAuthors.bst
Curse you, wizard, before you recurse me:
function format.names is illegal in its own definition
---line 418 of file utphys_custom_threeAuthors.bst
```

### A bad cross reference--entry "<key>" refers to entry "<key>", which doesn't exist

```bibtex
@Book{knuth1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
    crossref = {nothing},
}
```

```
A bad cross reference---entry "knuth1990"
refers to entry "nothing", which doesn't exist
Warning--I didn't find a database entry for "nothing"
(There was 1 error message)
```

### The literal stack isn't empty for entry <key>



### Too many commas in name

```bibtex
@Book{knuth1990,
    author = {D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth},
    title = {The {\TeX} book },
    year = {1990},
    isbn = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}
```

```
Database file #1: references.bib
Too many commas in name 1 of "D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth, D.E. Knuth" for entry knuth1990
while executing---line 1049 of file plain.bst
```

## Warnings

### I didn't find a database entry for "<reference>"

```latex
\documentclass{article}
\begin{document}
    \cite{knuth19902}.
    \bibliography{references}
    \bibliographystyle{plain}
\end{document}
```

```
Database file #1: references.bib
Warning--I didn't find a database entry for "knuth19902"
(There was 1 warning)

Process finished with exit code 0
```

### I'm ignoring <something>

```bibtex
@Book{knuth1990,
    author    = {Knuth, Donald E.},
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}
```

```
Database file #1: references.bib
Warning--I'm ignoring knuth1990's extra "author" field
--line 5 of file references.bib
(There was 1 warning)
```

### entry type for "<key>" isn't style-file defined

```bibtex
@online{knuth1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
}
```

```
Warning--entry type for "knuth1990" isn't style-file defined
--line 3 of file references.bib
```

### You've nested cross references

```bibtex
@Book{knuth1990,
    author    = {Knuth, Donald E.},
    title     = {The {\TeX}book },
    year      = {1990},
    isbn      = {0-201-13447-0},
    publisher = {Addison\,\textendash\,Wesley},
    crossref = {greenwade1993},
}

@Article{greenwade1993,
    author  = "George D. Greenwade",
    title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
    year    = "1993",
    journal = "TUGBoat",
    volume  = "14",
    number  = "3",
    pages   = "342--351",
    note    = mytext,
    crossref = {goossens1993},
}
```

```
Database file #1: references.bib
Warning--you've nested cross references--entry "knuth1990"
refers to entry "greenwade1993", which also refers to something
Warning--can't use both volume and number fields in knuth1990
(There were 2 warnings)
```

### <string> isn't a brace-balanced string for entry <key>

http://g2pc1.bu.edu/~jpaley/Thesis/src/thesis-ss.blg

```
Warning--"{" isn't a brace-balanced string for entry BTRAF
while executing--line 939 of file prsty.bst
```

### I didn't find any fields

```latex
\documentclass{article}
\begin{document}
    \cite{knuth1990}.
    \bibliography{references}
    \bibliographystyle{style}
\end{document}
```

`style.bst`: `ENTRY{}{}{}`

```
The style file: style.bst
Warning--I didn't find any fields--line 1 of file style.bst
(There was 1 warning)
```

### string name "<name>" is undefined

```bibtex
@Article{greenwade1993,
    author  = ``George D. Greenwade'',
    title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
    year    = "1993",
    journal = "TUGBoat",
    volume  = "14",
    number  = "3",
    pages   = "342--351",
    note    = mytext,
}
```

```
Database file #1: references.bib
Warning--string name "``george" is undefined
--line 12 of file references.bib
I was expecting a `,' or a `}'---line 12 of file references.bib
 :     author  = ``george
 :                        D. Greenwade'',
I'm skipping whatever remains of this entry
```

## References
* Frank Mittelbach, Michel Goossens, Johannes Braams, and Chris Rowley. 2004. _The Latex Companion. 2nd ed._ Boston: Addison-Wesley. {#mittelbach2004}
* User’s Guide for the amsmath Package. _http://mirrors.ctan.org/macros/latex/required/amsmath/amsldoc.pdf_ {#amsmath}
