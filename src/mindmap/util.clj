(ns mindmap.util
  (:use [clojure.pprint :only (pprint)])
  (:import [java.io StringWriter]))

(defn get-indexer
  "Helper function to create an indexer for a mm. Whenever the returned fn
  is called, it returns an incremented index."
  []
  (def add-and-get
    (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
      (fn [] (.addAndGet ai 1)))))

(def test-index (get-indexer))
(test-index)
(test-index)

(defn logged
  "Wrap any form in logged to have it printed to the console."
  [string stuff]
  (do
    (println string stuff)
    eval stuff))

(logged "test-log" '("foo" "bar")) ;test

(defn to-str
  "pretty-print object or lazy seq to string"
  [thing]
  (let [w (StringWriter.)]
    (pprint thing w)
    (.toString w)))

(to-str test-index)

(defn spaces [n]
  "Get some spaces for padding a string"
  (take (* n 2) (repeat \space)))

(spaces 5)
