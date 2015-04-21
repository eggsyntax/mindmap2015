(ns mindmap.test-zip-mm
  (:use [clojure.test])
  (:require [mindmap.zip-mm :refer :all]
            [clojure.zip :as z]
            [mindmap.util :as ut]
            [mindmap.tree :as tr]
            [mindmap.mm :as mm]))

(deftest test-unzip-to
 (let [zipper (z/seq-zip '(1 (2 (3 4)) (5 (6))))
      unzipped-zipper (unzip-to zipper 4)
      unzipped-node (z/node unzipped-zipper)]
  (is (= unzipped-node 4))))

; Pick an arbitrary node in a random mm, make a
; zipper unzipped to it, and verify it's the right one
(deftest test-zipper-at
  (let [_ (ut/reset-indexer)
        _ (ut/reset-epoch)
        rmm (mm/rand-mm :num-nodes 8 :seed 1)
        a-node (nth (vals (:nodes rmm)) 5)
        rmm-root (mm/get-cur-root rmm)
        unzipped-zipper (zipper-at rmm a-node)
        cur (z/node unzipped-zipper)
        ]
    (is (= cur a-node))))

(run-tests)
