# Discouraged or obsolete code

## Use of `\over` discouraged

Use `\frac` instead of `\over`.

## TeX styling primitives usage is discouraged

Instead of using styling commands like `{\bf bold text}` and `{\it italic text}`, use `\textbf{bold text}` and `\textit{italic text}`.
(In the second case, consider using `\emph{text}` for emphasis instead.)

## Discouraged use of `\def` and `\let`

Instead of using `\def` to define new commands, generally you should use `\newcommand` instead, or other new commands like `\NewDocumentCommand`.

## Avoid `eqnarray` {id="ins:avoid-eqnarray"}
The `eqnarray` and `eqnarray==` environments are not recommended because they produce inconsistent spacing of the equal signs and make no attempt to prevent overprinting of the equation body and equation number.

## Discouraged use of primitive TeX display math

Instead of `$$..$$` use `\[..\]`.

## Discouraged use of `\makeatletter` in tex sources

Only use `\makeatletter` and `\makeatother` if you know what you’re doing.
