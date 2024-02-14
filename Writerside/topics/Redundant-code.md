# Redundant code

## Redundant escape when Unicode is enabled

When unicode is enabled (see [Unsupported Unicode character](Probable-bugs.md#unsupported-unicode-character)) then you can use the unicode character instead of escaping the diacritic, so you can write `ú` instead of `\'u`.

## Redundant use of `\par`

When using `\par` in combination with two newlines (before, around or after) then the `\par` is redundant.

## Unnecessary whitespace in section commands

Instead of `\section{test }\label{sec:test}`, write `\section{test}\label{sec:test}` because the whitespace will not be rendered.

## Command is already defined

This inspection will trigger if you use `\newcommand` on a command for which TeXiFy knows that it already exists.
Because TeXiFy does not know about every LaTeX command in every package, it cannot warn you when using `\renewcommand` on a command that does not yet exist.

## Duplicate labels
## Package has been imported multiple times

While importing a package multiple times is not prohibited by LaTeX, in general it is best to avoid duplicate `\usepackage`-like commands.

## Duplicate command definitions

This inspection will check if you have used `\newcommand` multiple times to define the same command.
