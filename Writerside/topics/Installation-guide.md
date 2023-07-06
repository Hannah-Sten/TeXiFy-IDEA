---
switcher-label: Operating System
---

# Installation guide


> Before reading this page, select your operating system in the menu at the top right of this page.
{style="Warning"}

## Installing IntelliJ, LaTeX and the TeXiFy-IDEA plugin
In this section we will show you how to install everything that is necessary to get started with TeXiFy, as well as give a few pointers to get started with LaTeX.

### Installation instructions for Windows {switcher-key="Windows"}

* If you don’t have the latest version yet, download and install
  [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), the Community
  edition is free. You may want to install the
  [Jetbrains toolbox](https://www.jetbrains.com/toolbox/app/) instead, so
  you can manage projects and editors easily (Jetbrains has editors for
  more programming languages as well).
* Download and install [LaTeX for Windows](https://miktex.org/download).
  During installation, choose the option
  `Install missing packages on the fly: yes`.
* Open IntelliJ, in the welcome menu choose <ui-path>Configure | Plugins</ui-path> (or when
  you already have a project open, use <ui-path>File | Settings | Plugins</ui-path>).
* Click on Marketplace and search for `texify`, then click
  `install`.
* Install the [PDF Viewer](https://plugins.jetbrains.com/plugin/14494-pdf-viewer) plugin as well, it provides a built-in pdf viewer but does not have full functionality yet. Alternatively, you can install the [SumatraPDF](https://www.sumatrapdfreader.org/download-free-pdf-viewer.html) pdf viewer.
* If you had to install LaTeX, log out and log back in. Otherwise restarting IntelliJ is sufficient.
* Click on Create New Project (in the welcome menu) or <ui-path>File | New | Project</ui-path>,
  select LaTeX in the left column and click next. Specify a name for your
  project, and a location. A project can contain multiple LaTeX files and documents, so
  for example if you make a new document for each new homework you get,
  you could place them all in the same project.
* Double-click or click on the arrow next to your project name on the
  left to open the directory.
* A standard document called `main.tex` is already made for you, you can find it in the
  `src` folder. This folder will contain all your LaTeX source files. Double-click on
  the `.tex` file to open it. If you cannot see any directory structure
  (the folders on the left side), hit Project (it’s written on its side)
  on the left of your screen.
* Type some text between the `\begin{document}` and `\end{document}`
  lines, hit the compile icon next to the `\begin{document}` line and
  click Run, or use <shortcut>Control+Shift+F10</shortcut>. If you see a pdf appearing,
  congratulations!
* If you see `LaTeX error: file a4.sty not found` then you did not
  restart your pc. Do it now.
* The next time you can also compile using the Run button (looks like a
  Play button) in the top-right menu, or using <shortcut>Shift+F10</shortcut>.

#### Configuring forward and inverse search

* When your cursor is in IntelliJ and you have just compiled a document,
  you can look up which line in the pdf corresponds to the line your
  cursor is at by going in IntelliJ to <ui-path>Tools | LaTeX | Forward Search</ui-path>, or using the
  shortcut which is listed there. If you use SumatraPDF, this shortcut can also be used to bring
  the SumatraPDF window in view when you do not see it.
* You can also do the reverse: if you have the pdf open inside IntelliJ, you can hold Ctrl and click in the document to bring your cursor to the LaTeX source. Make sure you have compiled the document first.
  If you use SumatraPDF, you have to configure it once by clicking
  <ui-path>Tools | LaTeX | SumatraPDF | Configure inverse search</ui-path>. Then double-click
  in SumatraPDF in a pdf you just compiled, and it should make your cursor
  go to the correct location in IntelliJ.
* Have a look at the [tips](#tips).

### Installation instructions for linux {switcher-key="Linux"}

These instructions were tested on at least Ubuntu 16.04, 18.04, Fedora
and Arch Linux.

* If you don’t have the latest version yet, download and install
  [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), the Community
  edition is free. You may want to use the
  [Jetbrains toolbox](https://www.jetbrains.com/toolbox/app/) instead, so
  you can manage projects and editors easily (Jetbrains has editors for
  more programming languages as well). Download and save the `.tar.gz`
  file to your Downloads folder, or install the Toolbox (jetbrains-toolbox) via your package manager.
* You can extract in your Downloads folder with, in case you downloaded
  IntelliJ instead of the Toolbox, (change the exactly version number in the following command to the correct one, you can
  use tab for autocompletion)
  `sudo tar xf ideaIU-2018.1.5.tar.gz -C /opt/`, then run
  `/opt/idea-IU-181.5281.24/bin/idea.sh`, or in case you downloaded the
  toolbox directly, `sudo tar xf jetbrains-toolbox-1.11.4269.tar.gz -C /opt/` and
  run `/opt/jetbrains-toolbox-1.11.4269/jetbrains-toolbox`, then install
  IntelliJ.
* To install LaTeX, you can for example use the TeX Live distribution. If something
  is already installed, check that the version is at least 2017 with
  `latex --version`. If not, for example if you are on Ubuntu 16.04, you
  have to first remove the old TeX Live (see for example
  [these steps](https://tex.stackexchange.com/a/95502/98850)) and then
  install a newer TeX Live (based on the LaTeX3 setup), as follows.

#### Installing TeX Live

It is recommended to install TeX Live directly and not via your package manager, to ensure you have the latest version.
You should install TeX Live in your home directory, so it doesn’t need elevated permissions for everything.

In this example we will install the `basic` scheme instead of the `full` scheme, which will only install a basic set of packages to save space.
The full installation may be 3.5GB or more.

* In your Downloads folder, run the following.

```shell
wget http://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz
tar -xvf ./install-tl-unx.tar.gz
cd install-tl-*
./install-tl --scheme=basic
```

In the installer, press <control>D</control> and <control>Enter</control> to change the home directory from `/user/local/texlive/yyyy` to `/home/username/texlive/yyyy` or whatever you want.

Add `/home/username/texlive/yyyy/bin/x86_64-linux` or wherever you installed it to your `PATH` environment variable, for example by putting in some configuration file like `~/.bashrc` or `~/.profile` the line `export PATH="/home/username/texlive/2018/bin/x86_64-linux:$PATH`.
Don’t forget to log out and back in.
You can test the installation by running `latex small2e`.

If IntelliJ does not recognise your TeX Live installation, for example you get a warning `SyncTeX not installed: Forward search and inverse search need the synctex command line tool to be installed.`, make sure you modify your PATH as above. Note that for example `~/.zshrc` might not be picked up and you need to use `~/.profile`.

#### Installing packages for not-full installations

When not doing a full TeX Live install (e.g., to save space) not all tex engines are installed. To install one later on, use `tlmgr` to install the needed packages.
Note that when using `tlmgr` to search for the necessary packages, e.g., `tlmgr search xetex`, they might not show up.
For example, for XeLaTeX, you need to install `collection-xetex`.

* Install the packages you need, for example
  `tlmgr install xkeyval collection-latex collection-langeuropean`

If you have an existing document, you can also use [texliveonfly](https://tex.stackexchange.com/a/463842/98850) to install the required packages automatically.

#### Starting with IntelliJ

* Open IntelliJ, in the welcome menu choose <ui-path>Configure | Plugins</ui-path> (or when
  you already have a project open, use <ui-path>File | Settings | Plugins</ui-path>).
* Click on Marketplace and search for `texify`, then click
  `install`.
* Also install the PDF Viewer plugin to view the pdf within IntelliJ.
* If you had to install LaTeX, log out and log back in.
* Click on Create New Project (in the welcome menu) or <ui-path>File | New | Project</ui-path>,
  select LaTeX in the left column and click next. Specify a name for your
  project, and a location. A project can contain multiple LaTeX files and documents, so
  for example if you make a new document for each new homework you get,
  you could place them all in the same project.
* Double-click or click on the arrow next to your project name on the
  left to open the directory.
* A standard document called `main.tex` is already made for you, you can find it in the
  `src` folder. This folder will contain all your LaTeX source files. Double-click on
  the `.tex` file to open it. If you cannot see any directory structure
  (the folders on the left side), hit Project (it’s written on its side)
  on the left of your screen.
* Type some text between the `\begin{document}` and `\end{document}`
  lines, hit the compile icon next to the `\begin{document}` line and
  click Run, or use <shortcut>Ctrl+Shift+F10</shortcut>. If you see a pdf appearing,
  congratulations!
* If you see `LaTeX error: file a4.sty not found` then you did not
  restart your pc. Do it now.
* The next time you can also compile using the Run button (looks like a
  Play button) in the top-right menu, or using <shortcut>Shift+F10</shortcut>.

#### Forward and inverse search

* When your cursor is in IntelliJ and you have just compiled a document,
  you can look up which line in the pdf corresponds to the line your
  cursor is at by going in IntelliJ to <ui-path>Tools | LaTeX | Forward Search</ui-path>, or using the shortcut
  which is listed there. If you use Evince, this shortcut can also be used to bring the
  Evince window in view when you do not see it.
* You can also do the reverse: press <control>Ctrl</control> and click in a pdf (either in Evince or in IntelliJ)
  you just compiled, and it should make your cursor go to the correct
  location in IntelliJ.
* Also have a look at the [tips](#tips).

### Installation instructions for Mac {switcher-key="MacOS"}

* If you don’t have the latest version yet, download and install
  [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), the Community
  edition is free. You may want to install the
  [Jetbrains toolbox](https://www.jetbrains.com/toolbox/app/) instead, so
  you can manage projects and editors easily (Jetbrains has editors for
  more programming languages as well).
* Download and install [https://miktex.org/howto/install-miktex-mac[LaTeX](https://miktex.org/howto/install-miktex-mac[LaTeX)
  for Mac]. It’s less error-prone if you install MiKTeX `system-wide`. During installation, choose the option
  `Install missing packages on the fly: yes`.
* Open IntelliJ, in the welcome menu choose <ui-path>Configure | Plugins</ui-path> (or when
  you already have a project open, use <ui-path>File | Settings | Plugins</ui-path>).
* Click on Marketplace and search for `texify`, then click
  `install`.
* Install the PDF Viewer plugin as well to view the pdf inside IntelliJ.
* If you had to install LaTeX, log out and log back in. Otherwise restarting IntelliJ is sufficient.
* Click on Create New Project (in the welcome menu) or <ui-path>File | New | Project</ui-path>,
  select LaTeX in the left column and click next. Specify a name for your
  project, and a location. A project can contain multiple LaTeX files, so
  for example if you make a new document for each new homework you get,
  place them all in the same project.
* Double-click or click on the arrow next to your project name on the
  left to open the directory.
* A standard document is already made for you, you can find it in the
  `src` folder. This folder will contain all your LaTeX. Double-click on
  the `.tex` file to open it. If you cannot see any directory structure
  (the folders on the left side), hit Project (it’s written on its side)
  on the left of your screen.
* Type some text between the `\begin{document}` and `\end{document}`
  lines, hit the compile icon next to the `\begin{document}` line and
  click Run. If you see a pdf appearing, congratulations!
* The next time you can also compile using the Run button (looks like a
  Play button) in the top-right menu.
* Have a look at the [tips](#tips).

If you want to use Skim instead of the built-in pdf viewer, for configuring forward and backward search see the [Skim support](Skim-support) wiki page.

### Tips

* You never have to remember to save your work, IntelliJ will
  automatically save every letter you type.
* You can personalise the template that is used when you create a new
  LaTeX file in <ui-path>Settings | Editor | File and code templates | LaTeX
  source</ui-path>.
* You don’t need to close the pdf to recompile, it will automatically
  refresh.
* A good way to start learning LaTeX is by asking someone how to do what
  you want to do or by Googling ``what-I-want-to-do latex''.
* Some standard LaTeX commands are available in the LaTeX menu.
* Pay attention to squiggles (wavey lines) under text you typed, they
  indicate that something is wrong. Hovering over it gives extra
  information. In some cases, a ready-made fix is waiting to be applied:
  hit the lightbulb that appears on the left, or hit Alt+Enter to view and
  apply it. A quick overview of useful shortcuts is on the [Shortcuts](Shortcuts) page.
* If your LaTeX indentation (the number of spaces that is in front of
  each line) looks messy, reformat with <shortcut key="$Reformat">.
* This wiki contains documentation about many features, you can browse around the [Features](Features) page.
* If you are searching how a particular symbol has to be written in
  LaTeX, the [Detexify](http://detexify.kirelabs.org/classify.html) tool can
  probably help you. Just draw your symbol in the `draw here` box and the
  command will be listed on the right.
  In LaTeX projects you should have a Detexify tool window on the right.
* If you want a proper explanation of what LaTeX and its philosophy is
  about, read the [Not So Short Introduction To LaTeX2e](http://ctan.cs.uu.nl/info/lshort/english/lshort.pdf).

Any suggestions for improvements of the installation instructions, however small? Please feel free to edit this wiki page, or let us know at [gitter](https://gitter.im/TeXiFy-IDEA)!

### When you want to work together: install git

* Often you will be working together on one document. In that case, use
  git to make this go smoothly. We use git because git is awesome, but
  there exist other tools as well.

Note that you can work with git and LaTeX even if you cannot use IntelliJ, for example because you are at
school. For those cases, you can use Overleaf, which is a minimalistic but web-based editor, but in that case you have to host your LaTeX at the Overleaf site instead of GitHub.

* If you are on the IntelliJ welcome screen and want to clone a project with git, IntelliJ will suggest to install git automatically. Otherwise, go to [git](https://git-scm.com/downloads), during installation just
  click `next` everywhere.
* Restart your computer.
* Make sure you have a GitHub account, and create a new repository.
* Go to IntelliJ and click <ui-path>File | New | Project from VCS | GitHub</ui-path> and select the repository you created.
* If you are asked by IntelliJ `Do you want to add this file to git?`,
  just click `no` when you do not recognize the file, and `yes` when you recognise it as a source file (`.tex` for example).

#### To push (upload) changes

* Hit <shortcut key="$Commit"> to commit changes to git.
* Specify a commit message.
* Click `commit and push` by hovering over the `commit` button.
* If your git username is asked, specify it.
* Click `push`.

#### To pull (download) changes

* Hit <shortcut key="$Pull"> or the arrow icon at the top right.

### I want to know more about git

* That’s great! Actually, git and similar tools are used everywhere by
  programmers to collaborate; it’s not just for LaTeX and doesn’t work
  just with GitHub.
* If you want to know more, a great git tutorial is at
  [learngitbranching.js.org](http://learngitbranching.js.org/).
* Want to know even more of advanced use of git? Read the excellent
  [Pro Git book](https://git-scm.com/book/en/v2) for free.

### Installing a specific version.

* Go to [https://plugins.jetbrains.com/plugin/9473-texify-idea/versions](https://plugins.jetbrains.com/plugin/9473-texify-idea/versions) and download the version you want
* Go to <ui-path>Settings | Plugins</ui-path> and click the gear icon, click Install Plugin from Disk, select the zip file and install.

For installing alpha versions, see [Alpha builds](Alpha-builds).
