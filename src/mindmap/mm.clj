(ns mindmap.mm
  (:require [mindmap.util :as ut]
            [clojure.set :refer [union]]))

(defrecord Entity [id])
; Call create-edge rather than constructing directly
(defrecord Edge [origin-id dest-id id])

; Schema this:      val map   set       val
; NOTE! Make sure edges are a set -- easy to screw up :P
(defrecord Mindmap [nodes edges cur-pointer])

(defn- merge-with-timestamp-and-id
  "Merge properties into item, also adding timestamp and id"
  [item properties]
  (ut/with-id
    (merge item {:timestamp (ut/timestamp)} properties)))

(defn create-entity
  "Create a new system Entity, passing in any properties you want it
  to have.  It will be given a unique id automatically. Any other
  properties you want it to have should be passed in as a map.
  eg (def mynode (entity))
  or (def mynode (entity {:title \"foo\"}).  "
  [properties]
  (merge-with-timestamp-and-id (Entity. nil) properties))

(defn create-edge
  "Create an Edge, passing in origin node, dest node, and any other properties
  you want it to have. It will be given its own id automatically."
  [origin dest properties]
  (merge-with-timestamp-and-id (Edge. (:id origin) (:id dest) nil) properties))

(defn default-node
  []
  (create-entity {:title "Node 1"}))

(defn default-mindmap
  "Default mindmap contains one node, no edges."
  []
  (let [first-node (default-node)
        first-id (:id first-node)]
     (Mindmap. {first-id first-node} #{} (:id first-node))))

(defn get-node
  "Extract a node Entity by id"
  [mm id]
  ((:nodes mm) id))

(defn get-cur
  "Return the current node of the Mindmap"
  [mm]
  (get-node mm (:cur-pointer mm)))

(defn get-edge
  "Extract an edge Entity by id"
  [mm id]
  (first (filter #(= (:id %) id) (:edges mm))))

(defn get-edges
  "Get some edges from the head of a hypermap by id"
  [mm ids]
  (for [id ids]
    (get-edge mm id)))

(defn edges-between
  [mm origin dest]
  (filter
    #(and
       (= (:origin-id %) (:id origin))
       (= (:dest-id %) (:id dest)))
    (:edges mm)))

; NOTE This needs to remain public for the unit test to be able to see it
(defn filter-edges-by
  "Returns a seq of edges whose specified key-property
  (:origin-id, :dest-id, id) matches the id of the entity"
  [mm key-property entity]
  (filter #(= (key-property %) (:id entity)) (:edges mm)))

(defn edges-to
  "Returns a seq of all edges terminating at this node"
    [mm node]
    (let [parent-rels (filter-edges-by mm :dest-id node)]
      (map #(get-edge mm (:id %)) parent-rels)))

(defn edges-from
  "Returns a seq of all edges originating from this node"
    [mm node]
    (let [child-rels (filter-edges-by mm :origin-id node)]
      (map #(get-edge mm (:id %)) child-rels)))

(defn child-nodes
  "Returns a seq of all nodes to which this node has edges "
  [mm node]
  (let [child-rels (edges-from mm node)]
    (map #(get-node mm (:dest-id %)) child-rels)))

(defn parent-nodes
  "Returns a seq of all nodes which have edges to this node"
  [mm node]
  (let [par-rels (edges-to mm node)]
    (map #(get-node mm (:origin-id %)) par-rels)))

(defn set-cur
  "Get the current node Entity of the mindmap"
  [mm node]
  (assoc mm :cur-pointer (:id node)))

(defn update-entity
  "Update some entity type of a mindmap (nodes or edges) by adding a
  new value, returning an updated mindmap with a new id. "
  [mm entity-type entity]
  (let [id (:id entity)]
    ; nil args are probably a bad idea here -- although maybe it's fine
    ; to be starting from a nil mindmap.
    (assert (ut/no-nils? [mm entity-type entity])
            "Don't pass nil args to update-entity.")
    ; And the mindmap ought to have this property
    (assert (entity-type mm)
            (str "This mindmap doesn't have the entity type "
                 entity-type))
    ; add the new value in the appropriate place
     (assoc-in mm [entity-type id] entity)))

(defn add-node
  "Adds a node with the given attributes to the head mindmap of this
  hypermap, and set it as the current node. Does not create any edges
  in the mindmap.  Return the modified hypermap."
  [mm node-attrs]
  (let [node (create-entity node-attrs)]
    (-> mm
        (update-entity :nodes node)
        (assoc :cur-pointer (:id node)))))

(defn add-edge
  "Add an edge to a mindmap between two nodes.
  Its unidirectional connecting two nodes through an edge.
  Return modified mindmap."
  [mm origin dest attributes]
  (let [edge (create-edge origin dest attributes)
        updated-edges (conj (:edges mm) edge)]
    (assoc mm :edges updated-edges)))

(defn add-new-node-from
  "Add a new node as the child of the parent node making the child the current node. Return modified mindmap."
  [mm parent node-attrs edge-attrs]
  (let [new-map (add-node mm node-attrs)
        new-node (get-cur new-map)]
    (add-edge new-map parent new-node edge-attrs)))

(defn remove-edge
  "Remove this edge from the mindmap. Return new mindmap."
  [mm edge]
  (if (empty? edge)
    mm
    (let [new-adj-set (set (remove #(= (:id edge) (:id %)) (:edges mm)))]
        (-> mm
          (assoc :edges new-adj-set)))))

(defn remove-child-edges
  "Remove all child edges from this node.
  If there are no child edges it returns the mindmap, otherwise it returns a
  new mindmap without updating the id. "
  [mm node]
  (let [children (edges-from mm node) ]
    (reduce remove-edge mm children)))

(defn remove-parent-edges
  "Remove all parent edges from this node.
  If there are no parent edges it returns the mindmap, otherwise it returns a new mindmap without updating the id. "
  [mm node]
  (let [parent-edges (edges-to mm node) ]
      (reduce remove-edge mm parent-edges)))

(defn node-and-children
  ;TODO could probably be made more efficient by taking advantage of the functionality
  ; in tree.clj (at minimum by using make-node-edge-map, which would make just as much
  ; sense living in this ns if we didn't want to make this ns aware of the tree ns)
  "Does a DFS and returns a seq that is the node and all its children"
  [mm node]
  ; Use a stack to do a DFS travesal of all the nodes to remove
  (loop [nodes []
         explored #{}
         frontier [node]]
      (if (empty? frontier)
        nodes
        (let [n (peek frontier)
              children (child-nodes mm n)]
          (recur
            (conj nodes n)
            (into explored children)
            (into (pop frontier) (remove explored children)))))))

(defn remove-node
  [mm node]
  (let [new-nodes (into {} (remove #(= (:id node) (key %)) (:nodes mm))) ]
    (-> mm
      (remove-child-edges node)
      (remove-parent-edges node)
      (assoc :nodes new-nodes))))

(defn remove-node-and-children
  [mm node]
  (let [children (node-and-children mm node) ]
    (reduce remove-node mm children)))

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

(defn parent-if-exists
  "Try returning parent, but if parent-of throws an exception, just return nil.
  Useful for recursive searches which will eventually hit the root node."
  [mm node]
  (try
    (parent-of mm node)
    (catch AssertionError e
      nil)))

(defn prune
  "Remove any attributes on a mappish thing whose value is set to :remove-attr"
  [mappish]
  (let [prunable? #(= (val %) :remove-attr)
        keys-to-prune (for [[k v] (seq mappish)]
                        (when (= v :remove-attr) k))]
    (apply dissoc mappish keys-to-prune)))

(defn rand-mm
  "Convenience function to generate a random mindmap.

  Optional arguments:
    :num-nodes        - Size of the mindmap (default 4)
    :seed             - for the RNG (default 255, or -1 to randomize)
    :num-extra-links  - number of non-child links to add"
  [& {:keys [num-nodes seed num-extra-links]
      :or {num-nodes 4, seed -1, num-extra-links 0}}]
  (let [new-map (default-mindmap)
        new-node-attrs (fn [v] {:title (str "Node " (+ 2 v))})
        new-edge-attrs (fn [v] {:title (str "Edge " (+ 1 v)) :type :child})
        rng (ut/seeded-rng seed)
        some-node (fn [mm]
                    (if (empty? (:nodes mm))
                      nil
                      (nth (vals (:nodes mm))
                           (.nextInt rng (count (:nodes mm))))))
        add-a-node (fn [mm i]
                     (add-new-node-from
                       mm
                       (some-node mm)
                       (new-node-attrs i)
                       (new-edge-attrs i)))
        add-extra-edge (fn [mm i]
                         (add-edge
                           mm
                           (some-node mm)
                           (some-node mm)
                           {:title (str "Extra Edge " (+ 1 i)) :type :extra}))
        main-map (reduce add-a-node new-map (range (- num-nodes 1)))
        ]
    ; Add extra edges if requested
    ;TODO uncomment after finding indeterminacy
    main-map
    ;(reduce add-extra-edge main-map (range num-extra-links))

    ))

(defn alter-node
  "Change the attributes of a node. New values are added, values for existing
  keys are changed, and any values set to :remove-attr will be removed. id is
  unchanged. Cur is unchanged, on the assumption that the typical use-case is
  to call on cur (arity 2), and if you're specifying the node you'll want to
  decide whether to make the altered node cur or not. Timestamp is unchanged.
  Return the modified mindmap."
  ;TODO do we change timestamp?
    ; another option would be to keep a list of alteration-timestamps.
  ;TODO consider saving changeset? Derivable at ht level, so maybe no need.
  ;TODO might make some assertions here to ensure that (eg) id isn't changed
  ([mm new-content]
   (alter-node mm (get-cur mm) new-content))
  ([mm node new-content]
   (let [_ (println "Node at beginning of alter-node: " node)
         _ (println "New-content at beginning of alter-node: " new-content)
         altered-node (merge node new-content)
         _ (println "Altered node:" altered-node)
;          _ (println "type of :nodes is " (type (:nodes mm)))
;          _ (println ":id node is " (:id node))
;          _ (println ":nodes is " (ut/to-str (:nodes mm)))
         altered-mm   (assoc-in mm [:nodes (:id node)] altered-node)
         _ (println "Has been altered")
         pruned-mm    (prune altered-mm)
;          _ (println "Pruned!" (ut/to-str altered-mm))
         _ (println "New cur: " (get-cur pruned-mm))
         ]
     pruned-mm)))

(let [rmm (rand-mm)
      new-content {:title "foo"}
      ]
  (alter-node rmm new-content)
  )

(defn get-root
  "Get root of mindmap relative to some node"
  [mm node]
  (loop [cur node]
    (let [cur-parent (parent-if-exists mm cur)]
      (if cur-parent
        (recur cur-parent)
        cur))))

(defn get-cur-root
  "Get root relative to current node"
  [mm]
  (get-root mm (get-cur mm)))

;(ut/ppprint (rand-mm :num-nodes 3 :num-extra-links 1))
;(def r1 (rand-mm :num-nodes 6 :num-extra-links 1))
;(nth (vals (:nodes r1)) 3)
