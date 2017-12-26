(ns its.kbd-test
  (:require [its.kbd :as kbd]
            [cljs.test :as t :include-macros true :refer [deftest is]]))

(deftest kbd-single-key
  (is (= (kbd/kbd "a" [[#{} "a"]]))))

(deftest kbd-simple-chord
  (is (= (kbd/kbd "C-a") [[#{:control} "a"]])))

(deftest kbd-multimod-chord
  (is (= (kbd/kbd "C-M-a" [[#{:control :meta} "a"]]))))

(deftest kbd-sequence
  (is (= (kbd/kbd "C-M-a C-b M-c d")
         [[#{:control :meta} "a"]
          [#{:control} "b"]
          [#{:meta} "c"]
          [#{} "d"]])))

(deftest mod->x11 [mod]
  (is (= (kbd/mod->x11 #{:meta}) 0x8))
  (is (= (kbd/mod->x11 #{:super :hyper}) 0x60))
  (is (= (kbd/mod->x11 #{:meta :control}) 0xc))
  (is (= (kbd/mod->x11 #{:shift :meta :super} 0x49))))
