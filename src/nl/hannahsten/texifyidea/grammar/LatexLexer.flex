package nl.hannahsten.texifyidea.grammar;

import java.util.*;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import nl.hannahsten.texifyidea.util.Magic;

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
WHITE_SPACE=[ \t\n\x0B\f\r]+
BEGIN_TOKEN="\\begin"
END_TOKEN="\\end"
COMMAND_TOKEN=\\([a-zA-Z@]+|.|\r)
COMMAND_IFNEXTCHAR=\\@ifnextchar.

// Comments
COMMENT_TOKEN=%[^\r\n]*
MAGIC_COMMENT_LEXER_SWITCH="%" {WHITE_SPACE}? "!" {WHITE_SPACE}? (TeX)? {WHITE_SPACE}? "parser" {WHITE_SPACE}? "=" {WHITE_SPACE}?
LEXER_OFF_TOKEN={MAGIC_COMMENT_LEXER_SWITCH} "off" [^\r\n]*
LEXER_ON_TOKEN={MAGIC_COMMENT_LEXER_SWITCH} "on" [^\r\n]*

NORMAL_TEXT_WORD=[^\s\\{}%\[\]$\(\)|!\"=&<>]+
// Separate from normal text, e.g. because they can be \verb delimiters or should not appear in normal text words for other reasons
NORMAL_TEXT_CHAR=[|!\"=&<>]
ANY_CHAR=[^]

// Algorithmicx
// Currently we just use the begin..end structure for formatting, so there is no need to disinguish between separate constructs
BEGIN_PSEUDOCODE_BLOCK="\\For" | "\\ForAll" | "\\If" | "\\While" | "\\Repeat" | "\\Loop" | "\\Function" | "\\Procedure" |
    // Algorithm2e
    "\\uIf" | "\\eIf" | "\\lIf" | "\\leIf"
MIDDLE_PSEUDOCODE_BLOCK="\\ElsIf" | "\\Else"
END_PSEUDOCODE_BLOCK="\\EndFor" | "\\EndIf" | "\\EndWhile" | "\\Until" | "\\EndLoop" | "\\EndFunction" | "\\EndProcedure"

%states INLINE_MATH INLINE_MATH_LATEX DISPLAY_MATH TEXT_INSIDE_INLINE_MATH NESTED_INLINE_MATH PREAMBLE_OPTION
%states NEW_ENVIRONMENT_DEFINITION_NAME NEW_ENVIRONMENT_DEFINITION NEW_ENVIRONMENT_SKIP_BRACE NEW_ENVIRONMENT_DEFINITION_END
// Every inline verbatim delimiter gets a separate state, to avoid quitting the state too early due to delimiter confusion
// States are exclusive to avoid matching expressions with an empty set of associated states, i.e. to avoid matching normal LaTeX expressions
%states INLINE_VERBATIM_START
%xstates INLINE_VERBATIM_PIPE INLINE_VERBATIM_EXCL_MARK INLINE_VERBATIM_QUOTES INLINE_VERBATIM_EQUALS

%states POSSIBLE_VERBATIM_BEGIN VERBATIM_OPTIONAL_ARG VERBATIM_START VERBATIM_END
%xstates VERBATIM POSSIBLE_VERBATIM_OPTIONAL_ARG POSSIBLE_VERBATIM_END

%xstates OFF

%%
{WHITE_SPACE}           { return com.intellij.psi.TokenType.WHITE_SPACE; }

/*
 * Inline verbatim
 */

// Use a separate state to start verbatim, to be able to return a command token for \verb
\\verb                  |
\\verb\*                |
\\lstinline             { yypushState(INLINE_VERBATIM_START); return COMMAND_TOKEN; }

<INLINE_VERBATIM_START> {
    "|"                 { yypopState(); yypushState(INLINE_VERBATIM_PIPE); return OPEN_BRACE; }
    "!"                 { yypopState(); yypushState(INLINE_VERBATIM_EXCL_MARK); return OPEN_BRACE; }
    "\""                { yypopState(); yypushState(INLINE_VERBATIM_QUOTES); return OPEN_BRACE; }
    "="                 { yypopState(); yypushState(INLINE_VERBATIM_EQUALS); return OPEN_BRACE; }
}

<INLINE_VERBATIM_PIPE> {
    "|"                 { yypopState(); return CLOSE_BRACE; }
}

<INLINE_VERBATIM_EXCL_MARK> {
    "!"                 { yypopState(); return CLOSE_BRACE; }
}

<INLINE_VERBATIM_QUOTES> {
    "\""                { yypopState(); return CLOSE_BRACE; }
}

<INLINE_VERBATIM_EQUALS> {
    "="                 { yypopState(); return CLOSE_BRACE; }
}

<INLINE_VERBATIM_PIPE, INLINE_VERBATIM_EXCL_MARK, INLINE_VERBATIM_QUOTES, INLINE_VERBATIM_EQUALS> {
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
    // Because the states are exclusive, we have to handle bad characters here as well (in case of an open \verb|... for example)
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
        if (Magic.Environment.verbatim.contains(yytext().toString())) {
            yypushState(VERBATIM_START);
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
    {OPEN_BRACKET}      { verbatimOptionalArgumentBracketsCount++; yypopState(); yypushState(VERBATIM_OPTIONAL_ARG); return OPEN_BRACKET; }
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
        if (Magic.Environment.verbatim.contains(yytext().toString())) {
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
    {ANY_CHAR}          { return RAW_TEXT_TOKEN; }
    {LEXER_ON_TOKEN}    { yypopState(); return COMMENT_TOKEN; }
}

/*
 * \newenvironment definitions
 */

// For new environment definitions, we need to switch to new states because the \begin..\end will interleave with groups
\\newenvironment        { yypushState(NEW_ENVIRONMENT_DEFINITION_NAME); return COMMAND_TOKEN; }
\\renewenvironment      { yypushState(NEW_ENVIRONMENT_DEFINITION_NAME); return COMMAND_TOKEN; }

// A separate state is used to track when we start with the second parameter of \newenvironment, this state denotes the first one
<NEW_ENVIRONMENT_DEFINITION_NAME> {
    {CLOSE_BRACE}       { yypopState(); yypushState(NEW_ENVIRONMENT_DEFINITION); return CLOSE_BRACE; }
}

// We are visiting a second parameter of a \newenvironment definition, so we need to keep track of braces
// The idea is that we will skip the }{ separating the second and third parameter, so that the \begin and \end of the
// environment to be defined will not appear in a separate group
<NEW_ENVIRONMENT_DEFINITION> {
    {OPEN_BRACE}       { newEnvironmentBracesNesting++; return OPEN_BRACE; }
    {CLOSE_BRACE}      {
        newEnvironmentBracesNesting--;
        if(newEnvironmentBracesNesting == 0) {
            yypopState(); yypushState(NEW_ENVIRONMENT_SKIP_BRACE);
            // We could have return normal text, but in this way the braces still match
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

<YYINITIAL,DISPLAY_MATH> {
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

<PREAMBLE_OPTION> {
    "$"                 { return NORMAL_TEXT_WORD; }
    {CLOSE_BRACE}       { yypopState(); return CLOSE_BRACE; }
}

<DISPLAY_MATH> {
    "\\]"               { yypopState(); return DISPLAY_MATH_END; }
}

/*
 * Other elements
 */

// The array package provides <{...} and >{...} preamble options for tables
// which are often used with $, in which case the $ is not an inline_math_start (because there's only one $ in the group, which would be a parse errror)
\<\{                   { yypushState(PREAMBLE_OPTION); return OPEN_BRACE; }
>\{                    { yypushState(PREAMBLE_OPTION); return OPEN_BRACE; }

// In case a backslash is not a command, probably because  a line ends with a backslash, then we do not want to lex the following newline as a command token,
// because that will confuse the formatter because it will see the next line as being on this line
\\                    { return NORMAL_TEXT_CHAR; }

"*"                     { return STAR; }
// A separate token, used for example for aligning & in tables
"&"                     { return AMPERSAND; }
{OPEN_BRACKET}          { return OPEN_BRACKET; }
{CLOSE_BRACKET}         { return CLOSE_BRACKET; }
{OPEN_BRACE}            { return OPEN_BRACE; }
{CLOSE_BRACE}           { return CLOSE_BRACE; }
{OPEN_PAREN}            { return OPEN_PAREN; }
{CLOSE_PAREN}           { return CLOSE_PAREN; }

{BEGIN_PSEUDOCODE_BLOCK} { return BEGIN_PSEUDOCODE_BLOCK; }
{MIDDLE_PSEUDOCODE_BLOCK} { return MIDDLE_PSEUDOCODE_BLOCK; }
{END_PSEUDOCODE_BLOCK}  { return END_PSEUDOCODE_BLOCK; }

{WHITE_SPACE}           { return WHITE_SPACE; }
{BEGIN_TOKEN}           { yypushState(POSSIBLE_VERBATIM_BEGIN); return BEGIN_TOKEN; }
{END_TOKEN}             { return END_TOKEN; }
{COMMAND_TOKEN}         { return COMMAND_TOKEN; }
{COMMAND_IFNEXTCHAR}    { return COMMAND_IFNEXTCHAR; }
{LEXER_OFF_TOKEN}       { yypushState(OFF); return COMMENT_TOKEN; }
{COMMENT_TOKEN}         { return COMMENT_TOKEN; }
{NORMAL_TEXT_WORD}      { return NORMAL_TEXT_WORD; }
{NORMAL_TEXT_CHAR}      { return NORMAL_TEXT_CHAR; }

[^]                     { return com.intellij.psi.TokenType.BAD_CHARACTER; }
