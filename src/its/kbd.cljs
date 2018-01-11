(ns ^:figwheel-always its.kbd
  (:require [clojure.string :as str]
            [its.x11 :as x11]))

;;;
;;; keybindings:
;;; chord: str->x11 => modmap + keysym
;;; seq  : kbd => [[keysym modmap] [keysym modmap]]


(def str->mods
  {"C" :control
   "L" :lock
   "M" :meta
   "s" :super
   "H" :hyper
   "S" :shift})

(defn parse-chord [chord]
  (let [res (str/split chord #"-")]
    [(into #{} (map str->mods (butlast res)))
     (last res)]))

(defn kbd [s]
  (map parse-chord
       (str/split s #" ")))

(defn empty-keymap [] {})

(def map-key assoc-in)

(defn defkey [keymap keyseq command]
  (swap! keymap map-key keyseq command))

;; TODO: get from keyboard mappings
;;       also needed for upper?, to work on different layouts.
(defn mods->x11mods [mods shift]
  (let [->x11 {:shift   :shift
               :lock    :lock
               :control :control
               :meta    :mod1
               :numlock :mod2
               :hyper   :mod3
               :super   :mod4
               :altgr   :mod5}]
    (into (if shift #{:shift} #{}) (map ->x11 mods))))

(defn upper? [key]
  false)

(defn chord->x11 [[mods key]]
  [(mods->x11mods mods (upper? key))
   (->keysym key)])
