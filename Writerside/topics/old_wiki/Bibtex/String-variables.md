TeXiFy supports the use of `@string` variables in bibtex files, including syntax highlighting, autocompletion and 'go to source' navigation (kbd:[Ctrl + B] by default).

A usage example is the following.
```bibtex
@string{mytext = "This is a note."}

@Article{greenwade1993,
    author  = "George D. Greenwade",
    title   = "The {C}omprehensive {T}ex {A}rchive {N}etwork ({CTAN})",
    year    = "1993",
    journal = "TUGBoat",
    volume  = "14",
    number  = "3",
    pages   = "342--351",
    note    = mytext,
}
```
