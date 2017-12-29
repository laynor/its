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

(defn create-client [display-name init-fun]
  "Returns an x11 client and its event channel as [client events]"
  (let [events (chan 10)
        client (nodex11.createClient #js {:display display-name} init-fun)]
    (.on client "event" (partial put! events))
    [client events]))

  ;; (let [client display.client
  ;;       root (window/get-screen-root client 0)
  ;;       mod-1-mask 2r1000
  ;;       keysym-f1 67
  ;;       grab-mode-async 1
  ;;       none 0]
  ;;   (swap! state assoc :client client)

  ;;   (client.GrabKey root true mod-1-mask keysym-f1
  ;;                   grab-mode-async grab-mode-async)

(defn- grab-button
  [client wid owner-events mask pointer-mode keyb-mode confine-to cursor button modifiers]
  (.GrabButton client
               win
               owner-events
               mask
               pointer-mode
               keyb-mode
               confine-to
               cursor
               button
               modifiers))
                     ;; (apply bit-or
                     ;;        (map event-mask->x11
                     ;;             #{:button-press :button-release :pointer-motion}))
                     ;; (grab-mode->x11 :async) (grab-mode->x11 :async)
                     ;; (or nil 0) (or nil 0)
                     ;; 1 mod-1-mask))

  ;;   (client.GrabButton root true
  ;;                      (bit-or nodex11.eventMask.ButtonPress
  ;;                              nodex11.eventMask.ButtonRelease
  ;;                              nodex11.eventMask.PointerMotion)
  ;;                      grab-mode-async grab-mode-async
  ;;                      none none
  ;;                      3 mod-1-mask)))
