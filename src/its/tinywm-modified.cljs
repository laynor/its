(ns ^:figwheel-always its.tinywm
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as nodejs]
            [its.util :as util]
            [its.window :as window]
            [its.x11 :as x11]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]))

(def nodex11 (nodejs/require "x11"))

;; Window manager state
(defonce state (atom {}))

(defn tinywm-init!
  "WM initialization"
  [error display]
  (let [client display.client
        root (window/get-screen-root client 0)
        mod-1-mask 2r1000
        keysym-f1 67
        grab-mode-async 1
        none 0]
    (swap! state assoc :client client)

    (client.GrabKey root true mod-1-mask keysym-f1
                    grab-mode-async grab-mode-async)

    (client.GrabButton root true
                       (bit-or nodex11.eventMask.ButtonPress
                               nodex11.eventMask.ButtonRelease
                               nodex11.eventMask.PointerMotion)
                       grab-mode-async grab-mode-async
                       none none
                       1 mod-1-mask)

    (client.GrabButton root true
                       (bit-or nodex11.eventMask.ButtonPress
                               nodex11.eventMask.ButtonRelease
                               nodex11.eventMask.PointerMotion)
                       grab-mode-async grab-mode-async
                       none none
                       3 mod-1-mask)))





(defn handle-event [ev]
  (let [{start :start attr :attr client :client} @state
        win (:child ev)]

    (when client
      (case (:name ev)
        :key-press      (when (window/valid? win)
                          (window/raise-window client win))

        :button-press   (when (window/valid? win)
                          (let [c (chan 1)]
                            (.GetGeometry client win #(put! c (util/lispify %2)))
                            (go (swap! state merge  {:start ev
                                                     :attr (<! c)}))))


        :button-release (swap! state assoc :start nil)

        :motion-notify  (when (and start (window/valid? (:child start)))
                          (let [xdiff (- (:rootx ev) (:rootx start))
                                ydiff (- (:rooty ev) (:rooty start))
                                keycode (:keycode start)
                                is-lmb (= 1 keycode)
                                is-rmb (= 3 keycode)
                                win (:child start)]
                            (window/set-geometry client win
                                                 (+ (:x-pos attr) (if is-lmb xdiff 0))
                                                 (+ (:y-pos attr) (if is-lmb ydiff 0))
                                                 (max 1 (+ (:width attr)
                                                           (if is-rmb xdiff 0)))
                                                 (max 1 (+ (:height attr)
                                                           (if is-rmb ydiff 0))))))
        :quit ::quit
        nil))))


(defn start-event-handler! [f events]
  "Starts an asynchronous event handler. f is called on the event."
  (go-loop [retval nil]
    (let [ev (<! events)]
      (when-not (= retval ::quit)
        (recur (f (util/lispify-event ev)))))))


(defn tinywm []
  (let [[client events] (x11/create-client ":1" tinywm-init!)]
    (start-event-handler! handle-event events)))
