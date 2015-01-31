(ns mindmap.core-examples
  (:use [mindmap.mm-pub]
        [mindmap.util])
  (:require [mindmap.mm :as mm]))

; Make it easy to reload namespaces
;(require 'mindmap.core-examples :reload-all)
(use '[clojure.tools.namespace.repl :only (refresh)])
(refresh)

; Just gives some examples of how the core functions work.
; Warning! Anything in here with hardcoded numbers won't succeed across
; runs, because those numbers change.

(demo "foo")

(default-hypermap)
(demo (def hypermap (atom (default-hypermap))))
(demo @hypermap)

(get-mm @hypermap (:head-pointer @hypermap))

(get-head @hypermap)

(demo (get-head @hypermap))
(demo (get-cur @hypermap))

(demo (:maps @hypermap))
(demo (assoc-in @hypermap [:maps 1234] {:foo :bar}))

(demo (def anode (mm/entity {:title "Second node"})))
(demo (swap! hypermap add-node anode))

; Update our test mindmap:
(demo (swap! hypermap add-node anode))
(demo @hypermap)

(def anothernode (mm/entity {:title "Third node"}))
(demo anothernode)

(demo (swap! hypermap add-node anothernode))
(demo @hypermap)

(def cur (get-cur @hypermap))
(demo (swap! hypermap add-edge cur anode {:title "Edge 1" :type :child}))
(demo (swap! hypermap add-edge anode anothernode {:title "Edge 2" :type :child}))

; We can add a second edge between the same pair of nodes (some type of edge that I just made up)
(demo (swap! hypermap add-edge anode anothernode
             {:title "Edge 3" :type :file-contains}))
(demo (swap! hypermap add-edge anode cur {:title "Edge 4" :type :child}))

(defn print-head [hype] (ppprint (get-head hype)))
(print-head @hypermap)
(get-cur @hypermap)

(get-edges @hypermap [67 69])

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

