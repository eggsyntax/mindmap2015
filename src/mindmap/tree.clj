(ns mindmap.tree
  (:use [mindmap.mm]
        [mindmap.util :as ut]
        [clojure.walk :as walk]))

"Functions for returning tree views of mindmaps"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; The naive approach to building a tree would be, for each node, to search the
; mindmap for edges leading from that node. That's O(N*E), though. In the
; interest of performance, we instead build a map from nodes to lists of edges,
; so that we can retrieve all edges for a node in O(1) time. That way, building
; the tree is O(N+E) instead of O(N*E), at the cost of some additional code
; complexity.
; To that end, we create the following helper functions:

(defn- make-node-edge-map
  "Return a map from node to (edges). Allows fast downward navigation. Runs
  in O(E) with E the number of edges."
  [mm]
  (let [edges (:edges mm)
        add-to-map (fn [cur-map edge]
                     (update-in cur-map [(:origin-id edge)] conj edge))]
    (reduce add-to-map {} edges)))

; (ut/demo (def rmm (rand-mm :num-nodes 12 :seed -1)))
; (ut/ppprint (make-node-edge-map rmm))

(defn- edges-from-nem [node-edge-map node]
  (get node-edge-map (:id node)))

(defn- nodes-from-nem [mm node-edge-map node]
   (let [node-from-edge (fn [edge] (get (:nodes mm) (:dest-id edge)))]
      (map node-from-edge (edges-from-nem node-edge-map node))))

; End helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- node-list-comparator
  "Sort with node at the front, and subtrees after it in id order."
  ; note - each of either a and b is a node (at most one of them) or a list
  ;TODO consistent but maybe not quite what I want.
  [a b]
  (if (seq? a)
    1
    (compare (:id a) (:id b))))

; Consider making sorting optional - it makes printing consistent
; but at a small performance penalty, and it's unnecessary for some porpoises.
(defn to-tree
  "Return the tree whose root is 'node', to a depth of 'depth'. Doesn't find disjoint trees."
  ; Default depth assumption
  ([mm node]
   (to-tree mm node 1000))

  ; Outer call
  ([mm node depth]
   ;TODO - ultimately after doing all this, we should scan for unhandled nodes
   ;  in case of disjoint trees. Maybe.
   (let [node-edge-map (make-node-edge-map mm)]
     (sort
       node-list-comparator
       (to-tree mm node depth node-edge-map))))

  ; Recursive call
  ([mm node depth node-edge-map]
   (cons node
         (if (> depth 0)
           (for [cur (nodes-from-nem mm node-edge-map node)]
             (if (empty? (edges-from-nem node-edge-map cur))
               (list cur)
               (let [retval (to-tree mm cur (- depth 1) node-edge-map)]
                 retval)))
           nil))))

(defn- walk-str-fn
  "Given an Entity (assumed to be a node), return a function of it.
  Given anything else, return that thing."
  [f thing]
  (if (= (type thing) mindmap.mm.Entity)
    (f thing)
    thing))

(defn display-fn [f] (partial walk-str-fn f))

(defn tree-ids
  "Return a simple tree matching tr but containing only ids. Useful
  basis for other representation functions and mappings of trees."
  [tr]
  (let [id-fn (display-fn #(:id %))]
    (postwalk id-fn tr)))

; Example tree
(def ex-tree
  (let [_ (ut/reset-indexer)
        rmm (rand-mm :num-nodes 8 :seed 3)
        root (get-root rmm (get-cur rmm))
        my-tree (to-tree rmm root)
        ;_ (ppprint my-tree)
        ]
    my-tree))

(ut/ppprint ex-tree)
(tree-ids ex-tree)
