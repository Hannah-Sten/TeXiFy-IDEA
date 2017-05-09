package nl.rubensten.texifyidea.grammar;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static nl.rubensten.texifyidea.psi.LatexTypes.*;
%%

%{
  private boolean startedInlineMath = false;

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
OPEN_BRACKET="["
CLOSE_BRACKET="]"
M_OPEN_BRACKET="["
M_CLOSE_BRACKET="]"
OPEN_BRACE="{"
CLOSE_BRACE="}"

WHITE_SPACE=[ \t\n\x0B\f\r]+
BEGIN_TOKEN="\\begin"
END_TOKEN="\\end"
COMMAND_TOKEN=\\([a-zA-Z]+|.|\n|\r)
COMMENT_TOKEN=%[^\r\n]*
NORMAL_TEXT=[^\\{}%\[\]$]+

%states INLINE_MATH DISPLAY_MATH
%%
{WHITE_SPACE}        { return com.intellij.psi.TokenType.WHITE_SPACE; }

"\\["                { yybegin(DISPLAY_MATH); return DISPLAY_MATH_START; }
"\\]"                { return DISPLAY_MATH_END; }

<YYINITIAL> {
    "$"                { yybegin(INLINE_MATH); return INLINE_MATH_START; }
}

<INLINE_MATH> {
    {M_OPEN_BRACKET}   { return M_OPEN_BRACKET; }
    {M_CLOSE_BRACKET}  { return M_CLOSE_BRACKET; }
    "$"                { yybegin(YYINITIAL); return INLINE_MATH_END; }
}

<DISPLAY_MATH> {
    {M_OPEN_BRACKET}   { return M_OPEN_BRACKET; }
    {M_CLOSE_BRACKET}  { return M_CLOSE_BRACKET; }
}

"*"                  { return STAR; }
"["                  { return OPEN_BRACKET; }
"]"                  { return CLOSE_BRACKET; }
"{"                  { return OPEN_BRACE; }
"}"                  { return CLOSE_BRACE; }

{WHITE_SPACE}        { return WHITE_SPACE; }
{BEGIN_TOKEN}        { return BEGIN_TOKEN; }
{END_TOKEN}          { return END_TOKEN; }
{COMMAND_TOKEN}      { return COMMAND_TOKEN; }
{COMMENT_TOKEN}      { return COMMENT_TOKEN; }
{NORMAL_TEXT}        { return NORMAL_TEXT; }

[^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
