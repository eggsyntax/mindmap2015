(ns mindmap.as-tree
  (:use [mindmap.mm]
        [mindmap.util :as ut]

        ))

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

(defn subtree
  "Return the subtree whose root is 'node', to a depth of 'depth'"
  [mm node depth]

  )
