(ns ^:figwheel-always its.x11
  (:require [cljs.nodejs :as nodejs]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]))

(def nodex11 (nodejs/require "x11"))

(defn create-client [display-name init-fun]
  "Returns an x11 client and its event channel as [client events]"
  (let [events (chan 10)
        client (nodex11.createClient #js {:display display-name} init-fun)]
    (.on client "event" (partial put! events))
    [client events]))
