### About alpha builds

This plugin also has an alpha channel besides the default stable channel.
The alpha channel contains the latest build with the latest features available (of most open pull requests), and is updated much more frequently than the stable channel.

It is used for testing features before they are released in the stable channel, so alpha versions of the plugin may be more unstable.
There are multiple ways by which you can acquire an alpha build.

### Download it directly

You have multiple options of acquiring a zip file with the plugin.

1. Go to https://plugins.jetbrains.com/plugin/9473-texify-idea/versions and click on Alpha, then to the right you have a download button for each alpha version.
2. You could also download a stable release from there or from the releases page (on which they appear just a bit earlier) at https://github.com/Ruben-Sten/TeXiFy-IDEA/releases
3. You could also build from source, see https://github.com/Ruben-Sten/TeXiFy-IDEA#building-from-source-using-intellij
4. Possibly someone else gave you a zip file.

#### Installing the plugin from a zip file

* Go to menu:Settings[Plugins] and click the gear icon, click Install Plugin from Disk, select the zip file and install.
* Make sure to restart IntelliJ.

### Subscribing to the alpha channel

More detailed information is at https://www.jetbrains.com/help/idea/managing-plugins.html#repos but we will quickly summarize the steps.

* Subscribe to the alpha channel by going to Settings | Plugins | gear icon | Manage Plugin Repositories | plus icon, then use the url https://plugins.jetbrains.com/plugins/alpha/list or https://plugins.jetbrains.com/plugins/alpha/list?pluginId=9473
* IntelliJ should suggest an update of TeXiFy now. If not, uninstall the plugin first and then reinstall the plugin by going to Marketplace and searching for `TeXiFy-IDEA`, you should see the version next to the name is the alpha version.
