(ns ^:figwheel-always its.x11
  (:require [cljs.nodejs :as nodejs]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]
            [its.util :as util]))

(def nodex11 (nodejs/require "x11"))

(def event-mask->x11
  (util/lispify nodex11.eventMask))

(def grab-mode->x11
  {:sync 0
   :async 1})

(def mouse-button->x11
  {:left   1
   :middle 2
   :right  3})

(def none 0)

(def modmask->x11
  {:shift   1
   :lock    2
   :control 4
   :meta    8
   :fixme   10
   :hyper   20
   :super   40})

(defn create-client [display-name init-fun]
  "Returns an x11 client and its event channel as [client events]"
  (let [events (chan 10)
        client (nodex11.createClient #js {:display display-name} init-fun)]
    (.on client "event" (partial put! events))
    [client events]))


(defn- mods->x11 [mods]
  (reduce bit-or (map modmask->x11 mods)))

(defn grab-key
  [client wid owner-events modifiers key pointer-mode keyb-mode]
  (.GrabKey client wid
            owner-events
            (mods->x11 modifiers)
            key
            (grab-mode->x11 pointer-mode)
            (grab-mode->x11 keyb-mode)))

(defn grab-button
  [client wid owner-events mask pointer-mode keyb-mode confine-to cursor button modifiers]
  (.GrabButton client
               wid
               owner-events
               (reduce bit-or 0 (map event-mask->x11 mask))
               (grab-mode->x11 pointer-mode)
               (grab-mode->x11 keyb-mode)
               (or confine-to none)
               (or cursor none)
               (mouse-button->x11 button)
               (mods->x11 modifiers)))
