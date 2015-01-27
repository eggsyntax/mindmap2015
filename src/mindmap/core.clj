(ns mindmap.core
  (:require [mindmap.util :as ut]
            [clojure.set :refer [union]])
  (:gen-class))

; See core-examples for usage
; TODO consider adding Prismatic's schema for idiomatic data description
; https://github.com/Prismatic/schema

"

TODO

Timestamps (which only hypermap nodes have)
separate out per-context functionality


Node ID remains constant when node changes (eg new title)
    - double-check existing code


Nodes and edges don't know anything about each other. An edge doesn't know what nodes it connects. Only the mindmap knows.
"

"
There might be an interesting argument that we shouldn't even have nodes or edges in the mindmap,
just a hashmap of entities by id, each of which has a type, eg :node or :edge. Then getting all
nodes just means filtering entities on :type :node. But I suppose that's just begging for
efficiency problems.
"

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

; (defn get-node
;   "Extract a node by id"
;   [mm id]
;   ((mm :nodes) id))
 
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
  ; TODO does adding a node make it cur?
  [hype node]
  (let [mm (get-head hype)
        new-mm (add-mm-val mm :nodes node)]
    (add-mindmap hype new-mm)))

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
  "Add an edge to the head mindmap of this hypermap. Return the modified hypermap.
  Parameters:
    Hypermap
    Origin node
    Destination node
    Map of attributes you would like the edge to have. id will be added automatically."
  ; Consider interning edges for performance. http://nyeggen.com/post/2012-04-09-clojure/
  [hype origin dest attributes]

  (let [mm (get-head hype)
        ; create the new edge
        edge (entity attributes)
        new-mm (-> mm
                   ; create a new mindmap based on the old, but with the new edge added
                   (add-mm-val :edges edge)
                   ; and with a new entry in the adjacency representation
                   (add-adjacency origin dest edge))]
    (add-mindmap hype new-mm)))

(defn apply-filters
  "Apply multiple filters, returning a list of elements which satisfy them all."
  ; Could probably be made more efficient by shortcutting with a :while, but that
  ;   would require inverting the loop order (ie loop over collection outside filters
  [filter-list coll]
  (if-not filter-list
    ; if filter-list is empty, we're done
    coll
    ; otherwise apply this filter and recurse on the rest
    (let [[cur-filter & remaining-filters] filter-list
          filtered-coll (filter cur-filter coll)]
      (set (apply-filters remaining-filters filtered-coll)))))

(defn- get-adjacency
  [hype]
  (:adjacency (get-head hype)))

;TODO YOUAREHERE Just got this working properly. Could almost certainly be improved
; by some reorganization and simplification. "Older version," below, is dead code.
; Open question: doc says "return all edges" but in fact it returns all edge ids.
; Do we care? If we express in terms of edges, more interesting filters become natural.
(defn edges-from
  "Return all edges originating at this node, optionally applying a filterchain."
  ; Simple version with no filter
  ([hype node]
    (apply 
      union ; they come out as a list of sets which must be joined
      (let [adjacency (get-adjacency hype)]
        (for [[origin dest-struct] adjacency :when (= origin (:id node))
              [dest edges] dest-struct]
          edges))))

  ; With filter: wrap filtering around simple version
  ([hype node filter-chain]
   (apply-filters
     filter-chain
     (edges-from hype node))))

; Older version of the above
(defn edges-from
  "Return all edges originating at this node, optionally applying a filterchain."
  ([hype node]
    (edges-from hype node nil))
  ([hype node filter-chain]
    (let [adjacency (:adjacency (get-head hype))]
      ; Apply any filters (edges are multiple sets, so union them first)
      (apply-filters
        filter-chain 
        (apply union
          (for [[origin dest-struct] adjacency :when (= origin (:id node))
                [dest edges] dest-struct]
              edges))))))

(defn delete-node
  "Delete current node from the head mindmap, returning a modified hypermap.
  New current node is the (first) parent of the deleted node."
  [hype ]
  )
