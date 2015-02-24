(ns mindmap.core
  (:require [mindmap.console.core :as console])
  (:require [clojure.tools.namespace.repl :only  (refresh)]))

(defn -main [& args]
  (let [args (set args)
        screen-type (cond 
                      (args ":swing") :swing
                      (args ":text")  :text
                      :else           :auto) ]
      (console/main screen-type true)))
