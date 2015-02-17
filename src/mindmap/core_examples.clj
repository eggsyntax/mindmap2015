(ns mindmap.core-examples
  (:use [mindmap.hm :as mm-pub]
        [mindmap.util])
  (:require [mindmap.mm :as mm]))


; Just gives some examples of how the core functions work.
; Warning! Anything in here with hardcoded numbers won't succeed across
; runs, because those numbers change.

(demo "foo")

(default-hypermap)
(demo (def hypermap (atom (default-hypermap))))
(def firstnode (get-cur @hypermap))
(demo @hypermap)

(get-mm @hypermap (:head-pointer @hypermap))

(demo (get-cur @hypermap))

(demo (:maps @hypermap))
(demo (assoc-in @hypermap [:maps 1234] {:foo :bar}))

(demo (def anode (mm/create-entity {:title "Second node"})))
(demo (swap! hypermap add-node anode))
(demo (add-node @hypermap anode))
(demo @hypermap)

(println (hash anode))
(def anothernode (mm/create-entity {:title "Third node"}))
(println (hash anothernode))
(demo anothernode)

(demo (swap! hypermap add-node anothernode))
(demo @hypermap)

(demo (swap! hypermap add-edge firstnode anode {:title "Edge 1" :type :child}))
(demo (swap! hypermap add-edge anode anothernode {:title "Edge 2" :type :child}))

; We can add a second edge between the same pair of nodes (some type of edge that I just made up)
(demo (swap! hypermap add-edge anode anothernode
             {:title "Edge 3" :type :file-contains}))
(demo (swap! hypermap add-edge anode firstnode {:title "Edge 4" :type :child}))

(print-head @hypermap)
(get-cur @hypermap)

(demo (mm/add-node-returning-mm-and-node (get-head @hypermap) {:title "TEMP NODE"}))
(mm/get-edges @hypermap [67 69])

(get-head @hypermap)
((:nodes (get-head @hypermap)) 101)
(mm/get-node (get-head @hypermap) 101)

; apply-filters example
(def f-list [even?
             #(> % 3)
             #(< % 8)])
(apply-filters f-list (range 11))
(apply-filters nil (range 11))

(demo (edges-from @hypermap anode))

(def ef (edges-from @hypermap anode))
(:nodes (get-head @hypermap))
; (:type (:id anode) ((:nodes (get-head @hypermap)) ))
(def edges (:edges (get-head @hypermap)))
(println edges)

(mm/get-node (get-head @hypermap) (:id anode))
(def child-filter #(= (:type (mm/get-node (get-head @hypermap) %)) :child))
(def test-filter #(= (:type %) :child))

(print-head @hypermap)
(demo (edges-from @hypermap anode))
(demo (apply-filters
        [child-filter]
        (edges-from @hypermap anode)))

; For a single filter, we could just do "(filter my-filter" instead of "apply-filters [my-filter]"
(demo (apply-filters [test-filter] (edges-from @hypermap anode)))

