(ns its.kbd-test
  (:require [its.kbd :as sut]
            [cljs.test :as t :include-macros true :refer [deftest is]]))

(deftest numbertest
  (is (= 1 1)))
