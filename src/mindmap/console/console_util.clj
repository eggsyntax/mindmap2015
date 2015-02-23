(ns mindmap.console-util
  (:require lanterna.screen :as s))

; Lanterna doesn't provide a clear screen function
(defn clear-screen  [screen scol srow]
    (let  [blank  (apply str  (repeat scol \space))]
          (doseq  [row  (range srow)]
                  (s/put-string screen 0 row blank))))





