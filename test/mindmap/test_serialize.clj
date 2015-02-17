(ns mindmap.test-serialize
  (:use [mindmap.serialize]
        [mindmap.hm]
        [clojure.test])
  (:require [mindmap.mm :as mm]
            [mindmap.util :as util]))

(def expected_edn_output
  "{:head-pointer -1332609955,\n :map-edges\n {[2119426685 -1332609955] {:type :child},\n  [447350754 2119426685] {:type :child},\n  [958755248 447350754] {:type :child},\n  [1362154212 958755248] {:type :child}},\n :maps\n {-1332609955\n  {:adjacency\n   {754811195 {896630979 #{1647203936}},\n    -468285906 {754811195 #{704445981}}},\n   :id -1332609955,\n   :nodes\n   {1298679821 {:id 1298679821, :title \"Third node\"},\n    -1075204146 {:id -1075204146, :title \"Second node\"},\n    -468285906 {:id -468285906, :title \"New mindmap\"}},\n   :edges\n   {1647203936 {:id 1647203936, :type :child, :title \"Edge 2\"},\n    704445981 {:id 704445981, :type :child, :title \"Edge 1\"}},\n   :cur-pointer 1298679821},\n  2119426685\n  {:adjacency {-468285906 {754811195 #{704445981}}},\n   :id 2119426685,\n   :nodes\n   {1298679821 {:id 1298679821, :title \"Third node\"},\n    -1075204146 {:id -1075204146, :title \"Second node\"},\n    -468285906 {:id -468285906, :title \"New mindmap\"}},\n   :edges {704445981 {:id 704445981, :type :child, :title \"Edge 1\"}},\n   :cur-pointer 1298679821},\n  447350754\n  {:id 447350754,\n   :nodes\n   {1298679821 {:id 1298679821, :title \"Third node\"},\n    -1075204146 {:id -1075204146, :title \"Second node\"},\n    -468285906 {:id -468285906, :title \"New mindmap\"}},\n   :edges {},\n   :cur-pointer 1298679821},\n  958755248\n  {:id 958755248,\n   :nodes\n   {-1075204146 {:id -1075204146, :title \"Second node\"},\n    -468285906 {:id -468285906, :title \"New mindmap\"}},\n   :edges {},\n   :cur-pointer -1075204146},\n  1362154212\n  {:id 1362154212,\n   :nodes {-468285906 {:id -468285906, :title \"New mindmap\"}},\n   :edges {},\n   :cur-pointer -468285906}}}\n")

(defn setup []
  (def hypermap (atom (default-hypermap)))
  (def firstnode (get-cur @hypermap))
  (def anode (mm/entity {:title "Second node"}))
  (swap! hypermap add-node anode)
  (def anothernode (mm/entity {:title "Third node"}))
  (swap! hypermap add-node anothernode)
  (swap! hypermap add-edge firstnode anode {:title "Edge 1" :type :child})
  (swap! hypermap add-edge anode anothernode {:title "Edge 2" :type :child}))

(deftest test-serialize-to-edn
  (setup)
  ; Note that we pull out the :id and :timestmap, since they'll vary on every run.
  ; But also note that we test against the hashes, which we expect to be consistent.
  (let [edn_hype (serialize-to-edn (dissoc @hypermap :id :timestamp))]
    (is (= edn_hype expected_edn_output))))

(run-tests 'mindmap.test-serialize)
