(ns mindmap.serialize
  (require [clojure.edn :as edn])
  )

"Protocol and functions for serialization (currently to file, later for live
collaboration and possibly others)."

(defprotocol SerializeProtocol
  (serialize [structure]
             "Create a serialization of this structure (typically a hypermap)")
  (deserialize [text-chunk]
               "Reconstruct original structure from  serialization."))

;(defn serialize-for-file
;
;  )
