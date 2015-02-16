(ns mindmap.core
  (:require [mindmap.text-view :as tv])
  (:require [clojure.tools.namespace.repl :only  (refresh)]))

(defn -main
  "Run the main mindmap program"
  [& args]
  (println "Hello, World!")
  (tv/run-repl)
  )


