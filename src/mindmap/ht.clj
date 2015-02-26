(ns mindmap.ht
  (:require [mindmap.util :as ut]
            [mindmap.mm :as mm]
            [clojure.zip :as zip]
            )
  (:gen-class))

;TODO
"
 o Validation (Prismatic Schema, validators?)
 o unit tests
 o test for idempotency on (at least) most of these
"

; Tree structure
;
; [ N1 [ [N2] [N3 [N4 N5] ]
;
;           N1
;       N2    N3
;           N4  N5
;
(defrecord Node [id mm attrs])
(defrecord Hypertree [nodes head-pointer])

(defn default-hypertree
  []
  (let [first-mindmap (mm/default-mindmap)
        node-no-id (Node. nil first-mindmap {})
        first-node (ut/with-id node-no-id)
        tree-coll [first-node]
        ht (Hypertree.
             (zip/vector-zip tree-coll)
             (:id first-node)) ]
      ; Move the zipper to point to the first node
      (assoc ht :nodes (zip/down (:nodes ht)))
      ;(swap! (:nodes ht) zip/down)
    ))

(defn get-head
  "Get the mindmap which is the current head of the hypertree"
  [hyper]
  ; The head of the zipper always points to the head mindmap
  ; and is immediately accessible via nodes.
  (let [root (zip/node (:nodes hyper))]
    (:mm root)))

(defn get-tree-data
  "Returns the Hypertree data as a nested vector representing the tree of mindmaps."
  [hyper]
  (zip/root (:nodes hyper)))

(defn get-cur
  "Return the current node of the current head of the hypertree."
  [hyper]
  (let [head (get-head hyper)]
    (mm/get-cur head)) )

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

;TODO ganked this from a dead branch -- should work, but test it
; (defn parent-of
;   "Returns the single node which is the parent of this node."
;   [hyper node]
;   (let [possible-parents (edges-to hyper node)
;         is-parental #(= (:type %) :child)
;         parents (filter is-parental possible-parents)]
;     (assert (= (count parents) 1)
;             (str "This freaky node has too many parents. " node ":\n\t" parents))
;     parents))


; Note: This will need to become aware of the l/r nature
;       of the current branch when we adding branching
;
(defn- commit-mindmap
  "Commit a modified mindmap to this hypertree, and an edge from the previous head to
  the new mindmap. Make the new mindmap the head."
  [hyper mm attrs]
  (let [orig-head-id (:head-pointer hyper)
        node-no-id (Node. nil mm attrs)
        new-node (ut/with-id node-no-id)
        zipper (:nodes hyper) ]
      ; zip up, insert a new vector with the new node, traverse down to it
      ;
      (-> hyper
        (assoc
             :nodes
             (-> zipper
                  (zip/up)
                  (zip/insert-child [new-node])
                  (zip/down)
                  (zip/down)))
          (assoc :head-pointer (:id new-node)))))

(defn set-cur
  "Sets node to the current node of the head of the hypertree"
  [hyper node tree-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/set-cur mm node)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this hypertree,
  and set it as the current node. Does not create any edges in the mindmap.
  Return the modified hypertree"
  [hyper node-attrs tree-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/add-node mm node-attrs)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn add-new-node-from
  "Adds a child node to the given node with the given attributes to the head
  mindmap of this hypertree, and set it as the current node.  Return the
  modified hypertree"
  [hyper parent node-attrs edge-attrs tree-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/add-new-node-from mm parent node-attrs edge-attrs)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn add-edge
  "Add an edge to tee head mindmap of this hypertree Return the modified hypertree
  Parameters:
    Hypermap
    Origin node
    Destination node
    Map of attributes you would like the edge to have.
      id will be added automatically.
    Consider interning edges for performance.
      http://nyeggen.com/post/2012-04-09-clojure/"
  [hyper origin dest edge-attrs tree-attrs]
  (let [mm (get-head hyper)
        new-mm  (mm/add-edge mm origin dest edge-attrs)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn remove-node
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypertree"
  ; Question: if we remove the current head node what happens ?
  [hyper node tree-attrs]
  (let [mm (get-head hyper)
        new-mm
        (mm/remove-node mm node)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn remove-node-and-children
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypertree"
  ; Question: if we remove the current head node what happens ?
  [hyper node tree-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/remove-node-and-children mm node)]
    (commit-mindmap hyper new-mm tree-attrs)))

(defn remove-edge
  "Removes an edge from the head of the hypertree Return the modified hypertree"
  [hyper edge tree-attrs]
  (let [mm (get-head hyper)
        new-mm (mm/remove-edge mm edge)]
    (commit-mindmap hyper new-mm tree-attrs)))

