package nl.rubensten.texifyidea.grammar;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static nl.rubensten.texifyidea.psi.BibtexTypes.*;

%%

%public
%class BibtexLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    int braceCount = 0;
%}

WHITE_SPACE=([\ \t\f]|"\r"|"\n"|"\r\n")+
OPEN_BRACE="{"
CLOSE_BRACE="}"
SEPARATOR=","
ASSIGNMENT="="
CONCATENATE="#"
QUOTES="\""

WHITE_SPACE=[ \t\n\x0B\f\r]+
TYPE_TOKEN=@[a-zA-Z]+
COMMENT_TOKEN=%[^\r\n]*
IDENTIFIER=[a-zA-Z0-9:+_-]+
NUMBER=[0-9]+
NORMAL_TEXT=([^\"]|\\\")+
NORMAL_TEXT_BRACED_STRING=[^{} ]+
ENDTRY=,?\s*{CLOSE_BRACE}

%state XXENTRY
%state XXSTRINGDEF
%state XXQUOTED_STRING
%state XXBRACED_STRING
%state XXPREAMBLE
%state XXPREAMBLE_STRING

%%
{COMMENT_TOKEN}                 { return COMMENT_TOKEN; }

<YYINITIAL> {
    {TYPE_TOKEN}                { String sequence = yytext().toString();
                                  if ("@string".equalsIgnoreCase(sequence)) {
                                    yybegin(XXSTRINGDEF);
                                  }
                                  else if ("@preamble".equalsIgnoreCase(sequence)) {
                                    yybegin(XXPREAMBLE);
                                  }
                                  return TYPE_TOKEN; }
    {OPEN_BRACE}                { return OPEN_BRACE; }
    {ASSIGNMENT}                { return ASSIGNMENT; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {SEPARATOR}                 { yybegin(XXENTRY); return SEPARATOR; }
}

<XXPREAMBLE> {
    {OPEN_BRACE}                { return OPEN_BRACE; }
    {QUOTES}                    { yybegin(XXPREAMBLE_STRING); return QUOTES; }
    {NUMBER}                    { return NUMBER; }
    {CONCATENATE}               { return CONCATENATE; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {CLOSE_BRACE}               { yybegin(YYINITIAL); return CLOSE_BRACE; }
}

<XXSTRINGDEF> {
    {OPEN_BRACE}                { return OPEN_BRACE; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {ASSIGNMENT}                { return ASSIGNMENT; }
    {QUOTES}                    { yybegin(XXQUOTED_STRING); return QUOTES; }
}

<XXENTRY> {
    {NUMBER}                    { return NUMBER; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {ASSIGNMENT}                { return ASSIGNMENT; }
    {OPEN_BRACE}                { yybegin(XXBRACED_STRING); return OPEN_BRACE; }
    {QUOTES}                    { yybegin(XXQUOTED_STRING); return QUOTES; }
    {CONCATENATE}               { return CONCATENATE; }
    {SEPARATOR}                 { return SEPARATOR; }
    {ENDTRY}                    { yybegin(YYINITIAL); return ENDTRY; }
}

<XXPREAMBLE_STRING> {
    {QUOTES}                    { yybegin(XXPREAMBLE); return QUOTES; }
    {NORMAL_TEXT}               { return NORMAL_TEXT; }
}

<XXQUOTED_STRING> {
    {QUOTES}                    { yybegin(XXENTRY); return QUOTES; }
    {NORMAL_TEXT}               { return NORMAL_TEXT; }
}

<XXBRACED_STRING> {
    {OPEN_BRACE}                { braceCount++; return NORMAL_TEXT; }
    {CLOSE_BRACE}               { if (braceCount > 0) {
                                    braceCount--;
                                    return NORMAL_TEXT;
                                  }

                                  yybegin(XXENTRY);
                                  return CLOSE_BRACE; }
    {NORMAL_TEXT_BRACED_STRING} { return NORMAL_TEXT; }
}

{WHITE_SPACE}                   { return WHITE_SPACE; }

[^]                             { return BAD_CHARACTER; }
