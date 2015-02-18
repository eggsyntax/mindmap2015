(ns mindmap.test-hm
  (:use [mindmap.hm]
        [mindmap.util]
        )
  (:require [clojure.test :refer :all]
            [clojure.tools.namespace.repl :only  (refresh)]
            [mindmap.hm :refer :all]
            [mindmap.util :as ut]
            ))

(deftest test-default-hypermap
  (let [hyper (default-hypermap)
        mmap (get-head hyper) ]
    (is (= 1 (count (:nodes mmap))))
    (is (= 0 (count (:edges mmap))))
    (is (= 0 (count (:adjacency mmap))))))

(deftest test-get-cur 
  (let [hyper (default-hypermap)]
    (is (= (:title (get-cur hyper)) "Node 1"))))

(deftest test-add-node-no-edge
  (let [hyper (atom (default-hypermap))
        _ (swap! hyper add-node {:title "Node 2"})
        mmap (get-head @hyper) ]
    (is (= 2 (count (:nodes mmap))))))

(deftest test-add-new-node-from
  (let [hyper (atom (default-hypermap)) 
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @hyper)
        edge (first (edges-between @hyper n1 n2)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Edge 1" (:title edge))) )
)

(deftest test-add-2-leaf-tree
  (let [hyper (atom (default-hypermap)) 
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @hyper) 
        _ (swap! hyper add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
        n3 (get-cur @hyper)
        e1 (first (edges-between @hyper n1 n2)) 
        e2 (first (edges-between @hyper n1 n3)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Node 3" (:title n3)))
    (is (= "Edge 1" (:title e1)))
    (is (= "Edge 2" (:title e2)))
    ))

(deftest test-child-nodes
  (let [hyper (atom (default-hypermap))
        n1 (get-cur @hyper) 
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! hyper add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})]
      (is (= 2 (count (child-nodes @hyper n1))))))

(deftest test-node-and-children
  (let [hyper (atom (default-hypermap))
        n1 (get-cur @hyper) 
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! hyper add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
        ]
      (is (= 3 (count (node-and-children @hyper n1))))))

(deftest test-remove-edge
  (let [hyper (atom (default-hypermap)) 
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @hyper) 
        edge (first (edges-between @hyper n1 n2))]
      (swap! hyper remove-edge edge)
      (is (= 0 (count (:edges (get-head @hyper)))))
      (is (= 0 (count (:adjacency (get-head @hyper)))))
    )
  )

(deftest test-remove-node
 (let [ hyper (atom (default-hypermap))
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 3"} {:title "Edge 2"})
        ]
    (swap! hyper remove-node n2)
    (is (= 2 (count (:nodes (get-head @hyper)))))
    (is (= 0 (count (:edges (get-head @hyper)))))
    (is (= 0 (count (:adjacency (get-head @hyper))))))
  )

(deftest test-remove-node-and-children
  (let [; Make a 3-leaf node
        hyper (atom (default-hypermap))
        n1 (get-cur @hyper)
        _ (swap! hyper add-new-node-from (get-cur @hyper) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! hyper add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
  
        ; Attach 2 nodes to the rh leaf 
        n3 (get-cur @hyper)
        _ (swap! hyper add-new-node-from n3 {:title "Node 5"} {:title "Edge 3"})
        _ (swap! hyper add-new-node-from n3 {:title "Node 6"} {:title "Edge 4"})
        ]
    (swap! hyper remove-node-and-children n3) 
    (is (= 2 (count (:nodes (get-head @hyper) ))))
    (is (= 1 (count (:edges (get-head @hyper) ))))
    (is (= 1 (count (:adjacency (get-head @hyper) ))))
    )
  )

(run-tests 'mindmap.test-hm)

