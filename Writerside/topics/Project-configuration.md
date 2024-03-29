# Project configuration

IntelliJ works with projects to organize your source files.
You can have multiple LaTeX documents in one project.
For more information, see [Projects | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/creating-and-managing-projects.html#project-formats).

## Indexing

TeXiFy will index your LaTeX installation in order to add all commands and environment from all installed packages to the autocompletion.
Most regular LaTeX distribution installations are supported, see the table in [SDKs](#sdks).
You can refresh this index by using <ui-path>File | Invalidate Caches</ui-path>.

## SDKs

In IntelliJ you can use the SDK settings editor to specify a custom LaTeX SDK, in order to let TeXiFy know where your LaTeX installation is.
This is of course only necessary if it is not in your PATH already, but in some cases (like with a Flatpak installation) it is not possible to modify your PATH.

![SDK](sdk.png)

You can find these settings in <ui-path>File | Project Structure</ui-path>.
Under SDKs you can see all the configured SDKs.
If you click the plus button there, you can see the different types of SDKs you can add but it also shows automatically detected SDKs.

![project sdk](project-sdk.png)

Once you have created an SDK (which is a global setting), you can select it as Project SDK under the Project menu.
Then you can in your run configuration under LaTeX Distribution select Use project SDK.

> Set the LaTeX Distribution in your [run configuration template](Running-a-LaTeX-file.md) as well if you want to use it by default for new run configurations.

If you don’t have `pdflatex` in your PATH, TeXiFy will show a warning that you won’t be able to compile.

![set-up-sdk](set-up-sdk.png)

Because there are many different ways to install LaTeX, and many have their own SDK type, we provide a small overview.
The home path is the default path you need to select if you add such an SDK types, of course only if you did install it to the default location.
In most cases, these should be detected automatically.

The column 'Support package index' indicates if TeXiFy is able to locate package sources, which is required to have autocompletion for all commands in packages.
Note that MiKTeX can be installed for the user only or as admin, both of which are handled by the same SDK type.

| SDK type | Default home path | Supports package index |
| --- | --- | --- |
| MiKTeX on Mac/Linux (user install) | `~/bin` | yes |
| MiKTeX on Mac/Linux (admin install) | `/usr/local/bin` | no |
| MiKTeX on Windows (user install) | `C:\Users\myusername\AppData\Local\Programs\MiKTeX 2.9` | yes |
| MiKTeX on Windows (admin install) | ? | no |
| MiKTeX Docker image | `/usr/bin` (location of the `docker` executable) | no |
| TeX Live (recommended installation) | `~/texlive/2020` | yes |
| Native TeX Live (installation using package manager) | `/usr/bin` (location of `pdflatex` executable) | no |
| Tectonic | Cache path | yes (limited autocompletion) |

For the MiKTeX Docker image you can select the image name in the settings, but currently it still only works with MiKTeX images.

#### Tectonic
Tectonic is a bit of a special case, because while it uses TeX Live as source for all the packages, it has automatic package download implemented and uses a local cache in its own format.
That means TeXiFy can find these files, but because dtx files are not downloaded (only sty files) the autocompletion will be limited.
This is also the reason that you have to select the path containing the cached files as SDK home directory, see [https://github.com/tectonic-typesetting/tectonic/issues/159](https://github.com/tectonic-typesetting/tectonic/issues/159).

