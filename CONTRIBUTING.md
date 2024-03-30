# Contributing to TeXiFy IDEA
Hi there!
We love that you are interested in making TeXiFy IDEA better.
First off, we are open to any suggestions, issue reports, questions and pull requests.
Below we have compiled a number of rules and starting points for any contributions you want to make.

With this set of guidelines we hope to make it easier for everyone to join in and make sure your issue or changes are addressed properly.


## Contents
- [Code of Conduct](#code-of-conduct)
- [I just have a question!](#i-just-have-a-question)
- [Your first contribution](#your-first-contribution)
- [How to file an issue](#how-to-file-an-issue)
- [How to suggest a feature or enhancement](#how-to-suggest-a-feature-or-enhancement)
- [How to submit a pull request](#how-to-submit-a-pull-requst)
- [Code conventions](#code-conventions)
- [Helpful resources](#helpful-resources)


## Code of Conduct
To ensure a healthy and respectful developing environment, we have a set of ground rules described in the [Code of Conduct](.github/CODE_OF_CONDUCT.md).
By participating, we ask that you please respect this code.
When you spot unacceptable behavior, please report per the instructions in the Code of Conduct.


## I just have a question!
The fastest way to reach us is by starting a [GitHub Discussion](https://github.com/Hannah-Sten/TeXiFy-IDEA/discussions).
You can also reach us on the project's [Gitter chat](https://app.gitter.im/#/room/#TeXiFy-IDEA_Support:gitter.im) (just log in with your GitHub account).
We will get notified directly if you post something there and we try to get back to you as soon as possible.

## Your first contribution
Unsure where to start?
Look for issues that are labeled [_good-first-issue_](https://github.com/Hannah-Sten/TeXiFy-IDEA/issues?q=is%3Aopen+is%3Aissue+label%3Agood-first-issue).
These issues should only require a small amount of work and are a good starting point.
Please comment on an issue when you are going to work on it, so we can assign you and avoid duplicate work.

If you have another issue or idea in mind, that's great too!
Be sure to run your idea by us (see [here](#i-just-have-a-question) how to do this).
This prevents your changes to be rejected in a later stage.


Have a look at the [contributing documentation](https://hannah-sten.github.io/TeXiFy-IDEA/contributing-to-the-source-code.html) to see how you can setup local development and build from source, and the [helpful resources](#helpful-resources).

At any point, feel free to ask for help!
Everyone is a beginner at first, and we will try to help you on your way as good as we can.


## How to file an issue
When you open a new issue, make sure you fill in the provided issue template.
You can discard the template when you propose a new feature or are requesting support.
The template asks for:
- Which JetBrains IDE (IntelliJ, PyCharm, etc.) are you running?
- Which version of IntelliJ, PyCharm or other JetBrains IDE are you running?
- Which version of TeXiFy IDEA are you running?
- What did you do?
- What did you expect to see?
- What did you see instead?
- (if applicable) The full stacktrace of the exception thrown in the IDE.

We appreciate if you can provide a minimal example for which we are able to reproduce the issue.
If you are not able to work one up: that's fine too.
It might be harder however to diagnose the issue.

#### Prevent duplicates
Before submitting your issue, search for (already closed) issues that are similar or duplicates of your submission.
Not sure if you have a duplicate issue?
Submit yours anyway just to be sure.

#### Response
We might ask that you provide more information of your issue, or that you confirm that the issue is resolved.
Please make sure you respond within 30 days, as we might otherwise close your issue.


## How to suggest a feature or enhancement
It is just as easy as creating a new issue, but you can discard the issue template.
We are excited to hear your ideas!


## How to submit a pull request
Pull requests are managed via GitHub, and you are welcome to open one if you want your work to be included in the TeXiFy IDEA repository.
Make sure you complete the pull request template such that we can review your work efficiently.
The owners of the repository will review your work and provide you with feedback in a timely manner.
When we request changes, we ask that you please respond within 30 days.
You do not have to apply the changes before this time, but give at least an indication of how long it will take you to modify your work.

To prevent rejection, consider letting us know beforehand what you'll be working on.
Also make sure you adhere to the code conventions below and that you document your work accordingly.

If you want to know more or if anything is unclear, never be afraid [to ask](#i-just-have-a-question).


## Code conventions
- The language-of-choice is Kotlin, but Java is accepted as well.
- Indentation is 4 spaces, continuation indent 8 spaces.
- Split in multiple lines when it will be more readable (100 characters per line is a _guideline_).
- `else` must be put on a newline (e.g. `} else if (...) {` is wrong). Same for `catch` and `finally`.


## Helpful resources
- [README](README.md)
- [IntelliJ Platform SDK DevGuide](http://www.jetbrains.org/intellij/sdk/docs/welcome.html)
