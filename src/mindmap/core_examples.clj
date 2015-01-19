(ns mindmap.core-examples
  (:use [mindmap.core]
        [mindmap.util]))

; Just gives some examples of how the core functions work.


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

(def anedge (entity {:name "Devil-edge"}))
(demo (add-edge @hypermap anedge))

(defn print-head [hype] (ppprint (get-head hype)))
(print-head @hypermap)
(get-cur-from-hype @hypermap)
(swap! hypermap add-node )
