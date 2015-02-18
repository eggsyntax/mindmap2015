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
  [mm node-attrs]
  (let [node (create-entity node-attrs)]
    (-> mm
        (update-entity :nodes node)
        (assoc :cur-pointer (:id node)))))

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
  [mm parent node-attrs edge-attrs]
  (let [new-map (add-node mm node-attrs)
        new-node (get-cur new-map) ]
    (add-edge new-map parent new-node edge-attrs)))

(defn remove-edge
  "Removes the edge and any adjacency information from the mindmap. Returns new mindmap."
  [mm edge]
  (if (empty? edge)
    mm
    (let [new-adj-set (set (remove #(= (:id edge) (:edge-id %)) (:adjacency mm)))
          ;_ (println "r-e> New-Set: " new-adj-set)
          new-edges (into {} (remove #(= (:id edge) (key %)) (:edges mm)))
          ;_ (println "r-e> New Edges: " new-edges)
          ]
        (-> mm
          (assoc :adjacency new-adj-set)
          (assoc :edges new-edges))
    )))

(defn remove-child-edges
  "Removes the child edges and adjacency information of the node from the mindmap.
  If there are no child edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [children (edges-from mm node)
        _ (println "r-c-e> Children Edges: ")
        _ (ut/ppprint children)
        ]
    (reduce remove-edge mm children)))

(defn remove-parent-edges
  "Removes the edges and adjacency information of the node to it its parent from the mindmap.
  If there are no parent edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [parent-edges (edges-to mm node)
        _ (println "r-p-ce> Parent Edges: ")
        _ (ut/ppprint parent-edges)
        ]
      (reduce remove-edge mm parent-edges)))

(defn node-and-children
  "Does a DFS and returns a seq that is the node and all its children"
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
  (println "r-n> Node: " node)
  (let [new-nodes (into {} (remove #(= (:id node) (key %)) (:nodes mm)))
        _ (println "r-n> New Nodes:")
        _ (ut/ppprint new-nodes)
        ]
    (-> mm
      (remove-child-edges node)
      (remove-parent-edges node)
      (assoc :nodes new-nodes)
      (ut/ppprint )
    )
  ))


 (defn remove-node-and-children
   [mm node]
   (let [children (node-and-children mm node)
         _ (println "rnac> List: " )
         _ (ut/ppprint children)
         ]
     (reduce remove-node mm children)
   ))

