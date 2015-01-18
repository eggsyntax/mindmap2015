(ns mindmap.core
  (:require [mindmap.util :as ut])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

"
Hypermap
  Mindmap
    Node
    Edge
    Cur"

(defn default-node [] {:title "New mindmap"})
(default-node)
(defn default-mindmap []
  (let [first-node (default-node)]
    {:nodes [first-node] 
     :edges []
     :cur first-node }))
(def hypermap
  (let [first-mindmap (default-mindmap)]
    (atom {:maps [first-mindmap]
           :head first-mindmap
           })))
(print (ut/to-str @hypermap))

(defn cur-title
  "Return the title of the current node of a hypermap's current map"
  [hm]
  (get-in hm [:head :cur :title]))

(cur-title @hypermap)

; Observe that we can use get-in/assoc-in/update-in to dig down the
; whole way.
(get-in @hypermap [:head :nodes 0 :title])
