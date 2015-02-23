(ns mindmap.mm
  (:require [mindmap.util :as ut]
            [clojure.set :refer [union]]))

(defrecord Entity [id])
(defrecord Relationship [origin-id dest-id id])

; Schema this:      val map   map     set       val
(defrecord Mindmap [nodes bullshit adjacency cur-pointer]) ;TODO calling bullshit temporarily to not break callers right away

(defn create-entity
  "Create a new system Entity, passing in any properties you want it
  to have.  It will be given a unique id automatically. Any other
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
     (Mindmap. {first-id first-node} {} #{} (:id first-node)))) ;TODO eliminate 2nd arg

(defn get-node
  "Extract a node Entity by id"
  [mm id]
  ((:nodes mm) id))

(defn get-cur
  "Return the current node of the Mindmap"
  [mm]
  (get-node mm (:cur-pointer mm)))

(defn get-edge
  "Extract an edge Entity by id"
  [mm id]
  (first (filter #(= (:id %) id) (:adjacency mm))))

;TODO unused. Delete?
(defn get-edges
  "Get some edges from the head of a hypermap by id"
  [mm ids]
  (for [id ids]
    (get-edge mm id)))

;TODO needs test
(defn edges-between
  [mm origin dest]
  (filter
    #(and
       (= (:origin-id %) (:id origin))
       (= (:dest-id %) (:id dest)))
    (:adjacency mm)))

;TODO needs test
; NOTE This needs to remain public for the unit test to be able to see it
;TODO rename: filter-edges-by
(defn filter-relationships-by
  "Returns a seq of relationships whose specified key-property
  (:origin-id, :dest-id, id) matches the id of the entity"
  [mm key-property entity]
  (filter #(= (key-property %) (:id entity)) (:adjacency mm)))

(defn edges-to
  "Returns a seq of all edges terminating at this node"
    [mm node]
    (let [parent-rels (filter-relationships-by mm :dest-id node)]
      (map #(get-edge mm (:id %)) parent-rels)))

(defn edges-from
  "Returns a seq of all edges originating from this node"
    [mm node]
    (let [child-rels (filter-relationships-by mm :origin-id node)]
      (map #(get-edge mm (:id %)) child-rels)))

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
  "Update some entity type of a mindmap (nodes or edges) by adding a
  new value, returning an updated mindmap with a new id. "
  [mm entity-type entity]
  (let [id (:id entity)]
    ; nil args are probably a bad idea here -- although maybe it's fine
    ; to be starting from a nil mindmap.
    (assert (ut/no-nils? [mm entity-type entity])
            "Don't pass nil args to update-entity.")
    ; And the mindmap ought to have this property
    (assert (entity-type mm)
            (str "This mindmap doesn't have the entity type "
                 entity-type))
    ; add the new value in the appropriate place
     (assoc-in mm [entity-type id] entity)))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this
  hypermap, and set it as the current node. Does not create any edges
  in the mindmap.  Return the modified hypermap."
  [mm node-attrs]
  (let [node (create-entity node-attrs)]
    (-> mm
        (update-entity :nodes node)
        (assoc :cur-pointer (:id node))
        ;TODO youarehere
        ; really all i want to do is add this here:
        ; (list node) ; Put in list along with node and return
        ; but, y'know, macrofied to work also on add-new-node-from
        )))

(defn add-edge
  "Add a relationship to a mindmap between two nodes.
  Its unidirectional connecting two nodes through an edge.
  Return modified mindmap."
  ;TODO can now just call create-entity
  [mm origin dest attributes]
  (let [temp-id nil
        new-edge (ut/with-id
                   (merge
                     (Relationship. (:id origin) (:id dest) temp-id)
                     attributes))
        new-adj-set (conj (:adjacency mm) new-edge)]
    ;TODO use assoc-in and then don't need to create new-adj-set
    (assoc mm :adjacency new-adj-set)))

; (defn add-edge-old
;   "Adds an end between the originating and destination node updating
;   the adjacency relation which represents it."
;   [mm origin dest attributes]
;   (let [edge-ent (create-entity attributes)]
;     (-> mm
;         ;(update-entity :edges edge-ent) ;TODO no longer needed?
;         (add-relationship origin dest edge-ent))))
;
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
    (let [new-adj-set (set (remove #(= (:id edge) (:id %)) (:adjacency mm)))
          new-edges (into {} (remove #(= (:id edge) (key %)) (:edges mm))) ]
        (-> mm
          (assoc :adjacency new-adj-set)
          (assoc :edges new-edges))
    )))

(defn remove-child-edges
  "Removes the child edges and adjacency information of the node from the mindmap.
  If there are no child edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [children (edges-from mm node) ]
    (reduce remove-edge mm children)))

(defn remove-parent-edges
  "Removes the edges and adjacency information of the node to it its parent from the mindmap.
  If there are no parent edges it returns the mindmap, otherwise it returns a new mindmap
  without updating the id. "
  [mm node]
  (let [parent-edges (edges-to mm node) ]
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
  (let [new-nodes (into {} (remove #(= (:id node) (key %)) (:nodes mm))) ]
    (-> mm
      (remove-child-edges node)
      (remove-parent-edges node)
      (assoc :nodes new-nodes)
    )
  ))


 (defn remove-node-and-children
   [mm node]
   (let [children (node-and-children mm node) ]
     (reduce remove-node mm children)
   ))

