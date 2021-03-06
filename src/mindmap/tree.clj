(ns mindmap.tree
  (:use [clojure.walk :only [postwalk]])
  (:require [mindmap.mm :refer :all]
            [mindmap.util :as ut]))

"Functions for returning tree views of mindmaps"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; The naive approach to building a tree would be, for each node, to search the
; mindmap for edges leading from that node. That's O(N*E), though. In the
; interest of performance, we instead build a map from nodes to lists of edges,
; so that we can retrieve all edges for a node in O(1) time. That way, building
; the tree is O(N+E) instead of O(N*E), at the cost of some additional code
; complexity.
; To that end, we create the following helper functions:

(defn- compare-edges
  "Compare edges by timestamp. If timestamps are equal (frequently the case
  in test, probably not IRL) sort by ids on the theory that lower ids were
  added first."
  [n1 n2]
  (if (not= (:timestamp n1) (:timestamp n2))
    (compare (:timestamp n1) (:timestamp n2))
    (compare (:id n1) (:id n2))))

(defn- make-node-edge-map
  "Return a sorted map from node to (edges). Allows fast downward navigation.
  Runs in O(E) with E the number of edges."
  ; Note - don't try to apply walk to *this* - walk doesn't work on sorted-maps
  [mm]
  (let [edges (reverse (sort-by :id (:edges mm)))
        add-to-map (fn [cur-map edge]
                     (update-in cur-map [(:origin-id edge)] conj edge))
        empty-sorted-map (sorted-map-by compare-edges)
        ]
    ;(reduce add-to-map empty-sorted-map edges)
    (reduce add-to-map (sorted-map) edges)

    ))

; (ut/demo (def rmm (rand-mm :num-nodes 12 :seed -1)))
; (ut/ppprint (make-node-edge-map rmm))

(defn- edges-from-nem [node-edge-map node]
  (get node-edge-map (:id node)))

(defn- nodes-from-nem [mm node-edge-map node]
   (let [node-from-edge (fn [edge] (get (:nodes mm) (:dest-id edge)))]
      (map node-from-edge (edges-from-nem node-edge-map node))))

;TODO delete if unused
(defn- node-list-comparator
  "Sort with node at the front, and subtrees after it in id order."
  ; note - each of either a and b is a node (at most one of them) or a list
  ;TODO consistent but maybe not quite what I want.
  [a b]
  (if (seq? a)
    1
    (compare (:id a) (:id b))))


; End helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn to-tree
  "Return the tree whose root is 'node', to a depth of 'depth'. Doesn't find disjoint trees."
  ; Natural candidate for memoization. Pluggable strategies in
  ; https://github.com/clojure/core.memoize/

  ; Simplest case: find the root from cur
  ;TODO this case added recently -- look for chances to refactor elsewhere to use it
  ([mm]
   (to-tree mm (get-cur-root mm)))

  ; Default depth assumption
  ([mm node]
   (to-tree mm node 1000))

  ; Outer call
  ([mm node depth]
   ;TODO - ultimately after doing all this, we should scan for unhandled nodes
   ;  in case of disjoint trees. Maybe.
   (let [node-edge-map (make-node-edge-map mm)]
       (to-tree mm node depth node-edge-map)))

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

; DELETE after, say, 4/1.
; (defn- walk-str-fn
;   "Given an Entity (assumed to be a node), return a function of it.
;   Given anything else, return that thing. Handler for display-fn."
;   [f thing]
;   (if (= (type thing) mindmap.mm.Entity)
;     (f thing)
;     thing))

(defn- apply-to
  "Apply f to the thing, if it's the right sort of thing. Otherwise just
  return the thing. Handler for display-fn."
  [f the-type thing]
  (if (= (type thing) the-type)
    (f thing)
    thing))

; DELETE after, say, 4/1.
; (defn display-fn
;   "Return a function which can be applied to the members of a tree
;   during a walk. Pass in a function which can be applied to a node."
;   [f]
;   (partial walk-str-fn f))

(defn tree-map
  "Return a representation of this tree by walking it and applying the supplied
  function to each contained node. As long as the supplied function returns the
  same type it received, these can be chained."
  ; Note - could easily be generalized to a collection-map by taking an
  ; item-type parameter.
  [tr f]
  (let [item-type mindmap.mm.Entity
        wrapped-fn (partial apply-to f item-type)] ; This fn was made for walking.
    (postwalk wrapped-fn tr)))

(defn tree-ids
  "Return a simple tree matching tr but containing only ids. Useful
  basis for other representation functions and mappings of trees."
  [tr]
  (tree-map tr #(:id %)))

(defn tree-titles
  "Return a tree containing the titles (and ids) of the nodes passed
  in."
  [tr]
  (tree-map tr  #(str (:title %) " (" (:id %) ")")))

(defn tree-ez-timestamp
  "DEPRECATED - the epoch functionality in util is now the better approach.
  Leaving this here for a while as an example of how to use tree-map.
  Display timestamp as starting from 0"
  [tr]
  (let [epoch (:timestamp (first tr))]
    (tree-map tr (fn [nd]
                   (update-in nd [:timestamp] #(- % epoch))
                   ;(update-in nd [:timestamp] inc)
                   ))))

;(def mrmm (rand-mm :num-nodes 8 :seed 3))

; Example tree
(defn ex-tree []
  (let [_ (ut/reset-indexer)
        _ (ut/reset-epoch)
        rmm (rand-mm :num-nodes 8 :seed 1)
        root (get-cur-root rmm)]
    (to-tree rmm root)))

;(ex-tree)
