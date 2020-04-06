- [ ] Document compiles and pdf viewer is opened
- [ ] Delete aux files and run configs, then test that makeindex, bibtex and biber configs are generated automatically when needed
- [ ] Forward/backward search
- [ ] Autocomplete (bibtex, latex)
- [ ] Run ctan.py

- [ ] Copy changelog to plugin.xml
- [ ] Update version in plugin.xml
- [ ] Update feature list in readme if needed
- [ ] Release on GitHub
- [ ] Release on plugin repo: in build.gradle change version number and `channels 'alpha'` to `channels 'stable'` temporarily

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