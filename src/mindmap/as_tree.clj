(ns mindmap.as-tree
  (:use [mindmap.mm]
        [mindmap.util :as ut]))

"Functions for returning tree views of mindmaps"

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

(defn- edges-from-map [node-edge-map node]
  (get node-edge-map (:id node)))

(defn- nodes-from-map [mm node-edge-map node]
   (let [node-from-edge (fn [edge] (get (:nodes mm) (:dest-id edge)))]
      (map node-from-edge (edges-from-map node-edge-map node))))

(defn as-tree
  "Return the tree whose root is 'node', to a depth of 'depth'. Pass depth
  of nil to return entire tree."
  ([mm node depth]
   (let [node-edge-map (make-node-edge-map mm)]
     (as-tree mm node depth node-edge-map)))

  ([mm node depth node-edge-map]
   (println "depth: " depth)
   (cons node
         (if (> depth 0)
           (for [cur (nodes-from-map mm node-edge-map node)]
             (if (empty? (edges-from-map node-edge-map cur))
               (list cur)
                ;TODO implement levels
               (let [retval (as-tree mm cur (- depth 1) node-edge-map)]
                 retval)))
           nil))))

(ut/demo (def rmm (rand-mm :num-nodes 300 :seed 3)))
(def n-e-m (make-node-edge-map rmm))
(ut/ppprint n-e-m)
(def root (last (vals (:nodes rmm))))
(ut/ppprint root)
(count (:nodes rmm))
(ut/ppprint (edges-from-map n-e-m root))
(ut/ppprint (nodes-from-map rmm n-e-m root))
(ut/ppprint root)
(get n-e-m 7239)
(def cur (get (:nodes rmm) 7239))
(ut/ppprint cur)
(ut/ppprint (as-tree rmm cur 10))

(let [mm (rand-mm)
      cur (get-cur mm)]
  (get-root mm cur)
  )


