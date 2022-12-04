package nl.hannahsten.texifyidea.grammar;

import com.intellij.lexer.FlexLexer;
import java.util.*;

import com.intellij.psi.tree.IElementType;
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic;

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

  /**
   * In order to avoid a parsing error for new environment definitions, this keeps track of the number of braces in the \newenvironment
   * parameters, so as to know when the parameters are exited.
   */
  private int newEnvironmentBracesNesting = 0;

  /**
   * Also keep track of brackets of verbatim environment optional arguments.
   */
  private int verbatimOptionalArgumentBracketsCount = 0;

  /**
   * Keep track of braces in the PARTIAL_DEFINITION state.
   * We need to count braces in order to avoid exiting the state too early, especially in case of entering this state incorrectly
   * (for example because someone has >{ in their text for whatever reason).
   */
  private int preambleOptionBracesCount = 0;

  /**
   * Remember the delimiter that inline verbatim started with, to check when to end it.
   */
  private String verbatim_delimiter = "";
%}

%public
%class LatexLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

ROBUST_INLINE_MATH_START="\\("
ROBUST_INLINE_MATH_END="\\)"
OPEN_BRACKET="["
CLOSE_BRACKET="]"
OPEN_BRACE="{"
CLOSE_BRACE="}"
OPEN_PAREN="("
CLOSE_PAREN=")"

SINGLE_WHITE_SPACE=[ \t\n\x0B\f\r]
WHITE_SPACE={SINGLE_WHITE_SPACE}+

// Commands
BEGIN_TOKEN="\\begin"
END_TOKEN="\\end"
COMMAND_IFNEXTCHAR=\\@ifnextchar.
COMMAND_TOKEN=\\([a-zA-Z@]+|.|\r)
COMMAND_TOKEN_LATEX3=\\([a-zA-Z@_:0-9]+|.|\r) // _ and : are only LaTeX3 syntax
LATEX3_ON=\\(ExplSyntaxOn|ProvidesExplPackage)
LATEX3_OFF=\\ExplSyntaxOff
NEWENVIRONMENT=\\(re)?newenvironment
NEWDOCUMENTENVIRONMENT=\\(New|Renew|Provide|Declare)DocumentEnvironment

// Verbatim commands which will be delimited by the same character
// \path from the 'path' package
PLAIN_VERBATIM_COMMAND=\\verb | \\verb\* | \\path

// Verbatim commands which can also have normal optional/required parameters (or a same-character delimiter)
VERBATIM_COMMAND=\\directlua | \\luaexec | \\lstinline
 // These can contain unescaped % for example.
 | \\url | \\href
 // PythonTex Python code commands
 | \\py | \\pyb | \\pyc | \\pys | \\pyv

 KNITR_START = <<.*>>=
 KNITR_END = @

// Commands which are partial definitions, in the sense that they define only the begin or end of a pair of definitions, and thus can contain \begin commands without \end, or single $
PARTIAL_DEFINITION_COMMAND=(\\pretitle|\\posttitle|\\preauthor|\\postauthor|\\predate|\\postdate)

// Comments
MAGIC_COMMENT_PREFIX=("!"|" !"[tT][eE][xX])
COMMENT_TOKEN=%[^\r\n]*
MAGIC_COMMENT_TOKEN="%"{MAGIC_COMMENT_PREFIX}[^\r\n]*

MAGIC_COMMENT_LEXER_SWITCH="%"{MAGIC_COMMENT_PREFIX} {WHITE_SPACE}? "parser" {WHITE_SPACE}? "=" {WHITE_SPACE}?
LEXER_OFF_TOKEN={MAGIC_COMMENT_LEXER_SWITCH} "off" [^\r\n]*
ENDINPUT=\\endinput
LEXER_ON_TOKEN={MAGIC_COMMENT_LEXER_SWITCH} "on" [^\r\n]*

NORMAL_TEXT_WORD=[^\s\\\{\}%\[\]$\(\)|!\"=&<>,-]+
// Separate from normal text, e.g. because they can be \verb delimiters or should not appear in normal text words for other reasons
ANY_CHAR=[^]

// Exclude some characters from possible verbatim delimiters. This is only a workaround because some things are impossible with our lexer.
// As an example, consider the case when \verb is redefined to be a non-verbatim command, then the lexer will find all sorts of wrong delimiters, and can apparently bring the parser into an infinite loop (not sure how).
// IMPORTANT: these characters need to be included in the exlusive INLINE_VERBATIM_START state
VERBATIM_DELIMITER=[^}\\\]\[\(]

// Algorithmicx
// Currently we just use the begin..end structure for formatting, so there is no need to disinguish between separate constructs
BEGIN_PSEUDOCODE_BLOCK="\\For" | "\\ForAll" | "\\If" | "\\While" | "\\Repeat" | "\\Loop" | "\\Function" | "\\Procedure"
MIDDLE_PSEUDOCODE_BLOCK="\\ElsIf" | "\\Else"
END_PSEUDOCODE_BLOCK="\\EndFor" | "\\EndIf" | "\\EndWhile" | "\\Until" | "\\EndLoop" | "\\EndFunction" | "\\EndProcedure"

%states INLINE_MATH INLINE_MATH_LATEX DISPLAY_MATH TEXT_INSIDE_INLINE_MATH NESTED_INLINE_MATH PARTIAL_DEFINITION
%states NEW_ENVIRONMENT_DEFINITION_NAME NEW_ENVIRONMENT_DEFINITION NEW_ENVIRONMENT_SKIP_BRACE NEW_ENVIRONMENT_DEFINITION_END NEW_DOCUMENT_ENV_DEFINITION_NAME NEW_DOCUMENT_ENV_DEFINITION_ARGS_SPEC

// latex3 has some special syntax
%states LATEX3

// Every inline verbatim delimiter gets a separate state, to avoid quitting the state too early due to delimiter confusion
// States are exclusive to avoid matching expressions with an empty set of associated states, i.e. to avoid matching normal LaTeX expressions
%xstates INLINE_VERBATIM_PLAIN_START INLINE_VERBATIM INLINE_VERBATIM_NORMAL_START

%states POSSIBLE_VERBATIM_BEGIN VERBATIM_OPTIONAL_ARG VERBATIM_START VERBATIM_END INLINE_VERBATIM_OPTIONAL_ARG
%xstates VERBATIM POSSIBLE_VERBATIM_OPTIONAL_ARG POSSIBLE_VERBATIM_END

// algorithmic environment
%states PSEUDOCODE POSSIBLE_PSEUDOCODE_END

%xstates OFF

%%
{WHITE_SPACE}           { return com.intellij.psi.TokenType.WHITE_SPACE; }

/*
 * Inline verbatim
 */

// Use a separate state to start verbatim, to be able to return a command token for \verb
{PLAIN_VERBATIM_COMMAND}  { yypushState(INLINE_VERBATIM_PLAIN_START); return COMMAND_TOKEN; }
{VERBATIM_COMMAND}        { yypushState(INLINE_VERBATIM_NORMAL_START); return COMMAND_TOKEN; }

// This is like INLINE_VERBATIM_START, but because lstinline supports normal optional/required parameters instead of two of the same delimiters,
// it is a separate state so that other verbatim commands are unlikely to be picked up incorrectly
<INLINE_VERBATIM_NORMAL_START> {
    // Experimental syntax of \lstinline: \lstinline{verbatim}
    {OPEN_BRACE}         { yypopState(); verbatim_delimiter = "}"; yypushState(INLINE_VERBATIM); return OPEN_BRACE; }
    // lstinline can have optional arguments, and using [ as verbatim delimiter is not exactly very readable
    {OPEN_BRACKET}       { yypopState(); yypushState(INLINE_VERBATIM_OPTIONAL_ARG); verbatimOptionalArgumentBracketsCount = 1; return OPEN_BRACKET; }
    {VERBATIM_DELIMITER} { yypopState(); verbatim_delimiter = yytext().toString(); yypushState(INLINE_VERBATIM); return OPEN_BRACE; }
    // Some characters need to be specified explicitly, see comment on VERBATIM_DELIMITER
    {CLOSE_BRACE}        { yypopState(); return CLOSE_BRACE; }
    {CLOSE_BRACKET}      { yypopState(); return CLOSE_BRACKET; }
    {OPEN_PAREN}         { yypopState(); return OPEN_PAREN; }
    \\                   { yypopState(); return BACKSLASH; }
    [^]                  { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<INLINE_VERBATIM_PLAIN_START> {
    // These two are indicators of the command being not actually a verbatim command, for these type of commands
    // Example: when \path from TikZ is used it should not be interpreted as a verbatim command
    {OPEN_BRACE}         { yypopState(); return OPEN_BRACE; }
    {OPEN_BRACKET}       { yypopState(); return OPEN_BRACKET; }
    {VERBATIM_DELIMITER} { yypopState(); verbatim_delimiter = yytext().toString(); yypushState(INLINE_VERBATIM); return OPEN_BRACE; }
    // Some characters need to be specified explicitly, see comment on VERBATIM_DELIMITER
    {CLOSE_BRACE}        { yypopState(); return CLOSE_BRACE; }
    {CLOSE_BRACKET}      { yypopState(); return CLOSE_BRACKET; }
    {OPEN_PAREN}         { yypopState(); return OPEN_PAREN; }
    \\                   { yypopState(); return BACKSLASH; }
    [^]                  { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<INLINE_VERBATIM_OPTIONAL_ARG> {
    // Count brackets to know when we exited the optional argument
    {OPEN_BRACKET}      { verbatimOptionalArgumentBracketsCount++; return OPEN_BRACKET; }
    {CLOSE_BRACKET}     {
            verbatimOptionalArgumentBracketsCount--;
            if (verbatimOptionalArgumentBracketsCount == 0) { yypopState(); yypushState(INLINE_VERBATIM_NORMAL_START); }
            return CLOSE_BRACKET;
        }
}

<INLINE_VERBATIM> {
    {ANY_CHAR}          { if(yytext().toString().equals(verbatim_delimiter)) { yypopState(); return CLOSE_BRACE; } else { return RAW_TEXT_TOKEN; } }
    // Because the state is exclusive, we have to handle bad characters here as well (in case of an open \verb|... for example)
    [^]                 { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

/*
 * Verbatim environments
 */

<POSSIBLE_VERBATIM_BEGIN> {
    // Assumes the close brace is the last one of the \begin{...}, and that if a verbatim environment was detected, that this state has been left
    {CLOSE_BRACE}       { yypopState(); return CLOSE_BRACE; }
    {NORMAL_TEXT_WORD}  {
            yypopState();
            // toString to fix comparisons of charsequence subsequences with string
            if (EnvironmentMagic.verbatim.contains(yytext().toString())) {
                yypushState(VERBATIM_START);
            }
            else if (yytext().toString().equals("algorithmic")) {
                yypushState(PSEUDOCODE);
            }
            return NORMAL_TEXT_WORD;
        }
}

// Jump over the closing } of the \begin{verbatim} before starting verbatim state
<VERBATIM_START> {
    {CLOSE_BRACE}       { yypopState(); yypushState(POSSIBLE_VERBATIM_OPTIONAL_ARG); return CLOSE_BRACE; }
}

// Check if an optional argument is coming up
// If you start a verbatim with an open bracket and don't close it, this won't work
<POSSIBLE_VERBATIM_OPTIONAL_ARG> {
    {OPEN_BRACKET}      { verbatimOptionalArgumentBracketsCount = 1; yypopState(); yypushState(VERBATIM_OPTIONAL_ARG); return OPEN_BRACKET; }
    {WHITE_SPACE}       { yypopState(); yypushState(VERBATIM); return com.intellij.psi.TokenType.WHITE_SPACE; }
    {ANY_CHAR}          { yypopState(); yypushState(VERBATIM); return RAW_TEXT_TOKEN; }
}

// Handle optional parameters
<VERBATIM_OPTIONAL_ARG> {
    // Count brackets to know when we exited the optional argument
    {OPEN_BRACKET}      { verbatimOptionalArgumentBracketsCount++; return OPEN_BRACKET; }
    {CLOSE_BRACKET}     {
        verbatimOptionalArgumentBracketsCount--;
        if (verbatimOptionalArgumentBracketsCount == 0) { yypopState(); yypushState(VERBATIM); }
        return CLOSE_BRACKET;
    }
}

<VERBATIM> {
    // Also catch whitespace, see LatexParserUtil for more info
    {WHITE_SPACE}       { return com.intellij.psi.TokenType.WHITE_SPACE; }
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
    // We have to return an END_TOKEN, in case this really is the verbatim end command (we cannot backtrack)
    // The token remapper will remap to raw text
    // Unfortunately, the brace matcher uses lexer tokens, so we will assume (regarding brace matching) that whenever there is an \end in a verbatim environment, there also is a \begin, but for that to work we also need to return \begin tokens
    {END_TOKEN}         { yypushState(POSSIBLE_VERBATIM_END); return END_TOKEN; }
    {BEGIN_TOKEN}       { return BEGIN_TOKEN; }
}

// Open brace will be remapped to raw text by token remapper, if needed
// Anything else will tell us that this is not an \end{verbatim}
<POSSIBLE_VERBATIM_END> {
    {OPEN_BRACE}        { return OPEN_BRACE; }
    {NORMAL_TEXT_WORD}  {
        // Pop current state
        yypopState();
        if (EnvironmentMagic.verbatim.contains(yytext().toString())) {
            // Pop verbatim state
            yypopState();
            return NORMAL_TEXT_WORD;
        }
        return RAW_TEXT_TOKEN;
    }
    {ANY_CHAR}          { yypopState(); return RAW_TEXT_TOKEN; }
}

// Switched off by a magic comment %! parser = off
<OFF> {
    {KNITR_END}         { yypopState(); return COMMENT_TOKEN; }
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
    {LEXER_ON_TOKEN}    { yypopState(); return COMMENT_TOKEN; }
}

/*
 * algorithmic environment
 *
 * The problem is that in an algorithmic environment, \while has to match with \endwhile
 * but in general in an algorithm environment, algorithm2e could be used which matches \while{ with a closing }.
 * To support algorithmic formatting, we therefore only match \while with \endwhile in an algorithmic environment (which also solves the problem of lexing a simple \while command outside of an algorithm-like environment like a pseudocode \while).
 */

<PSEUDOCODE> {

    {BEGIN_PSEUDOCODE_BLOCK}  { return BEGIN_PSEUDOCODE_BLOCK; }
    {MIDDLE_PSEUDOCODE_BLOCK} { return MIDDLE_PSEUDOCODE_BLOCK; }
    {END_PSEUDOCODE_BLOCK}    { return END_PSEUDOCODE_BLOCK; }
    {END_TOKEN}               { yypushState(POSSIBLE_PSEUDOCODE_END); return END_TOKEN; }
}

<POSSIBLE_PSEUDOCODE_END> {
    {NORMAL_TEXT_WORD}       {
        yypopState();
        if (yytext().toString().equals("algorithmic")) {
            // Pop pseudocode state
            yypopState();
        }
        return NORMAL_TEXT_WORD;
    }
}

/*
 * \newenvironment definitions
 */

// For new environment definitions, we need to switch to new states because the \begin..\end will interleave with groups
// \newenvironment{name}{begin}{end}
// Extra required argument with args spec, so we need an extra state for that
// \NewDocumentEnvironment{name}{args spec}{start}{end}
{NEWENVIRONMENT}        { yypushState(NEW_ENVIRONMENT_DEFINITION_NAME); return COMMAND_TOKEN; }
{NEWDOCUMENTENVIRONMENT} { yypushState(NEW_DOCUMENT_ENV_DEFINITION_NAME); return COMMAND_TOKEN; }

// A separate state is used to track when we start with the second parameter of \newenvironment, this state denotes the first one
<NEW_ENVIRONMENT_DEFINITION_NAME> {
    {CLOSE_BRACE}       {
          yypopState();
          newEnvironmentBracesNesting = 0;
          yypushState(NEW_ENVIRONMENT_DEFINITION);
          return CLOSE_BRACE;
    }
}

<NEW_DOCUMENT_ENV_DEFINITION_NAME> {
    {CLOSE_BRACE}       { yypopState(); yypushState(NEW_DOCUMENT_ENV_DEFINITION_ARGS_SPEC); newEnvironmentBracesNesting = 0; return CLOSE_BRACE; }
}

// Unfortunately, the args spec can contain braces as well, so we need to keep track when we leave the required argument
<NEW_DOCUMENT_ENV_DEFINITION_ARGS_SPEC> {
    {OPEN_BRACE}        { newEnvironmentBracesNesting++; return OPEN_BRACE; }
    {CLOSE_BRACE}       {
        newEnvironmentBracesNesting--;
        if (newEnvironmentBracesNesting <= 0) {
            yypopState();
            yypushState(NEW_ENVIRONMENT_DEFINITION);
        }
        return CLOSE_BRACE;
    }
}

// We are visiting a second parameter of a \newenvironment definition, so we need to keep track of braces
// The idea is that we will skip the }{ separating the second and third parameter, so that the \begin and \end of the
// environment to be defined will not appear in a separate group
// Include possible verbatim begin state, because after a \begin we are in that state (and we cannot leave it because we might be needing to start a verbatim environment)
// but we still need to count braces (specifically, the open brace after \begin)
<NEW_ENVIRONMENT_DEFINITION,POSSIBLE_VERBATIM_BEGIN> {
    {OPEN_BRACE}       { newEnvironmentBracesNesting++; return OPEN_BRACE; }
    {CLOSE_BRACE}      {
        newEnvironmentBracesNesting--;
        if(newEnvironmentBracesNesting == 0) {
            yypopState(); yypushState(NEW_ENVIRONMENT_SKIP_BRACE);
            // We could have returned normal text, but in this way the braces still match
            return OPEN_BRACE;
        } else {
            return CLOSE_BRACE;
        }
    }
    // To avoid changing state and thus tripping over the not matching group }{ in the middle, catch characters here which would otherwise change state
    "\\["               { return DISPLAY_MATH_START; }
    "\\]"               { return DISPLAY_MATH_END; }
    "$"                 { return NORMAL_TEXT_WORD; }
}

// Skip the next open brace of the third parameter, just as we skipped the close brace of the second
<NEW_ENVIRONMENT_SKIP_BRACE> {
    {OPEN_BRACE}        { yypopState(); newEnvironmentBracesNesting = 1; yypushState(NEW_ENVIRONMENT_DEFINITION_END); return CLOSE_BRACE; }
}

// In the third parameter, still skip the state-changing characters
<NEW_ENVIRONMENT_DEFINITION_END> {
    {OPEN_BRACE}        { newEnvironmentBracesNesting++; return OPEN_BRACE; }
    {CLOSE_BRACE}       {
        newEnvironmentBracesNesting--;
        if(newEnvironmentBracesNesting == 0) {
            yypopState();
        }
        return CLOSE_BRACE;
    }
    "\\["               { return DISPLAY_MATH_START; }
    "\\]"               { return DISPLAY_MATH_END; }
    "$"                 { return NORMAL_TEXT_WORD; }
}


/*
 * Inline math, display math and nested inline math
 */

"\\["                   { yypushState(DISPLAY_MATH); return DISPLAY_MATH_START; }

// We have to explicitly specify in which states the $ starts an inline math,
// because definitely not in all states this should be the case (like inline math)
<YYINITIAL,DISPLAY_MATH,PSEUDOCODE> {
    "$"                             { yypushState(INLINE_MATH); return INLINE_MATH_START; }
    {ROBUST_INLINE_MATH_START}      { yypushState(INLINE_MATH_LATEX); return INLINE_MATH_START; }
}

<NESTED_INLINE_MATH> {
    "$"                 { yypopState(); return INLINE_MATH_END; }
}

<INLINE_MATH> {
    "$"                 { yypopState(); return INLINE_MATH_END; }
    // When already in inline math, when encountering a \text command we need to switch out of the math state
    // because if we encounter another $, then it will be an inline_math_start, not an inline_math_end
    \\text              { yypushState(TEXT_INSIDE_INLINE_MATH); return COMMAND_TOKEN; }
}

// When in a \text in inline math, either start nested inline math or close the \text
<TEXT_INSIDE_INLINE_MATH> {
    "$"                 { yypushState(NESTED_INLINE_MATH); return INLINE_MATH_START; }
    {CLOSE_BRACE}       { yypopState(); return CLOSE_BRACE; }
}

<INLINE_MATH_LATEX> {
    {ROBUST_INLINE_MATH_END}    { yypopState(); return INLINE_MATH_END; }
}

// In this situation, we are defining one side of a pair of definitions, so an unmatched \begin or single $ is perfectly fine and is hence ignored. We rely on braces to check when we exit the definition.
// We do this at lexer level, because we do want to keep an unmatched \begin a parse error outside these (rare) special cases
<PARTIAL_DEFINITION> {
    "$"                 { return NORMAL_TEXT_WORD; }
    {BEGIN_TOKEN}       { return COMMAND_TOKEN; }
    {END_TOKEN}         { return COMMAND_TOKEN; }
    {OPEN_BRACE}        { preambleOptionBracesCount++; return OPEN_BRACE; }
    {CLOSE_BRACE}       {
        if (preambleOptionBracesCount == 0) {
          yypopState();
        }
        else {
            preambleOptionBracesCount--;
        }
        return CLOSE_BRACE;
    }
}

<DISPLAY_MATH> {
    "\\]"               { yypopState(); return DISPLAY_MATH_END; }
}

{LATEX3_ON}                 { yypushState(LATEX3); return COMMAND_TOKEN; }
<LATEX3> {
    {LATEX3_OFF}            { yypopState(); return COMMAND_TOKEN; }
    {COMMAND_TOKEN_LATEX3}  { return COMMAND_TOKEN; }
}

/*
 * Other elements
 */

// The array package provides <{...} and >{...} preamble options for tables
// which are often used with $, in which case the $ is not an inline_math_start (because there's only one $ in the group, which would be a parse errror)
// or a >{\begin{env}l<{\end{env}} structure
{PARTIAL_DEFINITION_COMMAND}                   { yypushState(PARTIAL_DEFINITION); preambleOptionBracesCount = -1; return COMMAND_TOKEN; } // -1 because the { is not included in the regex
\<\{                   { yypushState(PARTIAL_DEFINITION); preambleOptionBracesCount = 0; return OPEN_BRACE; }
>\{                    { yypushState(PARTIAL_DEFINITION); preambleOptionBracesCount = 0; return OPEN_BRACE; }
// Unfortunately, some packages decided that it's a good idea to use angular brackets as parameters, which can lead to the case where an angular parameter is followed by a required parameter, such that we have the >{...} structure again but now with a completely different meaning.
// In order to support inline math in the required parameter, we need to detect we are not in a tabular preamble, which is doable but a lot of overhead.
// Instead, since angular bracket parameters are rare and are mostly just beamer overlay parameters and a few TikZ cases, we hardcode a regex for those instead so that the above rules will not match.
\<[\d\-,\+]+>          { return ANGLE_PARAM; }

// In case a backslash is not a command, probably because  a line ends with a backslash, then we do not want to lex the following newline as a command token,
// because that will confuse the formatter because it will see the next line as being on this line
\\                    { return BACKSLASH; }

"*"                     { return STAR; }
// A separate token, used for example for aligning & in tables
"&"                     { return AMPERSAND; }

// Tokens for special characters of which certain grammar elements might support only a few
"="                     { return EQUALS; }
","                     { return COMMA; }
"\""                    { return QUOTATION_MARK; }
"<"                     { return OPEN_ANGLE_BRACKET; }
">"                     { return CLOSE_ANGLE_BRACKET; }
"|"                     { return PIPE;}
"!"                     { return EXCLAMATION_MARK; }
[-]+                    { return DASH; } // Dashes of various length

{OPEN_BRACKET}          { return OPEN_BRACKET; }
{CLOSE_BRACKET}         { return CLOSE_BRACKET; }
{OPEN_BRACE}            { return OPEN_BRACE; }
{CLOSE_BRACE}           { return CLOSE_BRACE; }
{OPEN_PAREN}            { return OPEN_PAREN; }
{CLOSE_PAREN}           { return CLOSE_PAREN; }

{LEXER_OFF_TOKEN}       { yypushState(OFF); return COMMENT_TOKEN; }
{KNITR_START}           { yypushState(OFF); return COMMENT_TOKEN; }
{ENDINPUT}              { yypushState(OFF); return COMMAND_TOKEN; }
{BEGIN_TOKEN}           { yypushState(POSSIBLE_VERBATIM_BEGIN); return BEGIN_TOKEN; }
{END_TOKEN}             { return END_TOKEN; }
{COMMAND_TOKEN}         { return COMMAND_TOKEN; }
{COMMAND_IFNEXTCHAR}    { return COMMAND_IFNEXTCHAR; }
{MAGIC_COMMENT_TOKEN}   { return MAGIC_COMMENT_TOKEN; }
{COMMENT_TOKEN}         { return COMMENT_TOKEN; }
{NORMAL_TEXT_WORD}      { return NORMAL_TEXT_WORD; }

[^]                     { return com.intellij.psi.TokenType.BAD_CHARACTER; }
