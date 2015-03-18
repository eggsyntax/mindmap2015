(ns mindmap.console.ui.core
  (:require [mindmap.util :as ut]))

(defrecord UI [action])

(defn get-buffer-string
  "Returns the characters in the input buffer ommitting 
   the special enter character if it exists."
  [context]
  (let [buffer (:input-buffer context)]
    (if (= (count buffer) 1)
      (peek buffer)
      (apply str buffer))))

(defn clear-input-buffer
  [context]
  (assoc context :input-buffer []))

(defn buffer-input
  [context input]
  (assoc 
    context
    :input-buffer
    (conj (:input-buffer context) input)))

(defn buffer-has-enter-key?
  [context]
  (ut/seq-contains? (:input-buffer context) :escape))

