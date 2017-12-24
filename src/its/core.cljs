(ns ^:figwheel-always its.core
  (:require [cljs.nodejs :as nodejs]
            [its.tinywm :as tinywm]))

(def x11 (nodejs/require "x11"))


(nodejs/enable-util-print!)


(defn -main
  []
  (tinywm/tinywm))

(set! *main-cli-fn* -main) ;; this is required
