(ns mindmap.core
  (:require [mindmap.util :as ut])
  (:gen-class))

; See core-examples for usage
; TODO consider adding Prismatic's schema for idiomatic data description
; https://github.com/Prismatic/schema

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn entity
  "Create a new system entity (which is just a map), passing in any properties
  you want it to have.  It will be given a unique id automatically. Any other
  properties you want it to have should be passed in as a map.
  eg (def mynode (entity))
  or (def mynode (entity {:title \"foo\"}).  "
  [properties]
  (let [base-props {:id (ut/main-indexer)
                    :title ""}]
  (merge base-props properties)))

(defn default-node []
  (entity {:title "New mindmap"}))

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

(defn- add-mm-val
  "Update some property of a mindmap (nodes or edges) by adding a new value,
  returning an updated mindmap with a new id. "
  [mm property val-to-add]
  (let [id (:id val-to-add)
        _ (println "Adding " property val-to-add) ]
    ; nil args are probably a bad idea here -- although maybe it's fine 
    ; to be starting from a nil mindmap. 
    (assert (ut/no-nils? [mm property val-to-add]))
    ; Nodes and edges have a numeric :id, so the added value better have one
    (assert (number? (:id val-to-add)))
    ; And the mindmap ought to have this property
    (assert (property mm))

    (-> mm
        ; add the new value in the appropriate place
        (assoc-in [property id] val-to-add)
        ; and give the modified mm a new id
        (assoc :id (ut/main-indexer)))))

(defn add-node
  "Add this node to the head mindmap of this hypermap, and set it as the
  current node. Does not create any edges in the mindmap. Return the modified
  hypermap."
  [hype node]
  (let [mm (get-head hype)
        new-mm (add-mm-val mm :nodes node)]
    (add-mindmap hype new-mm)))

"
;TODO YOUAREHERE

Should edges be stored by id like everything else, or be identified by [origin, dest]?
- entity id:
  - behavior matches the rest of the system
  - would need a separate place to store edges by [origin, dest] so that we could get
    them quickly, so then there would be some redundancy.
    But that place would be an implementation detail and could be swapped out at will
    (eg adjacency matrix vs adjacency list)
  - easy to have multiple edges between same pair of nodes
- [origin, dest]
  - less redundancy
  - better match with everything else
  - trickier to model multiple edges between same pair of nodes (would instead probably
    have to treat it as a single edge with multiple attributes)

"

(defn add-edge
  "Add an edge to the head mindmap of this hypermap. Return the modified hypermap."
  ;TODO youarehere: now add a second arity that creates an edge on demand and then
  ; calls the existing arity -- or else create a function which creates edges.
  [hype edge]
  (let [mm (get-head hype)
        new-mm (add-mm-val mm :edges edge) ]
    (add-mindmap hype new-mm)
    )
  )
