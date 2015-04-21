(ns mindmap.serialize
  (:use [mindmap.util :only [to-str]])
  (:require [clojure.edn :as edn]
            [mindmap.util :as ut]
            [clojure.zip :as zip]
            [mindmap.mm :as mm]
            [mindmap.ht :as ht]))


;TODO except hmm, do we want timestamps for mindmaps rather than hypermaps? Hmm.
(defprotocol SerializeProtocol
  "Protocol for serialization (currently to edn, possibly other formats later."
  (serialize [item]
             "Create a serialization of this structure")
  (deserialize [text-chunk]
               "Reconstruct original structure from  serialization."))


;TODO these will be extended to the hypermap defrecord, but that doesn't exist
; yet in this branch. When it does, we'll do it as follows (untested):
(comment
  extend Hypermap
    SerializeProtocol
    {:serialize   (fn [hypermap] (serialize-to-edn hypermap))
     :deserialize (fn [text-chunk] (deserialize-from-edn text-chunk))})

(defn serialize-to-edn
  "All we want to do is output an edn representation suitable for writing to a
  file which git can handle nicely. This turns out to be absurdly trivial. The
  only reason we bother to use our custom to-str instead of clojure's core.str
  is to get line separators so git won't see it as all one long line."
  [item]
  (to-str item))

(defn deserialize-from-edn
  "Takes a string (eg from a slurped file)"
  [text-chunk]
  (ht/ht-from-map (edn/read-string text-chunk)))

(defn save-ht [filename hype]
  (spit filename (serialize-to-edn hype)))

(defn load-ht [filename]
  (deserialize-from-edn (slurp filename)))
