(ns mindmap.util
  (:use [clojure.pprint :only (pprint)])
  (:require [clojure.tools.namespace.repl :as nsrepl]
            [clojure.zip :as zip])
  (:import [java.io StringWriter]
           [java.util Random]))

(def debug-mode (atom false)) ; rebind this in the REPL or wherever as desired

(defn r!
  "Reset REPL.
  See https://github.com/clojure/tools.namespace for details."
  []
  (nsrepl/refresh))

(defn get-indexer
  "Helper function to create an indexer for a mm. Whenever the returned fn
  is called, it returns an incremented index.
  TEST only! Shouldn't ever be used in prod."
  []
  (def add-and-get
    (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
      (fn [] (.addAndGet ai 1)))))

(def main-indexer (get-indexer))

(defn gen-id
  [item]
  (if debug-mode
    (main-indexer)
    (hash item)))

(defn with-id
  "Wrapper around any function that returns a maplike object, which adds
  an :id field containing the hash of the object. Exists so that it's easy
  to add :id last, so it'll contain as much info as possible.
  If debug-mode evaluates to true, use sequential integer indexes (unique
  per-run) instead for readability.
  Note that there's some pathology in clojure hashcode, so this might be a
  place to check for performance issues.
  http://dev.clojure.org/display/design/Better+hashing "
  [item]
    (assoc item
           :id
           (gen-id item)))

;TODO think hard about what gets a timestamp, and when. Remember that (in
;current design) if timestamp changes, id changes.
(defn timestamp
  "Return current timestamp in ms since epoch"
  []
  (System/currentTimeMillis))

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

(defn ppprint [thing]
  (println (to-str thing)))

(defn print-head [hype]
  (ppprint
    (let [root (zip/node (:nodes hype))]
      (:mm root))))

(defn spaces [n]
  "Get some spaces for padding a string"
  (take (* n 2) (repeat \space)))

(spaces 5)

(defmacro demo
  "demo macro just prints itself and its results to console, and returns the results."
  [form]
  (println "***" form "***")
  (println)
  (let [results (eval form)]
    (ppprint results)
    results))

(defn no-nils? [coll]
  (every? #(not (nil? %)) coll))

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

(defn seeded-rng
  "Returns a random number generator with the given seed. By convention,
  passing a seed of -1 returns a different-seeded RNG every time."
  [seed]
  (let [rng-seed (if (= seed -1)
                   (rand-int Integer/MAX_VALUE)
                   seed)]
   (Random. rng-seed)))

