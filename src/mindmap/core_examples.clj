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

(def cur (get-cur-from-hype @hypermap))
(demo (swap! hypermap add-edge cur anode {:title "Edge 1"}))
(demo (swap! hypermap add-edge anode anothernode {:title "Edge 2"}))
; We can add a second edge between the same pair of nodes (some edge that I just made up)
(demo (swap! hypermap add-edge anode anothernode
             {:title "Edge 3" :attributes {:type :file-contains}}))

(defn print-head [hype] (ppprint (get-head hype)))
(print-head @hypermap)
(get-cur-from-hype @hypermap)

(demo (seq @hypermap))
