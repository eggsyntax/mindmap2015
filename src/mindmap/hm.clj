(ns mindmap.hm
  (:require [mindmap.util :as ut]
            [mindmap.mm :as mm])
  (:gen-class))

;TODO
"
 o Validation (Prismatic Schema, validators?)
 o unit tests
 o test for idempotency on (at least) most of these
"
            
;                    {}    {}      Id of MM
; (defrecord Edge [id]) ? What will this end up looking like ?
(defrecord Hypermap [maps map-edges head-pointer])

(defn default-hypermap
  []
  (let [first-mindmap (mm/default-mindmap)
        first-id (ut/gen-id (ut/timestamp))]
    (Hypermap. {first-id first-mindmap} {} first-id)))

(defn get-mm
  "Extract a mindmap by id"
  [hyper id]
  ((:maps hyper) id))

(defn get-head
  "Get the mindmap which is the current head of the hyperrmap"
  [hyper]
  (get-mm hyper (hyper :head-pointer)))

(defn get-cur
  "Return the current node of the current head of the hypermap."
  [hyper]
  (let [head (get-head hyper)]
    (mm/get-cur head)) )

(defn- commit-mindmap
  "Commit a modified mindmap to this hypermap, and an edge from the previous head to
  the new mindmap. Make the new mindmap the head."
  [hyper mm]
    ; New hypermap had better include this mindmap!
    {:post [(contains? (:maps %) (:id mm))]}

    (let [orig-head-id (:head-pointer hyper)
          new-id (ut/with-id mm)
          new-edge-key [orig-head-id new-id]
          new-edge-val {:type :child} ]

      (-> hyper
        ; Add mindmap
        (assoc-in [:maps new-id] mm)
        ; Add edge
        (assoc-in [:map-edges new-edge-key] new-edge-val)
        ; Set head pointer
        (assoc :head-pointer new-id))))

(defn set-cur
  "Sets node to the current node of the head of the hypermap."
  [hyper node]
  (let [mm (get-head hyper)
        new-mm (mm/set-cur mm node)]
    (commit-mindmap hyper new-mm)))

(defn edges-between
  [hyper origin dest]
  (mm/edges-between (get-head hyper) origin dest))

(defn edges-from
  "Returns a coll of edges originating from this node."
  [hyper node]
  (mm/edges-from (get-head hyper) node))

(defn edges-to
  "Returns a coll of edges originating from this node."
  [hyper node]
  (mm/edges-to (get-head hyper) node))

(defn child-nodes
  "Returns a seek of all immediate children of the node"
  [hyper node]
  (mm/child-nodes (get-head hyper) node))

(defn node-and-children
  "Returns a coll consisting of Nodes that are DFS-ordering of a node
  and its children"
  [hyper node]
  (mm/node-and-children (get-head hyper) node))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this hypermap,
  and set it as the current node. Does not create any edges in the mindmap.
  Return the modified hypermap."
  [hyper attributes]
  (let [mm (get-head hyper)
        new-mm (mm/add-node mm attributes)]
    (commit-mindmap hyper new-mm)))

(defn add-new-node-from
  "Adds a child node to the given node with the given attributes to the head mindmap of 
  this hypermap, and set it as the current node. 
  Return the modified hypermap."
  [hyper parent attributes]
  (let [mm (get-head hyper)
        new-mm (mm/add-new-node-from mm parent attributes)]
    (commit-mindmap hyper new-mm)))

(defn add-edge
  "Add an edge to tee head mindmap of this hypermap. Return the modified hypermap.
  Parameters:
    Hypermap
    Origin node
    Destination node
    Map of attributes you would like the edge to have. id will be added automatically."
  ; Consider interning edges for performance. http://nyeggen.com/post/2012-04-09-clojure/
  [hyper origin dest attributes]
  (let [mm (get-head hyper)
        new-mm  (mm/add-edge mm origin dest attributes)]
    (commit-mindmap hyper new-mm)))

(defn remove-node
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypermap."
  ; Question: if we remove the current head node what happens ?
  [hyper node]
  (let [mm (get-head hyper)
        new-mm
        (mm/remove-node mm node)]
    (commit-mindmap hyper new-mm)))

(defn remove-node-and-children
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypermap."
  ; Question: if we remove the current head node what happens ?
  [hyper node]
  (let [mm (get-head hyper)
        new-mm (mm/remove-node-and-children mm node)]
    (commit-mindmap hyper new-mm)))

(defn remove-edge
  "Removes an edge from the head of the hypermap. Return the modified hypermap."
  [hyper edge]
  (let [mm (get-head hyper)
        new-mm (mm/remove-edge mm edge)]
    (commit-mindmap hyper new-mm)))
(defn add-new-node-from
  "Add a new node as the child of the parent node making the child the current node."
  [hyper parent child-attrs edge-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/add-new-node-from mm parent child-attrs edge-attrs)]
    (commit-mindmap hyper new-mm)))
