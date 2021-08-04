- [ ] Create branch bx.x.x
- [ ] Run 'Usage of IntelliJ API not available in older IDEs' and 'Stateful extension' inspection (ctrl+alt+shift+i)
- [ ] Increase stub version in LatexParserDefinition
- [ ] Gradle clean, create new empty project and check that document compiles and pdf viewer is opened
- [ ] Delete aux files and run configs, then test that makeindex, bibtex and biber configs are generated automatically when needed
- [ ] Forward/backward search
- [ ] Does it work in PyCharm?
- [ ] Run ctan.py

- [ ] Copy changelog to description-changenotes.xml, replace using regexes below and update welcome text
- [ ] Update version in plugin.xml and build.gradle.kts
- [ ] runIDE and check the changelog is formatted correctly
- [ ] Release on plugin repo: in build.gradle change `channels 'alpha'` to `channels 'stable'` temporarily and publishPlugin
- [ ] Merge PR
- [ ] buildPlugin and release on GitHub, using the markdown changelog (then remove it from Changelog.md). tag version: 0.x.x and release title: Beta x.x.x
- [ ] Close the milestone

Regexes to replace markdown by html:

```regexp
#(\d+)
<a href="https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/$1">#$1</a>

\*(.*)\n
            <li>$1</li>\n
```

```html
        <p><strong>Beta 0.6.x</strong></p>
        <p>
            Welcome to TeXiFy IDEA x.x.x, ...
        <p>
        <p>
            We thank everyone who submitted issues and provided feedback to make TeXiFy IDEA better.
            Your input is valuable and well appreciated.
        </p>
        <br>
        <p>
            <em>Additions</em>
        </p>
        <ul>
            <li> Fixed bug. (<a href="https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/1120">#1120</a>)</li>
            
        </ul>
        <br>
        <p>
            <em>Changes</em>
        </p>
        <ul>
            <li> </li>
        </ul>
        <br>
        <p>
            <em>Bug fixes</em>
        </p>
        <ul>
            <li> </li>
        </ul>
        <br>
        <p>
            Thanks to <a href="https://github.com/user">User Name</a> for contributing to this release.
        </p>
        <br>
        <p>
            The full list of releases is available on the <a href="https://github.com/Hannah-Sten/TeXiFy-IDEA/releases">GitHub releases page</a>.
        </p>
```