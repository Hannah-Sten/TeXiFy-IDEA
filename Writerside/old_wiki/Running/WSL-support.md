_Since b0.6.10_

In the [Run configuration settings](Run-configurations#_choose_latex_distribution) you can choose to use TeX Live from WSL.

## Setup

* Install WSL, see for example [https://docs.microsoft.com/en-us/windows/wsl/install-win10](https://docs.microsoft.com/en-us/windows/wsl/install-win10)
* Install TeX Live as usual, see for example [TeX Live installation](Installation).
* Add `export PATH="/path/to/texlive/yyyy/bin/x86_64-linux:$PATH"` to your `~/.bashrc`.

Currently, TeXiFy will use bash to run LaTeX compilers.
Test your installation with `bash -ic "pdflatex --version"`.

## Troubleshooting

If pdflatex is not found, double check if the default wsl distribution is the one in which you installed LaTeX with `wsl -l`
