(ns mindmap.mm
  (:require [mindmap.util :as ut]))

(defn entity
  "Create a new system entity (which is just a map), passing in any properties
  you want it to have.  It will be given a unique id automatically. Any other
  properties you want it to have should be passed in as a map.
  eg (def mynode (entity))
  or (def mynode (entity {:title \"foo\"}).  "
  [properties]
  (let [base-props {:id (ut/main-indexer)}]
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

(defn get-entity
  "Extract an entity of some type by id"
  [mm ent-type id]
  ((ent-type mm) id))

(defn get-node
"Extract a node by id"
  [mm id]
  (get-entity mm :nodes id))

(defn get-edge
"Extract an edge by id"
  [mm id]
  (get-entity mm :edges id))

(defn update
  "Update some entity type of a mindmap (nodes or edges) by adding a new value,
  returning an updated mindmap with a new id. "
  [mm entity-type entity]
  (let [id (:id entity)
        _ (println "Adding " entity-type entity) ]
    ; nil args are probably a bad idea here -- although maybe it's fine 
    ; to be starting from a nil mindmap. 
    (assert (ut/no-nils? [mm entity-type entity]))
    ; Nodes and edges have a numeric :id, so the added value better have one
    (assert (number? (:id entity)))
    ; And the mindmap ought to have this property
    (assert (entity-type mm))

    (-> mm
        ; add the new value in the appropriate place
        (assoc-in [entity-type id] entity)
        ; and give the modified mm a new id
        (assoc :id (ut/main-indexer)))))

"Let's say for now that adjacency is represented by a map of maps: origin:dest:edge
Adjacency representation, whatever it is, should be able to be addressed as a nested map from
origin to destination to a set of edge entities (a set because we might conceivably want to
represent more than one edge between a given pair of nodes).
Graph search & filtering functions can use either all edges, or only edges with some subset
of attributes."

(defn- add-adjacency
  "Add an adjacency relationship to a mindmap, connecting two nodes through an edge.
  Return modified mindmap."
  [mm origin dest edge]
  ; set-conj is just conj, but ensures return value is a set.
  ; Handles the case where the incoming set is nil.
  (letfn [(set-conj [the-set item] (set (conj the-set item)))]
    (update-in mm
      [:adjacency (:id origin) (:id dest)]
      set-conj (:id edge))))

(defn add-edge
  "Create a new edge entity (which is a map and a corresponding adjecency relation),
  passin in any properties you want it to have."
  [mm origin dest attributes]
  (let [edge-ent (entity attributes)]
    (-> mm 
        (update :edges edge-ent)
        (add-adjacency origin dest edge-ent))))

