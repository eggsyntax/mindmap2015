(ns mindmap.core
  (:require [mindmap.text-view :as tv]))

(defn -main
  "Run the main mindmap program"
  [& args]
  (println "Hello, World!")
  (tv/run-repl)
  )


