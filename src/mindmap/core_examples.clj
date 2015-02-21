(ns mindmap.core-examples
  (:use [mindmap.hm :as hm]
        [mindmap.util :as ut])
  (:require [mindmap.mm :as mm]))

(reset! debug-mode true) ; Uncomment and eval as desired
;(r!) ; Refresh REPL before run

; Just gives some examples of how the core functions work.
; Warning! Anything in here with hardcoded numbers won't succeed across
; runs, because those numbers change.

(demo "foo")

(def hypermap (atom (hm/default-hypermap)))
(demo @hypermap)

(def node1 (get-cur @hypermap))
(demo @hypermap)

(demo (swap! hypermap add-node {:title "Node 2"}))
(def node2 (get-cur @hypermap))
(demo node2)

(demo (swap! hypermap add-node {:title "Node 3"}))
(def node3 (get-cur @hypermap))
(demo node3)

(demo (swap! hypermap add-edge node1 node2 {:title "Edge 1" :type :child}))
(demo (swap! hypermap add-edge node2 node3 {:title "Edge 2" :type :child}))

; We can add a second edge between the same pair of nodes (some type of edge that I just made up)
(demo (swap! hypermap add-edge node2 node3 {:title "Edge 3" :type :file-contains}))
(demo (swap! hypermap add-edge node1 node3 {:title "Edge 4" :type :child}))

(print-head @hypermap)
(demo (get-cur @hypermap))

; apply-filters example
(def f-list [even?
             #(> % 3)
             #(< % 8)])
(apply-filters f-list (range 11))
(apply-filters nil (range 11))

(demo (edges-from @hypermap node2))

(def ef (edges-from @hypermap node2))
(:nodes (get-head @hypermap))
(def edges (:edges (get-head @hypermap)))
(println edges)

(mm/get-node (get-head @hypermap) (:id node3))
(def child-filter #(= (:type (mm/get-node (get-head @hypermap) %)) :child))
(def test-filter #(= (:type %) :child))

(print-head @hypermap)
(demo (edges-from @hypermap node1))
(demo (apply-filters
        [child-filter]
        (edges-from @hypermap node1)))

; For a single filter, we could just do "(filter my-filter" instead of "apply-filters [my-filter]"
(demo (apply-filters [test-filter] (edges-from @hypermap node1)))

