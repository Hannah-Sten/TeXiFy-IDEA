package nl.hannahsten.texifyidea.grammar;

import java.util.ArrayDeque;
import java.util.Deque;

import com.intellij.psi.tree.IElementType;

import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
%%

%{
  private Deque<Integer> stack = new ArrayDeque<>();

  public void yypushState(int newState) {
    stack.push(yystate());
    yybegin(newState);
  }

  public void yypopState() {
    yybegin(stack.pop());
  }


  public LatexLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class LatexLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+
DISPLAY_MATH_START="\["
DISPLAY_MATH_END="\]"
ROBUST_INLINE_MATH_START="\\("
ROBUST_INLINE_MATH_END="\\)"
OPEN_BRACKET="["
CLOSE_BRACKET="]"
M_OPEN_BRACKET="["
M_CLOSE_BRACKET="]"
OPEN_BRACE="{"
CLOSE_BRACE="}"
OPEN_PAREN="("
CLOSE_PAREN=")"

WHITE_SPACE=[ \t\n\x0B\f\r]+
BEGIN_TOKEN="\\begin"
END_TOKEN="\\end"
COMMAND_TOKEN=\\([a-zA-Z@]+|.|\n|\r)
COMMAND_IFNEXTCHAR=\\@ifnextchar.
COMMENT_TOKEN=%[^\r\n]*
NORMAL_TEXT_WORD=[^\s\\{}%\[\]$\(\)]+

%states INLINE_MATH INLINE_MATH_LATEX DISPLAY_MATH TEXT_INSIDE_INLINE_MATH NESTED_INLINE_MATH PREAMBLE_OPTION
%%
{WHITE_SPACE}        { return com.intellij.psi.TokenType.WHITE_SPACE; }

"\\["                { yypushState(DISPLAY_MATH); return DISPLAY_MATH_START; }

<YYINITIAL,DISPLAY_MATH> {
    "$"                             { yypushState(INLINE_MATH); return INLINE_MATH_START; }
    {ROBUST_INLINE_MATH_START}      { yypushState(INLINE_MATH_LATEX); return INLINE_MATH_START; }
}

<INLINE_MATH,INLINE_MATH_LATEX> {
    {M_OPEN_BRACKET}   { return M_OPEN_BRACKET; }
    {M_CLOSE_BRACKET}  { return M_CLOSE_BRACKET; }
}

<NESTED_INLINE_MATH> {
    "$"     { yypopState(); return INLINE_MATH_END; }
}

<INLINE_MATH> {
    "$"       { yypopState(); return INLINE_MATH_END; }
    // When already in inline math, when encountering a \text command we need to switch out of the math state
    // because if we encounter another $, then it will be an inline_math_start, not an inline_math_end
    \\text    { yypushState(TEXT_INSIDE_INLINE_MATH); return COMMAND_TOKEN; }
}

// When in a \text in inline math, either start nested inline math or close the \text
<TEXT_INSIDE_INLINE_MATH> {
    "$"     { yypushState(NESTED_INLINE_MATH); return INLINE_MATH_START; }
    "}"     { yypopState(); return CLOSE_BRACE; }
}

<INLINE_MATH_LATEX> {
    {ROBUST_INLINE_MATH_END}    { yypopState(); return INLINE_MATH_END; }
}

<PREAMBLE_OPTION> {
    "$"     { return NORMAL_TEXT_WORD; }
    "}"     { yypopState(); return CLOSE_BRACE; }
}

<DISPLAY_MATH> {
    {M_OPEN_BRACKET}   { return M_OPEN_BRACKET; }
    {M_CLOSE_BRACKET}  { return M_CLOSE_BRACKET; }
    "\\]"              { yypopState(); return DISPLAY_MATH_END; }
}

// The array package provides <{...} and >{...} preamble options for tables
// which are often used with $, in which case the $ is not an inline_math_start (because there's only one $ in the group, which would be a parse errror)
// It has to be prefixed by . because any other letter before the < or > may be seen as a normal text word together with the < or >, so we need to catch them together
.\<\{                 { yypushState(PREAMBLE_OPTION); return OPEN_BRACE; }
.>\{                  { yypushState(PREAMBLE_OPTION); return OPEN_BRACE; }

"*"                  { return STAR; }
"["                  { return OPEN_BRACKET; }
"]"                  { return CLOSE_BRACKET; }
"{"                  { return OPEN_BRACE; }
"}"                  { return CLOSE_BRACE; }
{OPEN_PAREN}         { return OPEN_PAREN; }
{CLOSE_PAREN}        { return CLOSE_PAREN; }

{WHITE_SPACE}        { return WHITE_SPACE; }
{BEGIN_TOKEN}        { return BEGIN_TOKEN; }
{END_TOKEN}          { return END_TOKEN; }
{COMMAND_TOKEN}      { return COMMAND_TOKEN; }
{COMMAND_IFNEXTCHAR} { return COMMAND_IFNEXTCHAR; }
{COMMENT_TOKEN}      { return COMMENT_TOKEN; }
{NORMAL_TEXT_WORD}   { return NORMAL_TEXT_WORD; }

[^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
