(ns ^:figwheel-always its.tinywm
  (:require [cljs.nodejs :as nodejs]))

(def x11 (nodejs/require "x11"))

;; Window manager state
(defonce state (atom {}))

(defn its-init!
  "WM initialization"
  [error display]
  (let [root (.-root (nth display.screen 0))
        x display.client
        mod-1-mask (bit-shift-left 1 3)
        keysym-f1 67
        grab-mode-async 1
        none 0]
    (swap! state assoc :client x)

    (x.GrabKey root true mod-1-mask keysym-f1
               grab-mode-async grab-mode-async)

    (x.GrabButton root true
                  (bit-or x11.eventMask.ButtonPress
                          x11.eventMask.ButtonRelease
                          x11.eventMask.PointerMotion)
                  grab-mode-async grab-mode-async
                  none none
                  1 mod-1-mask)

    (x.GrabButton root true
                  (bit-or x11.eventMask.ButtonPress
                          x11.eventMask.ButtonRelease
                          x11.eventMask.PointerMotion)
                  grab-mode-async grab-mode-async
                  none none
                  3 mod-1-mask)))

(defn handle-event [ev]
  (let [s @state
        start  (:start s)
        attr   (:attr s)
        client (:client s)]
    (case ev.name
      "KeyPress"      (when (not= ev.child 0)
                        (println "foobar")
                        (client.RaiseWindow ev.child))

      "ButtonPress"   (when (and client (not= ev.child 0))
                        (.GetGeometry client ev.child
                                      (fn [err attr]
                                        (swap! state assoc :start ev)
                                        (swap! state assoc :attr  attr))))

      "ButtonRelease" (swap! state assoc :start nil)

      "MotionNotify"  (when (and start (not= start.child 0))
                        (let [xdiff (- ev.rootx start.rootx)
                              ydiff (- ev.rooty start.rooty)]
                          (.MoveResizeWindow client start.child
                                             (+ attr.xPos (if (= 1 start.keycode) xdiff 0))
                                             (+ attr.yPos (if (= 1 start.keycode) ydiff 0))
                                             (max 1 (+ attr.width  (if (= 3 start.keycode) xdiff 0)))
                                             (max 1 (+ attr.height (if (= 3 start.keycode) ydiff 0))))))
      nil)))

(defn tinywm []
  (let [client (x11.createClient #js {:display ":1"} its-init!)]
    (.on client "event" (fn [ev] (handle-event ev)))))
