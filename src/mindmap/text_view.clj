(ns mindmap.text-view
  (:require [mindmap.hm :as mp]
            [mindmap.util :as ut]))


(defn validated
  [input]
  true) ;TODO write actual validation

(defn run-repl
  []
  (loop [value (do
               (println "What is your decision?")
               (read-line))]
    (if (validated value)
      value
      (recur (do
               (println "That is not valid.")
               (println "What is your decision?")
               (read-line))))))
