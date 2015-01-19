(ns mindmap.core-examples
  (:use [mindmap.core])
  (:require [mindmap.util :as ut]))

; Just gives some examples of how the core functions work.

; Create a hypermap for testing
(def hypermap
  (let [first-mindmap (default-mindmap)
        first-id (:id first-mindmap)]
    (atom {:id (ut/main-indexer)
           :maps {first-id first-mindmap}
           :map-edges {}
           :head-pointer first-id
           })))

; Just for the sake of convenience, grab the
; id of the default node we created. We'll later define functions
; to do this less awkwardly.
(def root-node-id
  (let [head-map  ((:maps @hypermap) (@hypermap :head-pointer))]
    (:cur-pointer head-map)) )

(print (ut/to-str @hypermap))

(get-mm @hypermap (:head-pointer @hypermap))

(get-head @hypermap)

(def head-map (get-head @hypermap))
(get-cur head-map)

(get-cur-from-hype @hypermap)

(cur-val @hypermap :title)

(print head-map)
(print (ut/to-str @hypermap))

(get-cur head-map)
(get-node head-map root-node-id)
(is-cur? head-map (get-node head-map root-node-id))
(is-cur? head-map (get-node head-map (+ 1 root-node-id)))

(:maps @hypermap)
(assoc-in @hypermap [:maps 1234] {:foo :bar})

(print (ut/to-str @hypermap))
(print (ut/to-str (add-mindmap @hypermap (node {:foo :bar}))))

(def anode (node {:title "Second node"}))
(ut/ppprint (add-node @hypermap anode))

; We can now update our test mindmap:
(swap! hypermap add-node anode)
(ut/ppprint @hypermap)
