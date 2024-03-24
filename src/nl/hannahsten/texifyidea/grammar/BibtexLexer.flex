package nl.hannahsten.texifyidea.grammar;

import java.util.*;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static nl.hannahsten.texifyidea.psi.BibtexTypes.*;

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
    private Deque<Integer> stack = new ArrayDeque<>();


    public void yypushState(int newState) {
        stack.push(yystate());
        yybegin(newState);
    }

    public void yypopState() {
        yybegin(stack.pop());
    }

    int braceCount = 0;
    boolean verbatim = false;
%}

WHITE_SPACE=([\ \t\f]|"\r"|"\n"|"\r\n")+
OPEN_BRACE="{"
CLOSE_BRACE="}"
OPEN_PARENTHESIS="("
CLOSE_PARENTHESIS=")"
SEPARATOR=","
ASSIGNMENT="="
CONCATENATE="#"
QUOTES="\""

WHITE_SPACE=[ \t\n\x0B\f\r]+
TYPE_TOKEN=@[a-zA-Z_]+
// BibTeX itself doesn't support % as a comment character (using it it the start of a line in an entry is illegal), but
// biber explicitly supports it as a comment, so we do as well.
COMMENT_TOKEN=%[^\r\n]*
// Characters disallowed by bibtex or biber (non-ascii or not depends on LaTeX compiler)
IDENTIFIER=[^,{}\(\)\"#%'=~\\ \n]+
VERBATIM_IDENTIFIER="url"
NUMBER=[0-9-]+
NORMAL_TEXT_WORD=([^\"]|\\\" )+
NORMAL_TEXT_BRACED_STRING=[^{} ]+
ANY_CHAR=[^]

%state XXAFTERTYPETOKEN
%state XXENTRY
%state XXENTRYIDENTIFIER
%state XXAFTERENTRY
%state XXSTRINGDEF
%state XXQUOTED_STRING
%state XXQUOTED_STRINGDEF
%state XXBRACED_STRING
%state XXPREAMBLE
%state XXPREAMBLE_STRING

%xstate XXCOMMENT
%xstate XXCOMMENT_STRING
%xstate XXQUOTED_VERBATIM
%xstate XXBRACED_VERBATIM
%%
{COMMENT_TOKEN}                 { return COMMENT_TOKEN; }

<XXAFTERENTRY> {
    {SEPARATOR}                 { yybegin(YYINITIAL); return SEPARATOR; }
}

<YYINITIAL,XXAFTERENTRY> {
    {TYPE_TOKEN}                { String sequence = yytext().toString();
                                  if ("@string".equalsIgnoreCase(sequence)) {
                                    yybegin(XXSTRINGDEF);
                                  }
                                  else if ("@preamble".equalsIgnoreCase(sequence)) {
                                    yybegin(XXPREAMBLE);
                                  }
                                  else if ("@comment".equalsIgnoreCase(sequence)) {
                                    yybegin(XXCOMMENT);
                                    return COMMENT_TOKEN;
                                  }
                                  else {
                                    yybegin(XXAFTERTYPETOKEN);
                                  }
                                  return TYPE_TOKEN; }
    [^\s]                       { yybegin(YYINITIAL); return COMMENT_TOKEN; }
}

<XXAFTERTYPETOKEN> {
    {OPEN_BRACE}                { yybegin(XXENTRYIDENTIFIER); return OPEN_BRACE; }
}

<XXCOMMENT> {
    {OPEN_BRACE}                { yybegin(XXCOMMENT_STRING); return COMMENT_TOKEN; }
}

<XXCOMMENT_STRING> {
    {CLOSE_BRACE}               { yybegin(XXAFTERENTRY); return COMMENT_TOKEN; }
    {ANY_CHAR}                  { return COMMENT_TOKEN; }
}

// Preamble: @preamble{ "some string" }
<XXPREAMBLE> {
    {OPEN_BRACE}                { return OPEN_BRACE; }
    {QUOTES}                    { yybegin(XXPREAMBLE_STRING); return QUOTES; }
    {NUMBER}                    { return NUMBER; }
    {CONCATENATE}               { return CONCATENATE; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {CLOSE_BRACE}               { yybegin(XXAFTERENTRY); return CLOSE_BRACE; }
}

// String in the preamble.
<XXPREAMBLE_STRING> {
    {QUOTES}                    { yybegin(XXPREAMBLE); return END_QUOTES; }
    {NORMAL_TEXT_WORD}          { return NORMAL_TEXT_WORD; }
}

// String definition: @string { name = "value" }
<XXSTRINGDEF> {
    {OPEN_BRACE}                { return OPEN_BRACE; }
    {OPEN_PARENTHESIS}          { return OPEN_PARENTHESIS; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {ASSIGNMENT}                { return ASSIGNMENT; }
    {QUOTES}                    { yybegin(XXQUOTED_STRINGDEF); return QUOTES; }
    {CLOSE_BRACE}               { yybegin(XXAFTERENTRY); return CLOSE_BRACE; }
    {CLOSE_PARENTHESIS}         { yybegin(XXAFTERENTRY); return CLOSE_PARENTHESIS; }
}

// String in string definition.
<XXQUOTED_STRINGDEF> {
    {QUOTES}                    { yybegin(XXSTRINGDEF); return END_QUOTES; }
    {NORMAL_TEXT_WORD}          { return NORMAL_TEXT_WORD; }
}

<XXENTRYIDENTIFIER> {
    {IDENTIFIER}                { yybegin(XXENTRY); return IDENTIFIER; }
}

// Complete entry.
<XXENTRY> {
    {NUMBER}                    { return NUMBER; }
    {VERBATIM_IDENTIFIER}       { verbatim = true; return VERBATIM_IDENTIFIER; }
    {IDENTIFIER}                { return IDENTIFIER; }
    {ASSIGNMENT}                { return ASSIGNMENT; }
    {OPEN_BRACE}                { if (verbatim) yypushState(XXBRACED_VERBATIM); else yybegin(XXBRACED_STRING); return OPEN_BRACE; }
    {QUOTES}                    { if (verbatim) yypushState(XXQUOTED_VERBATIM); else yybegin(XXQUOTED_STRING); return QUOTES; }
    {CONCATENATE}               { return CONCATENATE; }
    {SEPARATOR}                 { return SEPARATOR; }
    {CLOSE_BRACE}               { yybegin(XXAFTERENTRY); return CLOSE_BRACE; }
    {CLOSE_PARENTHESIS}         { yybegin(XXAFTERENTRY); return CLOSE_PARENTHESIS; }
}

// "Quoted string" in an entry.
<XXQUOTED_STRING> {
    {QUOTES}                    { yybegin(XXENTRY); return END_QUOTES; }
    {NORMAL_TEXT_WORD}          { return NORMAL_TEXT_WORD; }
}

// { Braced String }  in an entry.
<XXBRACED_STRING> {
    {OPEN_BRACE}                { braceCount++; return NORMAL_TEXT_WORD; }
    {CLOSE_BRACE}               { if (braceCount > 0) {
                                    braceCount--;
                                    return NORMAL_TEXT_WORD;
                                  }

                                  yybegin(XXENTRY);
                                  return CLOSE_BRACE; }
    {NORMAL_TEXT_BRACED_STRING} { return NORMAL_TEXT_WORD; }
}

// { Braced text } in an entry where all characters are allowed.
<XXBRACED_VERBATIM> {
    {CLOSE_BRACE}       { yypopState(); verbatim = false; return CLOSE_BRACE; }
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
}

// "Quoted text" in an entry where all characters are allowed.
<XXQUOTED_VERBATIM> {
    {QUOTES}       { yypopState(); verbatim = false; return END_QUOTES; }
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
}

{WHITE_SPACE}                   { return WHITE_SPACE; }

[^]                             { return BAD_CHARACTER; }
