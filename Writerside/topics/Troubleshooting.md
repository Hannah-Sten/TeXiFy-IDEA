# Troubleshooting

## Debugging performance issues

If you are experiencing UI freezes, IntelliJ will generate a thread dump, please upload this file as well.

For any performance issue: if you do not have a favourite profiler yet, you can use VisualVM. Install it using your package manager or go to https://visualvm.github.io/.

* First, just run TeXiFy like usual.
* Start VisualVM.
* In the Applications panel on the left, identify the instance of IntelliJ where TeXiFy is running, probably it is named Idea. Right-click on it and open.
* Go to the Sampler tab.
* Click Settings, and click Profile only packages. Specify `nl.hannahsten.**` (or a specific class you want to filter on. Note that if you want to filter for a Kotlin class you have to append `Kt` to the class name, e.g. `nl.hannahsten.texifyidea.editor.UpDownAutoBracketKt`. However, not all classes will appear in the view.)
* Click CPU to start profiling
* Reproduce the performance issue
* Stop the profiling
* Take a Snapshot to view and save results. Note that you may have to click a few more levels open to see the actual methods.
* Now you can zip the nps file and upload it here on GitHub.