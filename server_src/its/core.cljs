(ns ^:figwheel-always its.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(println "Hello from the Node!")

(defn -main
  []
  nil)

(set! *main-cli-fn* -main) ;; this is required
