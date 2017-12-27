# its

It's a tiling window manager.

## Overview

FIXME: Write a paragraph about the library/project and highlight its goals.

## Setup
Install npm dependencies with

    lein npm isntall

Run a Xephyr server on display :1, or change the hardcoded display value in core.cljs.

    $ Xephyr -resizeable :1

To get an interactive development environment run:

    lein figwheel server-dev

and run the script with node:

    node target/out/its.js


Works on CIDER too.
Put this in your emacs dotfile:

    (require 'cider)
    (setq cider-cljs-lein-repl
          "(do (require 'figwheel-sidecar.repl-api)
             (figwheel-sidecar.repl-api/start-figwheel!)
             (figwheel-sidecar.repl-api/cljs-repl))")


To clean all compiled files:

    lein clean


## License

Copyright © 2017 Alessandro Piras

Distributed under the MIT license.
