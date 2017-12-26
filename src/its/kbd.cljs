(ns ^:figwheel-always its.kbd
  (:require [clojure.string :as str]))

(def str->mods
  {"C" :control
   "L" :lock
   "M" :meta
   "s" :super
   "H" :hyper
   "S" :shift})

(def mods->x11
  {:shift   0x1
   :lock    0x2
   :control 0x4
   :meta    0x8
   :hyper   0x20
   :super   0x40})

(defn mod->x11 [mod]
  (reduce bit-or 0 (map mods->x11 mod)))

(defn parse-chord [chord]
  (let [res (str/split chord #"-")]
    [(into #{} (map str->mods (butlast res)))
     (last res)]))

(defn kbd [s]
  (map parse-chord
       (str/split s #" ")))

(defn empty-keymap [] {})

(def map-key assoc-in)
