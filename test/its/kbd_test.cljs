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
