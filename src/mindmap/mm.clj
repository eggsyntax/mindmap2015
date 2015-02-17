(ns mindmap.mm
  (:require [mindmap.util :as ut]
            [clojure.set :refer [union]]))

(defrecord Entity [id])
(defrecord Relationship [origin-id dest-id edge-id])

; Schema this:      val map   map     set       val
(defrecord Mindmap [nodes edges adjacency cur-pointer])

(defn create-entity
  "Create a new system Entity, passing in any properties
  you want it to have.  It will be given a unique id automatically. Any other
  properties you want it to have should be passed in as a map.
  eg (def mynode (entity))
  or (def mynode (entity {:title \"foo\"}).  "
  [properties]
    (ut/with-id
      (merge (Entity. nil) {:timestamp (ut/timestamp)} properties)))

(defn default-node
  []
  (create-entity {:title "Node 1"}))

(defn default-mindmap
  "Default mindmap contains one node, no edges."
  []
  (let [first-node (default-node)
        first-id (:id first-node)]
;     (ut/with-id
;       {:nodes       {first-id first-node}
;        :edges       {}
;        :cur-pointer first-id})))
     (Mindmap. {first-id first-node} {} #{} (:id first-node))))

(defn get-entity
  "Extract an entity of some type by id"
  [mm ent-type id]
  ((ent-type mm) id))

(defn get-node
  "Extract a node Entity by id"
  [mm id]
  (get-entity mm :nodes id))

(defn get-cur
  "Return the current node of the Mindmap"
  [mm]
  (get-node mm (:cur-pointer mm)))

(defn get-edge
  "Extract and edge Entity by id"
  [mm id]
  (get-entity mm :edges id))

(defn get-edges
  "Get some edges from the head of a hypermap by number"
  [mm edge-ids]
  (for [edge-id edge-ids]
    (get (:edges mm) edge-id)))

(defn- relationships-between
  [mm origin dest]
  (filter 
    #(and 
       (= (:origin-id %) (:id origin)) 
       (= (:dest-id %) (:id dest))) 
    (:adjacency mm)))

(defn edges-between
  [mm parent child]
  (let [child-rels (relationships-between mm parent child)]
    (map #(get-edge mm (:edge-id %)) child-rels)))

; NOTE This needs to remain public for the unit test to be able to see it
(defn- filter-relationships-by
  "Returns a seq of relationship's who's specified key-property match's
  the id of the entity"
  [mm entity-type entity]
  (filter #(= (entity-type %) (:id entity)) (:adjacency mm)))

 (defn edges-from
  "Returns a seq of all edges originating from this node"
    [mm node]
    (let [child-rels (filter-relationships-by mm :origin-id node)]
      (map #(get-edge mm (:edge-id %)) child-rels)))

 (defn edges-to
  "Returns a seq of all edges terminating at this node"
    [mm node]
    (let [par-rels (filter-relationships-by mm :dest-id node)]
      (map #(get-edge mm (:edge-id %)) par-rels)))

(defn child-nodes
  "Returns a seq of all children node Entities of this node"
  [mm node]
  (let [child-rels (filter-relationships-by mm :origin-id node)]
    (map #(get-node mm (:dest-id %)) child-rels)))

(defn parent-nodes
  "Returns a seq of all parent node Entities of this node"
  [mm node]
  (let [par-rels (filter-relationships-by mm :dest-id node)]
    (map #(get-node mm (:origin-id %)) par-rels)))

(defn set-cur
  "Get the current node Entity of the mindmap"
  [mm node]
  (assoc mm :cur-pointer (:id node)))

(defn update-entity
  "Update some entity type of a mindmap (nodes or edges) by adding a new value,
  returning an updated mindmap with a new id. "
  [mm entity-type entity]
  (let [id (:id entity)]
    ; nil args are probably a bad idea here -- although maybe it's fine
    ; to be starting from a nil mindmap.
    (assert (ut/no-nils? [mm entity-type entity]))
    ; And the mindmap ought to have this property
    (assert (entity-type mm))
    ; add the new value in the appropriate place
     (assoc-in mm [entity-type id] entity)))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this hypermap,
  and set it as the current node. Does not create any edges in the mindmap.
  Return the modified hypermap."
  [mm attributes]
  (let [node (create-entity attributes)]
    (-> mm
        (update-entity :nodes node)
        (assoc :cur-pointer (:id node))
        )))

(defn- add-relationship
  "Add a relationship to a mindmap between two nodes.
  Its unidirectional connecting two nodes through an edge.
  Return modified mindmap."
  [mm origin dest edge]
  (let [new-relationship (Relationship. (:id origin) (:id dest) (:id edge))
        new-adj-set (conj (:adjacency mm) new-relationship)]
    (assoc mm :adjacency new-adj-set)))


(defn add-edge
  "Adds an end between the originating and destination node updating the
   adjacency relation which represents it."
  [mm origin dest attributes]
  (let [edge-ent (create-entity attributes)]
    (-> mm
        (update-entity :edges edge-ent)
        (add-relationship origin dest edge-ent))))

(defn add-new-node-from
  "Add a new node as the child of the parent node making the child the current node."
  [mm parent child-attrs edge-attrs]
  (let [child (create-entity child-attrs)]
    (-> mm
      (update-entity :nodes child)
      (add-edge parent child edge-attrs)
      (assoc :cur-pointer (:id child)))))

(defn- remove-edge-no-inc
  "Removes the edge and any adjacency information from the mindmap
  Returns new mindmap without updating the id."
  [mm edge]
  (let [new-adj-set (set (remove #(= (:id edge) (:edge-id %)) (:adjacency mm)))
        new-edges (into {} (remove #(= (:id edge) (key %)) (:edges mm)))]
    (-> mm
      (assoc :adjacency new-adj-set)
      (assoc :edges new-edges))))

(defn remove-child-edges
  "Removes the child edges and adjacency information of the node from the mindmap.
  If there are no child edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [children (edges-from mm node)]
    (if (empty? children)
      mm
      (apply remove-edge-no-inc mm children))))

(defn remove-parent-edges
  "Removes the edges and adjacency information of the node to it its parent from the mindmap.
  If there are no parent edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [parent-edges (edges-to mm node)]
    (if (empty? parent-edges)
      mm
      (apply remove-edge-no-inc mm parent-edges))))

(defn remove-edge
  "Removes the edge and any adjacency information from the mindmap incrementing
  the id of the map. Returns new mingmap with updated id."
  [mm edge]
  (-> mm
      (remove-edge-no-inc edge)))

; Need to make this recursive on node children
(defn node-descendents
  "Does a DFS and returns a seq that is the node and all its descendents"
  [mm node]
  ; Use a stack to do a DFS travesal of all the nodes to remove
  (loop [nodes []
         explored #{}
         frontier [node]]
      (if (empty? frontier)
        nodes
        (let [n (peek frontier)
              children (child-nodes mm n)]
          (recur
            (conj nodes n)
            (into explored children)
            (into (pop frontier) (remove explored children)))))))


  (defn remove-node
   [mm node]
   ; Remove all adges to and from this node, then remove the node
;    (-> mm
;      (remove-child-edges node)
;      (remove-parent-edges node)
;      (assoc :nodes new-nodes))

    )
;
; (defn remove-node-and-children
;   [mm node]
;   (let [descendents (node-descendents mm node)
;         cur-mm mm
;         ]
;
;     (for [nd descendents
;           :let cur-mm (remove-node cur-mm nd)]
;       ))
;   )

