(ns mindmap.core
  (:require [mindmap.util :as ut])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

"
Hypermap
  Mindmap
    Node
    Edge
    Cur"

(defn default-node [] {:id (ut/main-indexer)
                       :title "New mindmap"})
(default-node)

(defn default-mindmap
  "Default mindmap contains one node, no edges."
  []
  (let [first-node (default-node)
        first-id (:id default-node)]
    {:id (ut/main-indexer)
     :nodes {first-id first-node} 
     :edges {}
     :cur-pointer first-id}))

(def hypermap
  (let [first-mindmap (default-mindmap)
        first-id (:id first-mindmap)]
    (atom {:id (ut/main-indexer)
           :maps {first-id first-mindmap}
           :head-pointer first-id
           })))

(print (ut/to-str @hypermap))

; There's a lot of redundancy in get-mm/get-node, and in
; get-head/get-cur, which could be factored out, but I'm deliberately
; leaving them separate because a) we need to be careful to keep them
; separated in our mental models, and b) we may want to change the
; implementation of one or both.

(defn get-mm
  "Extract a mindmap by id"
  [hm id]
  ((hm :maps) id))

(get-mm @hypermap 6)

(defn get-head
  "Get the mindmap which is the current head of the hypermap"
  [hm]
  (get-mm hm (hm :head-pointer)))

(get-head @hypermap)

(defn get-node
  "Extract a node by id"
  [mm id]
  ((mm :nodes) id))

(defn get-cur
  "Get the node which is the current node of a mindmap"
  [mm]
  (get-node mm (mm :cur-pointer)))

(def head-map (get-head @hypermap))
(get-cur head-map)

(defn get-cur-from-hm
  "Get the current node of the current head of the hypermap."
  ; Could be expressed as polymorphism on get-cur, but again
  ; I'm trying to make every effort to keep the levels distinct.
  [hm]
  (get-cur (get-head hm)))

(get-cur-from-hm @hypermap)

(defn cur-val
  "Get a value from the current node"
  [hm attribute]
  (attribute (get-cur-from-hm hm))
  )

(cur-val @hypermap :title)

