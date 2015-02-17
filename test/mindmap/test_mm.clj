(ns mindmap.test-mm
  (:use [clojure.test]
        [mindmap.mm])
  (:require [clojure.test :refer :all]
            [clojure.tools.namespace.repl :only  (refresh)]
            [mindmap.mm :as mm]))

(deftest basic-math
  (is (= 4 4)))

(deftest test-default-mindmap
  (let [mmap (default-mindmap)]
    (is (= 1 (count (:nodes mmap))))
    (is (= 0 (count (:edges mmap))))
    (is (= 0 (count (:adjacency mmap))))))

(deftest test-create-entity
  (let [title {:title "TEST"}
        ent (create-entity title)]
    (is (:title ent) "TEST")))

(deftest test-get-cur 
  (let [mmap (default-mindmap)]
    (is (= (:title (get-cur mmap) "Node 1")))))

(deftest test-add-node-no-edge
  (def mmap (atom (default-mindmap)))
  (swap! mmap add-node {:title "Node 2"})
  (is (= 2 (count (:nodes @mmap)))))

(deftest test-add-new-node-from
  (def mmap (atom (default-mindmap)))
  (def n1 (get-cur @mmap))
  (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
  (def n2 (get-cur @mmap))
  (is (= "Node 2" (:title n2)))
  (def edge (first (edges-between @mmap n1 n2)))
  (is (= "Edge 1" (:title edge))))

(deftest test-add-2-leaf-tree
  (def mmap (atom (default-mindmap)))
  (def n1 (get-cur @mmap))
  (swap! mmap add-new-node-from (get-cur @mmap) {:title "Node 2"} {:title "Edge 1"})
  (def n2 (get-cur @mmap))
  (swap! mmap add-new-node-from n1 {:title "Node 3"} {:title "Edge 2"})
  (def n3 (get-cur @mmap))
  (is (= "Node 2" (:title n2)))
  (def e1 (first (edges-between @mmap n1 n2)))
  (is (= "Edge 1" (:title e1)))
  (def e2 (first (edges-between @mmap n1 n3)))
  (is (= "Edge 2" (:title e2)))
  )

(run-tests 'mindmap.test-mm)

