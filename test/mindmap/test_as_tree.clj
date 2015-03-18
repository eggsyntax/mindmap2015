(ns mindmap.test-as-tree
  (:use [mindmap.as-tree]
        [mindmap.util :as ut]
        [clojure.test])
  (:require [mindmap.mm :as mm]))


(deftest test-parent-of-1
   (let [mmap1 (mm/default-mindmap)
         cur-1 (mm/get-cur mmap1)
         mmap2 (mm/add-new-node-from mmap1 cur-1 {} {:type :child})
         cur-2 (mm/get-cur mmap2)]
     (is (= cur-1 (mm/parent-of mmap2 cur-2)))))

(deftest test-parent-of-2
   "Loop several times over mmap, adding nodes and testing to be sure
   that parents are correct."
   (let []
     (loop [mmap (mm/default-mindmap)
            i 3]
       (when (> i 0)
         (let [cur (mm/get-cur mmap)
               new-mmap (mm/add-new-node-from mmap cur {} {:type :child})
               new-cur (mm/get-cur new-mmap)]
           (is (= cur (mm/parent-of new-mmap new-cur)))
           (recur new-mmap (- i 1)))))))

(defn test-to-tree [num-nodes]
   (let [mm (mm/rand-mm)
         root (last (vals (:nodes mm)))]
     (ut/ppprint root)
     (ut/ppprint (to-tree mm root 5))))
;(test-to-tree)

(def rmm2 (mm/rand-mm))

(ut/ppprint (mm/rand-mm :num-nodes 3))

(run-tests)

