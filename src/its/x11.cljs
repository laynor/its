(ns ^:figwheel-always its.x11
  (:require [cljs.nodejs :as nodejs]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]
            [its.util :as util]
            ))

(def nodex11 (nodejs/require "x11"))
(def nodeksm (nodejs/require "keysym"))

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

(def mod->x11
  {:shift   1
   :lock    2
   :control 4
   :mod1    8
   :mod2   10
   :mod3   20
   :mod4   40
   :mod5   80})



(defn key->x11 [k]
  (mod->x11 (key->mod k)))

(def keysyms
  (js->clj nodex11.keySyms :keywordize-keys true))

(def keysym->kword
  (into {}
        (map (fn [[k v]] [(:code v) k]))
        keysyms))

(defn mods [& keys]
  (into #{} keys))

(defn keycode->kword [mapping min-keycode]
  (into {}
        (map-indexed (fn [i v] [(+ i min-keycode) [(keysym->kword (v 0))
                                                   (keysym->kword (v 1))]]))
        mapping))

(defn keycode->keysym [mapping min-keycode]
  (into {}
        (map-indexed (fn [i v] [(+ i min-keycode) [(v 0) (v 1)]]))
        mapping))

(defn keycode->str [keycode->keysym keycode shift?]
  (let [ks->unicode (comp char #(get % "unicode") js->clj nodeksm.fromKeysym)]
    (nth (->> (keycode->keysym keycode)
              (map ks->unicode))
         (if shift? 1 0))))

(defn str->keysym [s]
  (-> (nodeksm.fromUnicode s)
      (js->clj :keywordize-keys true)
      (first)
      (:keysym)))


(defn get-keyboard-mapping [client]
  (let [c (chan)
        disp client.display
        min-keycode disp.min_keycode
        max-keycode disp.max_keycode]
    (println min-keycode (- max-keycode min-keycode))
    (.GetKeyboardMapping client
                         min-keycode (- max-keycode min-keycode)
                         (fn [err list]
                           (if err
                             (put! c err)
                             (put! c (js->clj list)))))
    c))


(defn create-client [display-name init-fun]
  "Returns an x11 client and its event channel as [client events]"
  (let [events (chan 10)
        client (nodex11.createClient #js {:display display-name} init-fun)]
    (.on client "event" (partial put! events))
    [client events]))

(defn- mods->x11 [mods]
  (reduce bit-or (map mod->x11 mods)))

(defn grab-key
  [client wid owner-events modifiers k pointer-mode keyb-mode]
  (.GrabKey client wid
            owner-events
            (mods->x11 modifiers)
            k
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
