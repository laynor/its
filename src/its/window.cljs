(ns its.window)

(def x11 (nodejs/require "x11"))

(defn get-display [client]
  (.-display client))

(defn get-screen-root [client screen]
  (let [display (get-display client)]
    (.-root (nth display.screen screen))))

(defn set-geometry [client win x y w h]
  (.MoveResizeWindow client win
                     x y w h ))

(defn maximize [client win]
  (let [root (get-screen-root client 0)])
  (.GetGeometry client root
                (fn [err attr]
                  (set-geometry client win ))))
