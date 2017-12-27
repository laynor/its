(ns ^:figwheel-always its.util
  (:require [camel-snake-kebab.core :refer [->kebab-case]]))

(defn keywordize [s]
  (keyword (->kebab-case s)))

(defn lispify-kv [[k v]]
  [(keywordize k) v])

(defn lispify [obj]
  (into {}
        (map lispify-kv)
        (js->clj obj :keywordize-keys true)))

(defn lispify-event [ev]
  (-> (lispify ev)
      (update :name keywordize)))
