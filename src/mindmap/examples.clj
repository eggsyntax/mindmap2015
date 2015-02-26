(ns mindmap.examples
  (:use [mindmap.ht]
        [mindmap.util])
  (:require [mindmap.mm :as mm]))

(reset! debug-mode true) ; Uncomment and eval as desired
;(r!) ; Refresh REPL before run

; Just gives some examples of how the core functions work.
; Warning! Anything in here with hardcoded numbers won't succeed across
; runs, because those numbers change.

(demo "foo")

(def hypermap (atom (default-hypertree)))
(demo @hypermap)

(def node1 (get-cur @hypermap))
(demo @hypermap)

(demo (swap! hypermap add-node {:title "Node 2"} {}))
(def node2 (get-cur @hypermap))
(demo node2)

(demo (swap! hypermap add-node {:title "Node 3"} {}))
(def node3 (get-cur @hypermap))
(demo node3)

(demo (swap! hypermap add-edge node1 node2
             {:title "Edge 1" :type :child} {}))
(demo (swap! hypermap add-edge node2 node3
             {:title "Edge 2" :type :child} {}))
; We can add a second edge between the same pair of nodes (some type of edge that I just made up)
(demo (swap! hypermap add-edge node2 node3
             {:title "Edge 3" :type :file-contains} {}))
(demo (swap! hypermap add-edge node1 node3
             {:title "Edge 4" :type :child} {}))

(demo (def head (get-head @hypermap)))

(print-head @hypermap)
(mm/get-edge head 222)

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
(def test-filter #(= (:type %) :child))

; Make sure we can create and add edges properly
(get (:nodes (get-head @hypermap)) 33)
(demo (mm/create-edge node3 node1 {:title "Edge n3->n1" :type :child}))
(demo (mm/add-edge-new (get-head @hypermap) node3 node1
                       {:title "Edge n3->n1" :type :child}))
(print-head @hypermap)

; Returns two edges
(demo (edges-from @hypermap node2))
; Returns 1 edge of those 2, the one that's a child.
(demo (apply-filters [test-filter] (edges-from @hypermap node2)))

; For a single filter, we could just do "(filter my-filter" instead of "apply-filters [my-filter]"
(demo (apply-filters [test-filter] (edges-from @hypermap node1)))

