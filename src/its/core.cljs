(ns ^:figwheel-always its.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as nodejs]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]
            [its.util :as util]
            [its.window :as window]
            [its.x11 :as x11]
            [its.event-handler :refer [handle-event]]
            [its.state :refer [state]]))


(defn its-init!
  "WM initialization"
  [error display]
  (let [client display.client
        root (window/get-screen-root client 0)
        mod-1-mask 2r1000
        keysym-f1 67
        grab-mode-async 1
        none 0]
    (swap! state assoc :client client)

    (println "mastucci")

    (x11/grab-key client root true #{:meta} keysym-f1 :async :async)

    (x11/grab-button client root true
                     #{:button-press :button-release :pointer-motion}
                     :async :async
                     nil nil
                     :left #{:meta})

    (x11/grab-button client root true
                     #{:button-press :button-release :pointer-motion}
                     :async :async
                     nil nil
                     :right #{:meta})))


(defn start-event-handler! [f events]
  "Starts an asynchronous event handler. f is called on the event."
  (go-loop [retval nil]
    (let [ev (<! events)]
      (when-not (= retval ::quit)
        (recur (f (util/lispify-event ev)))))))


(defn its []
  (let [[client events] (x11/create-client ":1" its-init!)]
    (start-event-handler! handle-event events)))


(nodejs/enable-util-print!)


(defn -main
  []
  (its))

(set! *main-cli-fn* -main) ;; this is required
