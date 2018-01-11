(ns ^:figwheel-always its.event-handler
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]
            [its.util :as util]
            [its.window :as window]
            [its.x11 :as x11]
            [its.state :refer [state]]))

(defn handle-event [ev]
  (let [{start :start attr :attr client :client} @state
        win (:child ev)]

    (println "foo")
    (when client
      (case (:name ev)
        :key-press      (when (window/valid? win)
                          (let [mappings (x11/get-keyboard-mapping client)]
                            (go (let [m (<! mappings)
                                      kcode ev.keycode]
                                  (swap! state assoc-in [:kmap] m))))
                          (window/raise-window client win))

        :button-press   (when (window/valid? win)
                          (let [c (chan 1)]
                            ;; TODO wrap next call in its.x11
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
