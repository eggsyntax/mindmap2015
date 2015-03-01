(ns mindmap.as-tree
  (:use [mindmap.mm]
        [mindmap.util :as ut]))

"Functions for returning tree views of mindmaps"

(defn parent-of
  "Returns the single node which is the parent of this node (ie via
  a link of type :child)."
  [mm node]
  (let [possible-parents (edges-to mm node)
        is-parental #(= (:type %) :child)
        parent-edges (filter is-parental possible-parents)
        parents (map #(get (:nodes mm) (:origin-id %)) parent-edges)]
    (assert (> (count parents) 0)
            (str "This node has no parent. " node))
    (assert (< (count parents) 2)
            (str "This freaky node has too many parents. " node ":\n\t" parents))
    (first parents)))

(defn make-node-edge-map
  "Return a map from node to (edges). Allows fast downward navigation. Runs
  in O(E) with E the number of edges."
  [mm]
  (let [edges (:edges mm)
        add-to-map (fn [cur-map edge]
                     (update-in cur-map [(:origin-id edge)] conj edge))]
    (reduce add-to-map {} edges)))

(ut/demo (def rmm (rand-mm :num-nodes 8 :seed -1)))
(ut/ppprint (make-node-edge-map rmm))

(defn subtree
  "Return the subtree whose root is 'node', to a depth of 'depth'. Pass depth
  of nil to return entire tree. Throws error if resulting subtree would contain
  circularity."
  ; Use set of visited nodes to detect circularity
  [mm node depth]
  (let [edges (:edges mm)
        init-tree '(node)
        node-edge-map (make-node-edge-map mm)
        ]
    (loop [tree init-tree
           levels depth
           visited #{}
           cur node
           ]
      ;
      )))
