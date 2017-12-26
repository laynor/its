(defproject its "0.1.0-SNAPSHOT"
  :description "A tiling window manager"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}



  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async  "0.3.443"]]

  :plugins [[lein-figwheel "0.5.14"]
            [lein-npm "0.6.2"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.8"]]

  :npm {:dependencies [[x11 "2.3.0"]]
        :devDependencies [[ws "3.3.3"]]}

  :cljsbuild {:test-commands {"unit" ["nodejs"
                                      "target/out/unit-test.js"]}
              :builds [{:id "server-dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main its.core
                                   :output-to "target/out/its.js"
                                   :output-dir "target/out"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true }}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/out-test/unit-test.js"
                                   :output-dir "target/out-test"
                                   :target :nodejs
                                   :main its.runner
                                   :pretty-print true}}]}

  :figwheel {}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.14"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["target"]}
             })
