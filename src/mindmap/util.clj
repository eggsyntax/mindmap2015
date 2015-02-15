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

(def main-indexer (get-indexer))
(main-indexer)
(main-indexer)

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

(to-str main-indexer)

(defn ppprint [thing]
  (println (to-str thing)))

(defn print-head [hype] (ppprint (hype :maps (hype :head-pointer))))

(defn spaces [n]
  "Get some spaces for padding a string"
  (take (* n 2) (repeat \space)))

(spaces 5)

(defmacro demo
  "demo macro just prints itself and its results to console" 
  [form]
  (println "***" form "***")
  (println)
  (ppprint (eval form))
)

(defn no-nils? [coll]
  (every? #(not (nil? %)) coll)
  )

(no-nils? [1 2 3])
(no-nils? [1 nil 3])

(defn seq-contains?
  "Tests if the target is in a Seq"
  [coll target]
  (some #(= target %) coll))

(defn apply-filters
  "Apply multiple filters to a collection, returning a list of elements which satisfy them all."
  ; Could probably be moved even to another module: "transformations" or the like.
  ; Could probably be made more efficient by shortcutting with a :while, but that
  ;   would require inverting the loop order (ie loop over collection outside filters.
  [filter-list coll]
  (if-not filter-list
    ; if filter-list is empty, we're done
    coll
    ; otherwise apply this filter and recurse on the rest
    (let [[cur-filter & remaining-filters] filter-list
          filtered-coll (filter cur-filter coll)]
      (apply-filters remaining-filters filtered-coll))))

