(ns mindmap.core
  (:require [mindmap.util :as ut])
  (:gen-class))

; See core-examples for usage

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn node
  "Create a new node (which is just a map),
  passing in any properties you want it to have.
  eg (def mynode (node))
  or (def mynode (node {:title \"foo\"}).  "
  [properties]
  (let [base-props {:id (ut/main-indexer)
                    :title ""}]
  (merge base-props properties)))

(defn default-node []
  (node {:title "New mindmap"}))

(defn default-mindmap
  "Default mindmap contains one node, no edges."
  []
  (let [first-node (default-node)
        first-id (:id first-node)]
    {:id (ut/main-indexer)
     :nodes {first-id first-node} 
     :edges {}
     :cur-pointer first-id}))

; There's redundancy in get-mm/get-node, and in get-head/get-cur,
; which could be factored out, but I'm deliberately
; leaving them separate because a) we need to be careful to keep them
; separated in our mental models, and b) we may want to change the
; implementation of one or both.

(defn get-mm
  "Extract a mindmap by id"
  [hype id]
  ((hype :maps) id))

(defn get-head
  "Get the mindmap which is the current head of the hypermap"
  [hype]
  (get-mm hype (hype :head-pointer)))

(defn get-node
  "Extract a node by id"
  [mm id]
  ((mm :nodes) id))

(defn get-cur
  "Get the node which is the current node of a mindmap"
  [mm]
  (get-node mm (mm :cur-pointer)))

(defn get-cur-from-hype
  "Get the current node of the current head of the hypermap."
  ; Could be expressed as polymorphism on get-cur, but again
  ; I'm trying to make every effort to keep the levels distinct.
  [hype]
  (get-cur (get-head hype)))

(defn cur-val
  "Get a value from the current node"
  [hype attribute]
  (attribute (get-cur-from-hype hype))
  )

(defn is-cur?
  "Is this node the current node of this mindmap?"
  [mm anode]
  (= anode (get-cur mm) ))

(defn add-mindmap
  "Add a new mindmap to this hypermap, and an edge from the previous head to
  the new mindmap. Make the new mindmap the head."
  [hype mm]
    ; New hypermap had better include this mindmap!
    {:post [(contains? (:maps %) (:id mm))]}

    (let [;orig-maps (:maps hype)
          orig-head-id (:head-pointer hype)
          new-id (:id mm)
          new-edge-key [orig-head-id new-id]
          new-edge-val {:type :child} ]

      (-> hype
        ; Add mindmap
        (assoc-in [:maps new-id] mm)
        ; Add edge
        (assoc-in [:map-edges new-edge-key] new-edge-val)
        ; Set head pointer
        (assoc :head-pointer new-id)))
      
  )

(defn add-node
  "Add this node to the head mindmap of this hypermap, and set it as the
  current node. Does not create any edges in the mindmap. Return the modified
  hypermap."
  [hype node]

    (let [mm (get-head hype)
          id (:id node)
          ; Create a new mindmap with the new node and a new id        
          new-mm (-> mm 
                     (assoc-in [:nodes id] node) 
                     (assoc :id (ut/main-indexer)))]
      (add-mindmap hype new-mm)))

