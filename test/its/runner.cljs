(ns its.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [its.core-test]
            [its.kbd-test]))

(doo-tests 'its.kbd-test
           'its.core-test)
