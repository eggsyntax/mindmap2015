(ns mindmap.console.core
  (:use mindmap.console.ui.core)
  (:import [mindmap.console.ui.core UI])
  (:use mindmap.console.ui.input)
  (:use mindmap.console.ui.drawing)
  (:require [ mindmap.ht :as ht])
  (:require [lanterna.screen :as s]))

; Data Structures -------------------------------------------------------------
;
(defrecord Context  [mode style hyper uis input input-buffer])

(defn new-context  []
    (map->Context {:mode :navigate
                   :style :list
                   :hyper (ht/rand-hypertree)
                   :uis [(->UI :header)
                         (->UI :map)
                         (->UI :cmd-line)]
                   :input nil
                   :input-buffer []
                   }))

; Main ------------------------------------------------------------------------
;
; Process all UI's in the Q 
; 
; When there are no more UIs to process accumulate 
; input that is fed to the input handler for the 
; mode.
;
; Notes;
; 
; o The input handler can initiate a complex set of interactions
;   and tracks when an interaction is complete. Handler's can also
;   switch modes which change the input keymapping and drawing behaviour.
;
; o the :exit mode is special and will exit the application
;
(defn run-app
  [start-context screen]
  (loop [{:keys [mode style hyper uis input input-buffer] :as context} start-context] 
    (when (not= :exit mode)
        (if (nil? input)
          (do 
            (draw-app context screen ) ; Pure function 
            (recur (get-input context screen)))
          (recur (process-input (dissoc context :input) input))))))

(defn main
    ([screen-type]  (main screen-type false))
    ([screen-type block?]
      (letfn  [(go  []
         (let  [screen  (create-screen screen-type)]
            (s/in-screen screen
              (run-app  (new-context) screen))))]
               (if block?
                (go)
                (future (go))))))
