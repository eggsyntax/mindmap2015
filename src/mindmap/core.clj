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

(defn node
  "Create a new node (which is just a map),
  passing in any properties you want it to have.
  eg (def mynode (node))
  or (def mynode (node {:title \"foo\"}).
  Node will be given a unique id by default, but
  you can override it (if you REALLY want) in properties."
  [properties]
  (merge {:id (ut/main-indexer)} properties)
  )

(defn default-node []
  (node {:title "New mindmap"}))

(:id (default-node) )

(defn default-mindmap
  "Default mindmap contains one node, no edges."
  []
  (let [first-node (default-node)
        first-id (:id first-node)]
    {:id (ut/main-indexer)
     :nodes {first-id first-node} 
     :edges {}
     :cur-pointer first-id}))

(def hypermap
  (let [first-mindmap (default-mindmap)
        first-id (:id first-mindmap)]
    (atom {:id (ut/main-indexer)
           :maps {first-id first-mindmap}
           :map-edges {}
           :head-pointer first-id
           })))

; Just for the sake of later convenience in testing, grab the
; id of the default node we created. We'll later define functions
; to do this less awkwardly.
(def root-node-id
  (let [head-map  ((:maps @hypermap) (@hypermap :head-pointer))]
    (:cur-pointer head-map)) )


(print (ut/to-str @hypermap))

; There's redundancy in get-mm/get-node, and in get-head/get-cur,
; which could be factored out, but I'm deliberately
; leaving them separate because a) we need to be careful to keep them
; separated in our mental models, and b) we may want to change the
; implementation of one or both.

(defn get-mm
  "Extract a mindmap by id"
  [hype id]
  ((hype :maps) id))

(get-mm @hypermap (:head-pointer @hypermap))

(defn get-head
  "Get the mindmap which is the current head of the hypermap"
  [hype]
  (get-mm hype (hype :head-pointer)))

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

(defn get-cur-from-hype
  "Get the current node of the current head of the hypermap."
  ; Could be expressed as polymorphism on get-cur, but again
  ; I'm trying to make every effort to keep the levels distinct.
  [hype]
  (get-cur (get-head hype)))

(get-cur-from-hype @hypermap)

(defn cur-val
  "Get a value from the current node"
  [hype attribute]
  (attribute (get-cur-from-hype hype))
  )

(cur-val @hypermap :title)

(print head-map)
(print (ut/to-str @hypermap))

(defn is-cur?
  "Is this node the current node of this mindmap?"
  [anode mm]
  (= anode (get-cur mm) ))

(:maps @hypermap)
(assoc-in sr [1 :maps 10] 11)
(assoc-in @hypermap [:maps 1234] {:foo :bar})

(defn add-mindmap
  "Add a new mindmap to this hypermap, and an edge from the previous head to
  the new mindmap."
  [hype mm]
  (-> hype
      ; Add mindmap
      (let [orig-maps (:maps hype)
            id (:id mm)]
        (assoc-in hypermap [:maps id] mm))

      ; Add edge
      )
  )

(print (ut/to-str @hypermap))

;TODO YOUAREHERE test add-mindmap and then go back and handle adding edges.

(defn add-node
  "Add this node to the head mindmap of this hypermap
  (creating a new mindmap, of course). Does *not* create any edges in the
  mindmap."
  []
  )

(get-cur head-map)
(get-node head-map root-node-id)
(is-cur? (get-node head-map root-node-id) head-map)
(is-cur? (get-node head-map (+ 1 root-node-id)) head-map)
