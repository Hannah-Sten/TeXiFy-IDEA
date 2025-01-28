# Contributing to the source code

See [CONTRIBUTING.md](https://github.com/Ruben-Sten/TeXiFy-IDEA/blob/master/CONTRIBUTING.md) for the general contributing guide.
Note that we keep track of 'internal' tasks at a YouTrack instance: [https://texify-idea.myjetbrains.com/youtrack/issues](https://texify-idea.myjetbrains.com/youtrack/issues)

If you are contributing UI, please read the IntelliJ Platform UI guidelines at [https://jetbrains.github.io/ui/](https://jetbrains.github.io/ui/).

## Adding missing table environments, verbatim environments, cite commands or other commands or environments metadata

TeXiFy inspections and other functionality relies in many cases on certain magic knowledge about commands or environments.
Often, this information is manually hardcoded in the source code, and can be incomplete or incorrect.
Most often this is really easy to fix, even without any knowledge of the source code of TeXiFy.
If you think something can be improved, you are encouraged to scroll through the files in [https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/util/magic](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/util/magic) for example CommandMagic and EnvironmentMagic.
You can also look at previous pull requests for inspiration, for example [#2245](https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/2245).

Note, if commands are just missing from the autocompletion, this is likely more complicated because these shouldn’t be hardcoded, but detected automatically.

### Adding support for command: example
Note: [#2245](https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/2245 presents it very clearly and if You just want to see some code, go that way. Stay here, if You want explanations.

Let's use `\newcommandx` as our example. As the name suggests, this is alternative form of `\newcommand` with additional features.
First, optional step: add the package the command is from to the list of predefined packages (if it's not already there). List is located in [nl/hannahsten/texifyidea/lang/LatexPackage](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/lang/LatexPackage.kt)

Second step: declare the command. Commands are declared in [src/nl/hannahsten/texifyidea/lang/commands](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/lang/commands) package and in our example we are going to use [LatexNewDefinitionCommand](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/lang/commands/LatexNewDefinitionCommand.kt) class.
Other types of commands should go to respective classes (names should be self-explanatory).
Add your command using the syntax analogous to the already existing commands.

Third, and the last step: add handling for the command. Usual place is: [src/nl/hannahsten/texifyidea/util/magic](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/util/magic), with the class [CommandMagic](https://github.com/Hannah-Sten/TeXiFy-IDEA/tree/master/src/nl/hannahsten/texifyidea/util/magic/CommandMagic.kt) in our example.
Here we add `\newcommandx` to the `regularStrictCommandDefinitions`, which is the set of all standard command defining commands.

And here You go: it's done (at least for this simple example)

## Building from source

We assume that git, IntelliJ, java and LaTeX are installed. If not, follow the normal [installation instructions](Installation-guide.md) first.

* Make a new project from version control if you don’t have it yet downloaded, or from existing sources if you have.
* On the GitHub [home page](https://github.com/Hannah-Sten/TeXiFy-IDEA) of TeXiFy click 'clone or download' and copy the url to Git Repository Url.
* If the project opens and you get a popup 'Import Gradle project', click that.
* If you are prompted to open the `build.gradle` file, do so.
* Select 'Use auto-import'.
* Thank Gradle that you’re done now!
* Check that in <ui-path>Settings | Build, Execution, Deployment | Compiler | Kotlin Compiler</ui-path> the Target JVM version is set correctly, currently it should be 1.8. If you encounter an error like `Kotlin: Cannot inline bytecode built with JVM target 1.8 into bytecode that is being built with JVM target 1.6.` when building, you need to look here.
* Check that in <ui-path>Settings | Build, Execution, Deployment | Build Tools | Gradle | Gradle JVM</ui-path> it is set to the required java version as specified at [https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html#intellij-platform-based-products-of-recent-ide-versions](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html#intellij-platform-based-products-of-recent-ide-versions)
* Test it worked by executing the 'buildPlugin' task in <ui-path>Gradle | Tasks | intellij</ui-path>, or hit double control and run `gradle buildPLugin`
* To view sources of IntelliJ Platform api classes, go to the Gradle tool window and click 'Download sources'.
* If something doesn’t work, have a look at the [Troubleshooting](#Troubleshooting-build) section.

#### To run directly from source
* Click the Gradle button on the right, the gradle task is located in <ui-path>Tasks | intellij | runIde</ui-path>. Double-click to run.
* If at some time you cannot use this and you need to run from command line, use `gradlew runIde`.
* Note how IntelliJ adds this task as a run configuration in the normal location if you have run it once, so you can use that one the next time.
* The first time it will look like you are installing a new IntelliJ - don’t worry, just click through it.
* You can also debug against other IDEs. At the moment only PyCharm is set up, but it is easy to add others. You can use it by specifying the argument `-PusePycharm=true` in your runIde run configuration.
* To make a new project but also to open existing `.tex` files, use <ui-path>File | New | Project | LaTeX</ui-path>.
* Compile a `.tex` file by clicking on the gutter icon next to `\begin{document}` or create a custom run configuration using the drop-down menu.

#### To build a zip which contains the plugin
* Click the Gradle button on the right, the gradle task is located in <ui-path>Tasks | intellij | buildPlugin</ui-path>. Right-click and run. The zip will be in build/distributions.
* Install the zip in IntelliJ using <ui-path>Settings | Plugins | Install plugin from disk</ui-path>.

#### To run tests
* Click the Gradle button on the right, the gradle task is located in <ui-path>Tasks | verification | check</ui-path>. Right-click and run. Note that check includes test so it will run the tests as well as ktlint.


## Adding an inspection

* Use highlight level `ProblemHighlightType.GENERIC_ERROR_OR_WARNING` when it should be a warning by default, or `GENERIC_ERROR` for errors, because any other levels (like `WEAK_WARNING`, `WARNING`, `ERROR`) will not be overridden when the user selects a different highlighting level.
* Inspection descriptions should not end with a full stop.
* Looking at previous pull requests may be helpful, for example [#2420](https://github.com/Hannah-Sten/TeXiFy-IDEA/pull/2420)
* Read the IntelliJ Platform UI guidelines at [https://jetbrains.github.io/ui/text/inspections/](https://jetbrains.github.io/ui/text/inspections/)

### Editing a lexer

For some documentation, see [https://jflex.de/manual.html](https://jflex.de/manual.html)

### Editing a parser

Injected methods are currently not supported, see [JetBrains/gradle-grammar-kit-plugin#3](https://github.com/JetBrains/gradle-grammar-kit-plugin/issues/3).
Use mixin classes if you want to override methods, if you just want to extend behaviour use extension functions.


## LatexCommands.commandToken.text vs LatexCommands.name

Throughout the code, you may see either `LatexCommands.commandToken.text` or `LatexCommands.name` being used to get the command name (e.g. `\section`).
Since the (generated) implementation of `getName()` is

```java
default String getName() {
    return getCommandToken().getText();
}
```

you would think that these are the same.
But there is an important difference! The `name` is _indexed_ (see `LatexCommandsIndex`).
This means that probably using `name` is recommended, as it would use the index, but it may be completely wrong if the index is not updated correctly.
This can lead to strange behaviour (see e.g. [#1097](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/1097)), which can be fixed by updating the index correctly.

## Concurrency

Never use the regular `runReadAction` from a coroutine! This read action will block write actions, but in a coroutine the regular cancellation check/exception does not work, which may lead to a deadlock.
See https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html#write-allowing-read-action-vs-nonblockingreadaction

## Helpful tools

* <ui-path>Tools | View PSI Structure</ui-path>
* <ui-path>Tools | Internal Actions | UI | UI Inspector</ui-path> to view information about any UI element
* Index Viewer plugin
* Thread Access Info plugin

## Adding code which uses classes from the java plugin

Instead of registering the implementation of a class in `plugin.xml`, add it to `java.xml`.
The java plugin is an optional dependency so the plugin can still be used in non-IntelliJ IDEs.

At the moment, it does not seem to be possible to debug in PyCharm because to build the plugin, the java plugin is needed.

## Adding project templates

If you use <ui-path>Tools | Save as project template</ui-path> then a zip will be created in `build/idea-sandbox/config/projectTemplates`.
Copy it to `resources/projectTemplates` and add an entry to `resources/META-INF/extensions/project-templates.xml`.

## Debugging plugin unload fail

See [https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html?from=jetbrains.org#troubleshooting](https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html?from=jetbrains.org#troubleshooting)

* Make sure the runIde run config has Allow parallel run selected
* Run runIde
* Set registry key `ide.plugins.snapshot.on.unload.fail` (if not already set)
* Change something in the code
* runIde again
* Install YourKit (I have also tried with Eclips MAT, IntelliJ and visualvm but none worked)
* Open the generated hprof file
* Go to Class loader, find the class loader which references TeXiFy things and click Paths from GC Roots.
* The classes that are mentioned there, were not unloaded successfully for whatever reason. (However, even on a partially successful unload, I see classes present here, so not sure what that means)

## Qodana

View reported results in GitHub: Security > Code Scanning analysis.

Run analysis locally:
```
docker run --rm -it -v /path/to/TeXiFy-IDEA/:/data/project/ -p 8080:8080 jetbrains/qodana-jvm --show-report --cache-dir=/tmp/qodana-cache
```
or using the CLI: `qodana scan --show-report`.

View report downloaded from GH Actions:
```
docker run -it --rm -p 8000:80 -v $(pwd)/report:/usr/share/nginx/html nginx
```

## Troubleshooting {id="Troubleshooting-build"}

### java.util.zip.ZipException: Archive is not a ZIP archive

### bad class file: class file has wrong version 55.0, should be 52.0

The IntelliJ SDK [requires Java 11](https://blog.jetbrains.com/platform/2020/09/intellij-project-migrates-to-java-11/), see [https://stackoverflow.com/a/59783851/4126843](https://stackoverflow.com/a/59783851/4126843).

### Execution failed for task ':runIde'.	Process 'command java.exe' finished with non-zero exit value 1

If you get the error `Caused by: org.gradle.process.internal.ExecException: Process 'command 'C:\Users\username\.gradle\caches\modules-2\files-2.1\com.jetbrains\jbre\jbr-11_0_6-windows-x64-b765.25\jbr\bin\java.exe'' finished with non-zero exit value 1` then delete the `jbre` folder in that path.

### The server may not support the client's requested TLS protocol versions: (TLSv1.2, TLSv1.3)

Please make sure you’re using at least JDK 21, both as project SDK and in <ui-path>Settings | Build, Execution, Deployment | Build Tools | Gradle | Gradle JVM</ui-path>.

### `Gtk-WARNING **: Unable to locate theme engine in module_path: "murrine"`

If you get this warning, it is not critical so you could ignore it but to solve it you can install the mentioned gtk engine, in this case Murrine.
For example on Arch Linux, install the `gtk-engine-murrine` package. Arch Linux sets the default theme to Adwaita, so install that with the `gnome-themes-extra` package.
For more information see [wiki.archlinux.org](https://wiki.archlinux.org/index.php/GTK+).

### `Unable to find method 'sun.misc.Unsafe.defineClass'` or `Please provide the path to the Android SDK` when syncing Gradle

This probably means your Gradle cache is corrupt, delete (on Windows) `C:\Users\username\.gradle\caches` and `C:\Users\username\.gradle\wrapper\dists` or (on Linux) `~/.gradle/caches` and `~/.gradle/wrapper/dists`, then reboot your system.

### `Error: java: package com.google.common.base does not exist`

* Update IntelliJ (help - check for updates).
* Update your IntelliJ SDK: go to Project Structure - SDKs.
* Hit the plus in the middle column and select IntelliJ Platform Plugin SDK.
* Select your IntelliJ installation directory (e.g. `C:\Program Files (x86)\JetBrains\IntelliJ IDEA xxxx.x`).
* Remove your old SDK. It is called 'IntelliJ IDEA IU-xxx' where `xxx` is anything but the highest number.
* Go to Project Structure - Project and select the new SDK.

## Updating the MiKTeX Docker image

Because the official [miktex/miktex](https://hub.docker.com/r/miktex/miktex) is at the moment rather out of date, we provide an updated version.
It can be updated as follows.
* Clone the source repo [https://github.com/MiKTeX/docker-miktex](https://github.com/MiKTeX/docker-miktex)
* Create a Docker run config with image tag `docker.pkg.github.com/hannah-sten/texify-idea/miktex:latest` and run it. You might want to add `--no-cache --pull` build options.
* Make sure that there is a valid Docker Registry for GitHub in Settings > Build, ..., > Docker > Registry, use a Docker V2 registry, point it to `ghcr.io` and as a password provide a PAT (see [https://docs.github.com/en/packages/guides/pushing-and-pulling-docker-images#authenticating-to-github-container-registry](https://docs.github.com/en/packages/guides/pushing-and-pulling-docker-images#authenticating-to-github-container-registry)).
* Right-click the image and click Push, provide as repository `hannah-sten/texify-idea/miktex` and tag `latest`. If it doesn’t work, follow [https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry)

## Debugging the intellij-pdf-viewer plugin with TeXiFy

TeXiFy provides the `pdfViewer` extension point which is implemented by the [intellij-pdf-viewer](https://github.com/FirstTimeInForever/intellij-pdf-viewer) plugin.
This means that, to debug forward search and inverse search for this pdf viewer, you have to debug the `intellij-pdf-viewer` plugin.

### Setting up TeXiFy as a dependency

This plugin has an optional dependency on TeXiFy. If this version of TeXiFy is in the JetBrains repo, you can immediately run the pdf viewer plugin (I think). If this is not the case or if this doesn’t work, do the following (based on [https://plugins.jetbrains.com/docs/intellij/update-plugins-format.html](https://plugins.jetbrains.com/docs/intellij/update-plugins-format.html)):

* Build the TeXiFy version you want to debug with.
* Create an empty directory somewhere.
* Put the TeXiFy zip file of plugin in this directory.
* Create a file `updatePlugins.xml` in this directory, with the following contents

      <plugins>
          <plugin id="nl.rubensten.texifyidea" url="http://127.0.0.1:8000/<zip-file>.zip" version="<TeXiFy-Version>">
              <idea-version since-build="<FULL build number>"/>
          </plugin>
      </plugins>
* Create a local JetBrains plugin repo in this folder by running: `python -m http.server 8000 --bind 127.0.0.1`.
* In `build.gradle.kts` of the pdf viewer plugin (that has TeXiFy as dependency), add the following to the `intellij` block:

      pluginsRepo {
          custom("http://127.0.0.1:8000")
      }
      setPlugins("nl.rubensten.texifyidea:<TeXiFy-version>")

where the plugin id and version should match that given in `updatePlugins.xml` and in the zip of the plugin.

### Debugging

***TypeScript*** When the pdf viewer plugin is running, right click on an open PDF to open the dev tools.
This will open the ordinary dev tools that is in any browser, and you can print stuff to the console here by using `console.log(...)`.

***Kotlin*** The Kotlin part of the plugin can be debugged as usual.
