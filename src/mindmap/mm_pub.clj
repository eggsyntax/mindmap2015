(ns mindmap.mm-pub
  (:require [mindmap.util :as ut]
            [mindmap.mm :as mm]
            [clojure.set :refer [union]])
  (:gen-class))

; Public API for mindmap

; See core-examples for usage
; TODO consider adding Prismatic's schema for idiomatic data description
; https://github.com/Prismatic/schema

"

 alter and dysync


Adding Validation to Refs
Database transactions maintain consistency through various integrity
checks. You can do something similar with Clojure’s transactional
memory, by specifying a validation function when you create a ref:
(ref initial-state options*)
; options include:
; :validator validate-fn

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

(defn get-cur
  "Return the current node of the current head of the hypermap."
  [hype]
  (let [head (get-head hype)]
    (mm/get-node head (:cur-pointer head)))
  )

(defn- commit-mindmap
  "Commit a modified mindmap to this hypermap, and an edge from the previous head to
  the new mindmap. Make the new mindmap the head."
  [hype mm]
    ; New hypermap had better include this mindmap!
    {:post [(contains? (:maps %) (:id mm))]}

    (let [orig-head-id (:head-pointer hype)
          new-id (:id mm)
          new-edge-key [orig-head-id new-id]
          new-edge-val {:type :child} ]

      (-> hype
        ; Add mindmap
        (assoc-in [:maps new-id] mm)
        ; Add edge
        (assoc-in [:map-edges new-edge-key] new-edge-val)
        ; Set head pointer
        (assoc :head-pointer new-id))))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this hypermap,
  and set it as the current node. Does not create any edges in the mindmap.
  Return the modified hypermap."
  ; Does adding a node make it cur? I think so, and am writing it as such,
  ; but we could certainly drop that.
  [hype attributes]
  (let [mm (get-head hype)
        node (mm/entity attributes)
        new-mm
          (-> mm
              (mm/update :nodes node)
              ; bug here. add ID
              (assoc :cur-pointer (:id node)))]
    (commit-mindmap hype new-mm)))

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
        new-mm (mm/add-edge mm origin dest attributes)]
    (commit-mindmap hype new-mm)))

(defn add-new-node-from
  "Add a new node as the child of the parent node making the child the current node."
  [hype parent child-attrs edge-attrs]
  (let [mm (get-head hype)
        child (mm/entity child-attrs)
        new-mm
          (-> mm
              (mm/update :nodes child)
              (mm/add-edge mm parent child edge-attrs)
              (assoc :cur-pointer child))]
    (commit-mindmap hype new-mm)))

; Does this makes sense to generally expose ?
(defn get-edges
  "Get some edges from the head of a hypermap by number"
  [hype edge-nums]
  (for [edge-num edge-nums]
    (get (:edges (get-head hype)) edge-num)))

; The mm interface should hide that connectivity info about edges
; that are held in the adjacency keyword.
;
(defn edges-from
  "Return all edges originating at this node"
  ([hype node]
    (apply union ; they come out as a list of sets which must be joined
      (let [adjacency (:adjacency hype)]
        ; for each origin, for each destination, return the related edge
        (for [[origin dest-struct] adjacency :when (= origin (:id node))
              [dest edges] dest-struct]
          (get-edges hype edges)
          )))))

; Create a hypermap for testing
(defn default-hypermap
  []
  (let [first-mindmap (mm/default-mindmap)
        first-id (:id first-mindmap)]
    {:id (ut/main-indexer)
     :maps {first-id first-mindmap}
     :map-edges {}
     :head-pointer first-id}))
