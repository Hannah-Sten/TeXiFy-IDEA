When you have no TeX Live or MiKTeX installed directly on your system, but you do have a miktex docker image downloaded (it checks for 'miktex' in the output of `docker image ls`, TeXiFy assumes you want to use Dockerized MiKTeX.
You can also turn it on in the run configuration settings or template to always use Dockerized MiKTeX even if you have other LaTeX distributions installed.

Since the official Dockerized MiKTeX image at https://hub.docker.com/r/miktex/miktex is a bit old, which can cause problems downloading new packages, we provide our own Dockerized MiKTeX via GitHub.
You can pull it with `docker pull ghcr.io/hannah-sten/texify-idea/miktex:latest`.

You can then run your favourite LaTeX compiler like usual, and TeXify will make sure to perform the `docker run` (you can see the exact command at the top of the output log).
Custom output directories are supported.

## Installation of Docker

* To install Docker, see https://docs.docker.com/install/, for example use your package manager.
* Make sure to start the `docker` service (and enable if you want to start it on boot)
* To avoid a permission denied error when running, add yourself to the `docker` group.
* Reboot (logging out and in may not be enough)
* You have to login to GitHub to use the Docker image: get a github token from https://github.com/settings/tokens, save it somewhere secure and run `echo my_token | docker login https://docker.pkg.github.com -u myusername --password-stdin`
See https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-docker-for-use-with-github-packages#authenticating-to-github-packages for more info.
