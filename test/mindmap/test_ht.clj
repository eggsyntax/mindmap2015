(ns mindmap.test-ht
  (:use [mindmap.ht])
  (:require [clojure.test :refer :all]
            [clojure.stacktrace :as trace]
            [mindmap.util :as ut]
            [mindmap.mm :as mm]))

(deftest test-default-hypertree
  (let [hyper (default-hypertree)
        mmap (get-head hyper) ]
    (is (= 1 (count (:nodes mmap))))
    (is (= 0 (count (:edges mmap))))))

(deftest test-make-alter-hypertree
  (let [rand-ht (rand-hypertree 8 3 0)
        node-attrs {:title "ENode1" :flava :vanilla}
        edge-attrs {:title "Edge to new cur" :type :child}
        alter-ht (make-alter-hypertree rand-ht {:type :child :title "Made new node"})
        new-ht (alter-ht mm/add-new-node-from (get-cur rand-ht) node-attrs edge-attrs)
        new-mm (get-head new-ht)
        new-node (get-cur new-ht)]
    ; cur of the new ht is the one we wanted to add?
    (is (= (:title (get-cur new-ht)) "ENode1"))
    ; old cur is parent of new cur?
    (is (= (mm/parent-of new-mm new-node) (get-cur rand-ht)))))

(try
  (let [rht (rand-hypertree 8 3 0)
;         old-head (get-head rht)
        old-cur (get-cur rht)
        _ (println "old cur: " (ut/to-str old-cur))
        new-title "Altered node"
        nht (alter-node-ht rht nil {:title new-title})
;         _ (ut/ppprint nmm)
        _ (println "OK that's it")
        new-cur (get-cur nht)
        ]
  (println "New-cur"new-cur)
  )
  (catch Exception e (str "Exception local: " (trace/print-stack-trace e)))
  )


(deftest test-get-cur
  (let [hyper (default-hypertree)]
    (is (= (:title (get-cur hyper)) "Node 1"))))

(deftest test-add-node-no-edge
  (let [hyper (atom (default-hypertree))
        _ (swap! hyper add-node {:title "Node 2"} {:title "Tree 1"})
        mmap (get-head @hyper) ]
    (is (= 2 (count (:nodes mmap))))))

(deftest test-add-edge
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-node {:title "Node 2"} {:title "Tree 1"})
        n2 (get-cur @hyper)
        _ (swap! hyper add-edge n1 n2
                 {:title "Edge 1" :type :child}
                 {:title "Tree 1"})]
      (is (= 1 (count (edges-between @hyper n1 n2))))
      (is (= "Edge 1" (:title (first (edges-between @hyper n1 n2)))))
    )
  )

(deftest test-add-new-node-from
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        n2 (get-cur @hyper)
        edge (first (edges-between @hyper n1 n2)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Edge 1" (:title edge))) )
)

(deftest test-add-2-leaf-tree
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        n2 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 n1
                 {:title "Node 3"}
                 {:title "Edge 2"}
                 {:title "Tree 2"})
        n3 (get-cur @hyper)
        e1 (first (edges-between @hyper n1 n2))
        e2 (first (edges-between @hyper n1 n3)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Node 3" (:title n3)))
    (is (= "Edge 1" (:title e1)))
    (is (= "Edge 2" (:title e2)))
    ))

(deftest test-child-nodes
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        _ (swap! hyper add-new-node-from
                 n1
                 {:title "Node 3"}
                 {:title "Edge 2"}
                 {:title "Tree 2"})]
      (is (= 2 (count (child-nodes @hyper n1))))))

(deftest test-node-and-children
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        _ (swap! hyper add-new-node-from
                 n1
                 {:title "Node 3"}
                 {:title "Edge 2"}
                 {:title "Tree 2"})
        ]
      (is (= 3 (count (node-and-children @hyper n1))))))

(deftest test-remove-edge
  (let [hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        n2 (get-cur @hyper)
        edge (first (edges-between @hyper n1 n2))]
      (swap! hyper remove-edge edge {:comment "Removed Edge"})
      (is (= 0 (count (:edges (get-head @hyper)))))
    )
  )

(deftest test-remove-node
 (let [ hyper (atom (default-hypertree))
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"} {:title "Tree 1"})
        n2 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 3"} {:title "Edge 2"} {:title "Tree 2"})
        ]
    (swap! hyper remove-node n2 {:comment "Removed Node"})
    (is (= 2 (count (:nodes (get-head @hyper)))))
    (is (= 0 (count (:edges (get-head @hyper))))))
  )

(deftest test-remove-node-and-children
  (let [; Make a 3-leaf node
        hyper (atom (default-hypertree))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 (get-cur @hyper)
                 {:title "Node 2"}
                 {:title "Edge 1"}
                 {:title "Tree 1"})
        _ (swap! hyper add-new-node-from
                 n1
                 {:title "Node 3"}
                 {:title "Edge 2"}
                 {:title "Tree 2"})

        ; Attach 2 nodes to the rh leaf
        n3 (get-cur @hyper)
        _ (swap! hyper add-new-node-from
                 n3
                 {:title "Node 5"}
                 {:title "Edge 3"}
                 {:title "Tree 3"})
        _ (swap! hyper add-new-node-from
                 n3
                 {:title "Node 6"}
                 {:title "Edge 4"}
                 {:title "Tree 4"})
        ]
    (swap! hyper remove-node-and-children n3 {:comment "Removed Node and children"})
    (is (= 2 (count (:nodes (get-head @hyper) ))))
    (is (= 1 (count (:edges (get-head @hyper) ))))
    )
  )

(run-tests 'mindmap.test-ht)

