(ns ^:figwheel-always its.x11
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as nodejs]
            [clojure.core.async :as async :refer [put! <! >! close! chan timeout]]
            [its.util :as util]
            ))

(defonce x11-state (atom {}))
;; node requires
(def nodex11 (nodejs/require "x11"))
(def nodeksm (nodejs/require "keysym"))

;; event-mask
(def event-mask->x11
  (util/lispify nodex11.eventMask))

;; grab-mode
(def grab-mode->x11
  {:sync 0
   :async 1})

;; mouse button
(def mouse-button->x11
  {:left   1
   :middle 2
   :right  3})

;; null window id
(def none 0)

;; modifiers
(def mod->x11
  {:shift   1
   :lock    2
   :control 4
   :mod1    8
   :mod2   10
   :mod3   20
   :mod4   40
   :mod5   80})

;; keysym->keycode table
;; vector addressed by keycode:
;; first value is the keysym associated to the keycode
;; second value is the keysym associated to the keycode when shift is pressed
(def keysyms
  (js->clj nodex11.keySyms :keywordize-keys true))

(def keysym->kword
  (into {}
        (map (fn [[k v]] [(:code v) k]))
        keysyms))


(declare get-keyboard-mapping
         keysym->keycode-map)

(defn init-keyboard-support! [display]
  (let [[mappings-chan min-keycode] (get-keyboard-mapping display)]
    (go (let [foo (do (println "antani1") 12)
              mappings        (<! mappings-chan)
              bar (do (println "antani2") 13)
              keycode-table (concat (replicate min-keycode (vec (replicate 20 0)))
                                      mappings)
              keysym->keycode (keysym->keycode-map keycode-table)]

          (println "INIT KEYBOARD:" (map (take 10) [keycode-table keysym->keycode]))

          (swap! x11-state (fn [s]
                             (-> s
                                 (assoc :keycode-table   keycode-table)
                                 (assoc :keysym->keycode keysym->keycode))))))))



(defn init-x11! [error display]
  (init-keyboard-support! display))

(defn keycode->kword [mapping min-keycode]
  (into {}
        (map-indexed (fn [i v] [(+ i min-keycode) [(keysym->kword (v 0))
                                                   (keysym->kword (v 1))]]))
        mapping))

(defn keycode->keysym [code mappings shift?]
  ((mappings code) (if shift? 1 0)))

;; (defn keycode->keysym [mapping min-keycode]
;;   (into {}
;;         (map-indexed (fn [i v] [(+ i min-keycode) [(v 0) (v 1)]]))
;;         mapping))


;; (defn keycode->str [keycode->keysym keycode shift?]
;;   (let [ks->unicode (comp char #(get % "unicode") js->clj nodeksm.fromKeysym)]
;;     (nth (->> (keycode->keysym keycode)
;;               (map ks->unicode))
;;          (if shift? 1 0))))

;; (defn str->keysym [s]
;;   (-> (nodeksm.fromUnicode s)
;;       (js->clj :keywordize-keys true)
;;       (first)
;;       (:keysym)))

(defn keysym->keycode-map
  ([kcode x11-mappings itsmap]
   (let [[unshifted shifted & _] (first x11-mappings)
         new-map (as-> itsmap m
                   (assoc m unshifted (conj (get m unshifted) {:kcode kcode :shift? false}))
                   (assoc m shifted   (conj (get m shifted)   {:kcode kcode :shift? true })))]
     (if (empty? x11-mappings)
       itsmap
       (recur (inc kcode) (rest x11-mappings) new-map))))

  ([x11-mappings]
   (keysym->keycode-map 0 x11-mappings {})))

(defn- raw-keysym->keycode [raw]
  (get (:keysym->keycode @x11-state) raw))

(defn keysym->keycode [ksym]
  (raw-keysym->keycode (:code (keysyms ksym))))

(defn create-client [display-name init-fun]
  "Returns an x11 client and its event channel as [client events]"
  (let [events (chan 10)
        init! (fn [error display]
                (init-x11! error display)
                (init-fun error display))
        client (nodex11.createClient #js {:display display-name} init!)]
    (.on client "event" (partial put! events))
    [client events]))

(defn- mods->x11 [mods]
  (reduce bit-or (map mod->x11 mods)))

(defn grab-key
  ([client wid owner-events modifiers keycode pointer-mode keyb-mode]
   (.GrabKey client wid
             owner-events
             (mods->x11 modifiers)
             keycode
             (grab-mode->x11 pointer-mode)
             (grab-mode->x11 keyb-mode)))

  ([client wid modifiers keysym]
   ;; keycode, shift -> keysym
   (grab-key client wid true modifiers
             (keysym->keycode keysym)
             :async :async)))

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

(defn get-keyboard-mapping [display]
  (let [c (chan)
        client display.client
        min-keycode display.min_keycode
        max-keycode display.max_keycode]
    (println "min keycode" min-keycode "#keycodes" (- max-keycode min-keycode))
    (.GetKeyboardMapping client
                         min-keycode (- max-keycode min-keycode)
                         (fn [err list]
                           (if err
                             (put! c err)
                             (put! c (js->clj list)))))
    [c min-keycode]))
