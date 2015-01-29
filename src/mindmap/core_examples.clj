(ns mindmap.core-examples
  (:use [mindmap.core]
        [mindmap.util]))

; Just gives some examples of how the core functions work.
; Warning! Anything in here with hardcoded numbers won't succeed across
; runs, because those numbers change.

(demo "foo")

; Create a hypermap for testing
(def hypermap
  (let [first-mindmap (default-mindmap)
        first-id (:id first-mindmap)]
    (atom {:id (main-indexer)
           :maps {first-id first-mindmap}
           :map-edges {}
           :head-pointer first-id
           })))

(demo @hypermap)
(demo @hypermap)

(get-mm @hypermap (:head-pointer @hypermap))

(get-head @hypermap)

(def head-map (get-head @hypermap))
(get-cur head-map)

(get-cur-from-hype @hypermap)

(cur-val @hypermap :title)

(demo (:maps @hypermap))
(demo (assoc-in @hypermap [:maps 1234] {:foo :bar}))

(demo (add-mindmap @hypermap (entity {:title "Fake new map"})))
(def anode (entity {:title "Second node"}))
(demo (add-node @hypermap anode))

; Update our test mindmap:
(demo (swap! hypermap add-node anode))
(demo @hypermap)

(def anothernode (entity {:title "Third node"}))
(demo anothernode)

(demo (swap! hypermap add-node anothernode))
(demo @hypermap)

(def cur (get-cur-from-hype @hypermap))
(demo (swap! hypermap add-edge cur anode {:title "Edge 1" :type :child}))
(demo (swap! hypermap add-edge anode anothernode {:title "Edge 2" :type :child}))

; We can add a second edge between the same pair of nodes (some type of edge that I just made up)
(demo (swap! hypermap add-edge anode anothernode
             {:title "Edge 3" :type :file-contains}))
(demo (swap! hypermap add-edge anode cur {:title "Edge 4" :type :whatev}))

(defn print-head [hype] (ppprint (get-head hype)))
(print-head @hypermap)
(get-cur-from-hype @hypermap)

(get-edges @hypermap [67 69])

(get-head @hypermap) 
((:nodes (get-head @hypermap)) 101)
(get-node (get-head @hypermap) 101)

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

(get-node (get-head @hypermap) (:id anode))
(def child-filter #(= (:type (get-node (get-head @hypermap) %)) :child))
(def test-filter #(= (:type %) :child))

(demo (edges-from @hypermap anode))
(demo (apply-filters
        [test-filter]
        (edges-from @hypermap anode))) 

(demo (edges-from @hypermap anode [test-filter]))

(demo (edges-from @hypermap anode ))

