(ns mindmap.test-mm
  (:use [clojure.test]
        [mindmap.mm])
  (:require [clojure.test :refer :all]
            [clojure.tools.namespace.repl :only  (refresh)]
            [mindmap.mm :as mm]
            [mindmap.util :as ut]))

(deftest test-default-mindmap
  (let [mmap (default-mindmap)]
    (is (= 1 (count (:nodes mmap))))
    (is (= 0 (count (:edges mmap))))))

(deftest test-create-entity
  (let [title {:title "TEST"}
        ent (create-entity title)]
    (is (:title ent) "TEST")))

(deftest test-get-cur
  (let [mmap (default-mindmap)]
    (is (= (:title (get-cur mmap) "Node 1")))))

(deftest test-add-node-no-edge
  (let [mmap (atom (default-mindmap)) ]
    (swap! mmap add-node {:title "Node 2"})
    (is (= 2 (count (:nodes @mmap))))))

(deftest test-add-new-node-from
  (let [mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @mmap)
        edge (first (edges-between @mmap n1 n2)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Edge 1" (:title edge))) )
)

(deftest test-add-2-leaf-tree
  (let [mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @mmap)
        _ (swap! mmap add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
        n3 (get-cur @mmap)
        e1 (first (edges-between @mmap n1 n2))
        e2 (first (edges-between @mmap n1 n3)) ]
    (is (= "Node 2" (:title n2)))
    (is (= "Node 3" (:title n3)))
    (is (= "Edge 1" (:title e1)))
    (is (= "Edge 2" (:title e2)))
    ))

(deftest test-child-nodes
  (let [mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! mmap add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})]
      (is (= 2 (count (child-nodes @mmap n1))))))

(deftest test-node-and-children
  (let [mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! mmap add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
        ]
      (is (= 3 (count (node-and-children @mmap n1))))))

(deftest test-remove-edge
  (let [mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @mmap)
        edge (first (edges-between @mmap n1 n2))]
      (swap! mmap remove-edge edge)
      (is (= 0 (count (:edges @mmap))))
    )
  )

(deftest test-remove-node
 (let [ mmap (atom (default-mindmap))
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        n2 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 3"} {:title "Edge 2"})
        ]
    (swap! mmap remove-node n2)
    (is (= 2 (count (:nodes @mmap))))
    (is (= 0 (count (:edges @mmap))))))

(deftest test-remove-node-and-children
  (let [; Make a 3-leaf node
        mmap (atom (default-mindmap))
        n1 (get-cur @mmap)
        _ (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
        _ (swap! mmap add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})

        ; Attach 2 nodes to the rh leaf
        n3 (get-cur @mmap)
        _ (swap! mmap add-new-node-from n3 {:title "Node 5"} {:title "Edge 3"})
        _ (swap! mmap add-new-node-from n3 {:title "Node 6"} {:title "Edge 4"})]
    (swap! mmap remove-node-and-children n3)
    (is (= 2 (count (:nodes @mmap))))
    (is (= 1 (count (:edges @mmap))))))


(run-tests 'mindmap.test-mm)

