Nearly every JetBrains IDE offers a refactoring tool called [Inline](https://www.jetbrains.com/help/idea/inline.html) which allows you to replace every reference of something with its definition. TeXiFy implements this in the following way:

#### Before
------------
Main.tex:
```latex
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   \input{demo}

\end{document}
```

demo.tex:
```latex
Hello World!
```

#### After
--------------
Main.tex:
```latex
\documentclass[11pt]{article}
\begin{document}

   \section{Demo}
   Hello World!

\end{document}
```

To perform this, you can right click an input command -> refactor -> inline and select what kind on inlining you are looking for.