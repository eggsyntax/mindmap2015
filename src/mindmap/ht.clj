(ns mindmap.ht
  (:require [mindmap.util :as ut]
            [mindmap.mm :as mm]
            [mindmap.tree :as tr] ;TODO temp
            [clojure.zip :as zip]
            [clojure.stacktrace :as trace]
            )
  (:gen-class)) ;TODO Why the gen-class here?

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

(defn- hypertree-from-mindmap
  [first-mm]
  (let [node-no-id (Node. nil first-mm {})
        first-node (ut/with-id node-no-id)
        tree-coll [first-node]
        ht (Hypertree.
             (zip/vector-zip tree-coll)
             (:id first-node)) ]
      ; Move the zipper to point to the first node
      (assoc ht :nodes (zip/down (:nodes ht)))))

(defn default-hypertree []
  (hypertree-from-mindmap (mm/default-mindmap)))

(defn rand-hypertree
 "Convenience function to generate a random mindmap.
    num-nodes        - Size of the mindmap  (default 4)
    seed             - for the RNG (default 255, or -1 to randomize)
    num-extra-links  - number of non-child links to add"
  [num-nodes seed num-links]
  (let [first-mm (mm/rand-mm :num-nodes num-nodes :seed seed :num-extra-links num-links)]
    (hypertree-from-mindmap first-mm)))

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

; Note: This will need to become aware of the l/r nature
;       of the current branch when we adding branching
;
(defn- commit-mindmap
  "Commit a modified mindmap to this hypertree, and an edge from the previous head to
  the new mindmap. Make the new mindmap the head."
  [hyper mm attrs]
  (let [node-no-id (Node. nil mm attrs)
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

(defn make-alter-hypertree
  "Given a reference to the current hypertree, and a set of tree-level attributes
  on the new mindmap, return a function which will apply its args to create
  the altered hypertree."
  [hyper tree-attrs]
  (fn [mm-f & args]
  ; Given a function of mm -> mm, and the appropriate args, apply that fn to
  ; the head mm of the hypertree, and return a modified hypertree containing
  ; (as head) the modified mm.
    (let [mm (get-head hyper)
          new-mm (apply mm-f mm args)]
      (commit-mindmap hyper new-mm tree-attrs))))

(defn set-cur
  "Sets node to the current node of the head of the hypertree"
  [hyper node tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/set-cur node)))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this hypertree,
  and set it as the current node. Does not create any edges in the mindmap.
  Return the modified hypertree"
  [hyper node-attrs tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/add-node node-attrs))
  )

(defn add-new-node-from
  "Adds a child node to the given node with the given attributes to the head
  mindmap of this hypertree, and set it as the current node.  Return the
  modified hypertree"
  [hyper parent node-attrs edge-attrs tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/add-new-node-from parent node-attrs edge-attrs)))

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
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/add-edge origin dest edge-attrs)))

(defn remove-node
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypertree"
  ; Question: if we remove the current head node what happens ?
  [hyper node tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/remove-node node)))

(defn remove-node-and-children
  "Removes this node and any edges that originate from or terminate at this node from the head of the hyperrmap.
  Returns the modified hypertree"
  ; Question: if we remove the current head node what happens ?
  [hyper node tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/remove-node-and-children node)))

(defn alter-node
  "Change the attributes of a single node in the head of this hypertree (by
  default, the cur node). New values are added, values for existing keys are
  changed, and any values set to :remove-attr will be removed. id is unchanged.
  Cur is unchanged, on the assumption that the typical use-case is to call on
  cur (arity 3), and if you're specifying the node, you'll want to make your own
  decision about whether to change cur. Timestamp is unchanged. Return the modified
  hypertree."
  ; Operate on cur by default
  ([hyper tree-attrs new-content]
   (alter-node hyper tree-attrs (get-cur hyper) new-content))

  ([hyper tree-attrs node new-content]
   (let [alter-ht (make-alter-hypertree hyper tree-attrs)
         altered (alter-ht mm/alter-node node new-content)]
     altered)))

(defn remove-edge
  "Removes an edge from the head of the hypertree Return the modified hypertree"
  [hyper edge tree-attrs]
  (let [alter-ht (make-alter-hypertree hyper tree-attrs)]
    (alter-ht mm/remove-edge edge)))
