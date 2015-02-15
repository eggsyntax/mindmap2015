(ns mindmap.test-serialize
  (:use [mindmap.serialize]
        [mindmap.mm-pub]
        [clojure.test])
  (:require [mindmap.mm :as mm]
            [mindmap.util :as util]))

(deftest basic-math
  (is (= 4 4)))

(def expected_edn_output
  "{:id 195,\n :maps\n {205\n  {:adjacency {196 {199 #{204}}, 193 {196 #{202}}},\n   :id 205,\n   :nodes\n   {199 {:title \"Third node\", :id 199},\n    196 {:title \"Second node\", :id 196},\n    193 {:title \"New mindmap\", :id 193}},\n   :edges\n   {204 {:title \"Edge 2\", :type :child, :id 204},\n    202 {:title \"Edge 1\", :type :child, :id 202}},\n   :cur-pointer 199},\n  203\n  {:adjacency {193 {196 #{202}}},\n   :id 203,\n   :nodes\n   {199 {:title \"Third node\", :id 199},\n    196 {:title \"Second node\", :id 196},\n    193 {:title \"New mindmap\", :id 193}},\n   :edges {202 {:title \"Edge 1\", :type :child, :id 202}},\n   :cur-pointer 199},\n  201\n  {:id 201,\n   :nodes\n   {199 {:title \"Third node\", :id 199},\n    196 {:title \"Second node\", :id 196},\n    193 {:title \"New mindmap\", :id 193}},\n   :edges {},\n   :cur-pointer 199},\n  198\n  {:id 198,\n   :nodes\n   {196 {:title \"Second node\", :id 196},\n    193 {:title \"New mindmap\", :id 193}},\n   :edges {},\n   :cur-pointer 196},\n  194\n  {:id 194,\n   :nodes {193 {:title \"New mindmap\", :id 193}},\n   :edges {},\n   :cur-pointer 193}},\n :map-edges\n {[203 205] {:type :child},\n  [201 203] {:type :child},\n  [198 201] {:type :child},\n  [194 198] {:type :child}},\n :head-pointer 205}\n")

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
  (let [edn_hype (serialize-to-edn @hypermap)]
    (spit "/tmp/edn_hype" (util/to-str edn_hype)) 
    (is (= edn_hype expected_edn_output))
    )
  
  )

(run-tests 'mindmap.test-serialize)
