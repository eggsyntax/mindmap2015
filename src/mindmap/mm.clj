 (ns mindmap.mm
  (:require [mindmap.util :as ut]
            [clojure.set :refer [union]]))

 "
 There might be an interesting argument that we shouldn't even have nodes or edges in the mindmap,
 just a hashmap of entities by id, each of which has a type, eg :node or :edge. Then getting all
 nodes just means filtering entities on :type :node. But I suppose that's just begging for
 efficiency problems.
 "

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

 (defn get-edges
   "Get some edges from the head of a hypermap by number"
   [mm edge-ids]
   (for [edge-id edge-ids]
     (get (:edges mm) edge-id)))

 (defn edges-from
   "Return all edges originating at this node"
   ([mm node]
     (apply union ; they come out as a list of sets which must be joined
            (let [adjacency (:adjacency mm)]
              ; for each origin, for each destination, return the related edge
              (for [[origin dest-struct] adjacency :when (= origin (:id node))
                    [dest edges-ids] dest-struct]
                (get-edges mm edges-ids)
                )))))
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

 (defn- remove-adjacency
   "Remove an adjacency relationship from a mindmap"
   [mm origin dest]
   (let [adj (:adjacency mm)]
     (println adj)
     ; format of adj {from-id
     ;               {to-id #{edge-ids}}

     ))

(defn add-edge
  "Create a new edge entity (which is a map and a corresponding adjecency relation),
  passin in any properties you want it to have."
  [mm origin dest attributes]
  (let [edge-ent (entity attributes)]
    (-> mm
        (update :edges edge-ent)
        (add-adjacency origin dest edge-ent))))

 (defn remove-edge
   "Removes the edge and any adjacency information from the map"
   [mm edge]
   ; remove all relevant adjacency relationships
   ; remove this edge from the  map
   (println edge))

 (defn remove-node
   "Removes the node, all edges originating from or ending at this node and updates
   the adjacency relationships."
   [mm node]
   ; filter for all edges
   (println node))

