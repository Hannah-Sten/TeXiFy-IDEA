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
COMMENT_TOKEN=%[^\r\n]*
NORMAL_TEXT_WORD=[^\s\\{}%\[\]$\(\)]+

%states INLINE_MATH INLINE_MATH_LATEX DISPLAY_MATH
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

<INLINE_MATH> {
    "$"                         { yypopState(); return INLINE_MATH_END; }
}

<INLINE_MATH_LATEX> {
    {ROBUST_INLINE_MATH_END}    { yypopState(); return INLINE_MATH_END; }
}

<DISPLAY_MATH> {
    {M_OPEN_BRACKET}   { return M_OPEN_BRACKET; }
    {M_CLOSE_BRACKET}  { return M_CLOSE_BRACKET; }
    "\\]"              { yypopState(); return DISPLAY_MATH_END; }
}

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
{COMMENT_TOKEN}      { return COMMENT_TOKEN; }
{NORMAL_TEXT_WORD}   { return NORMAL_TEXT_WORD; }

[^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
