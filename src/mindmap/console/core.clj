(ns mindmap.console.core
  (:use mindmap.console.ui.core)

  ; Ensure that the generated class behind the defrecord is pulled in
  (:import [mindmap.console.ui.core UI])

  (:use mindmap.console.ui.input)
  (:use mindmap.console.ui.drawing)
  (:require [lanterna.screen :as s]))

; Data Structures -------------------------------------------------------------
;
; TODO These will get moved into other places
(defrecord Context  [])
(defrecord Appinfo  [context uis input])

(defn new-appinfo  []
    (map->Appinfo {:context nil
                   :uis [(->UI :navigate)]
                   :input nil
                   }))

; Main ------------------------------------------------------------------------
;
(defn run-app  [appinfo screen]
    (loop  [{:keys  [input uis] :as appinfo} appinfo]
      (when (seq uis)
        (recur (if input
                 (-> appinfo
                     (dissoc :input)
                     (process-input input))
                 (-> appinfo
                     ; This is where each item renders its
                     ; behaviour
                     ; (update-in [:context] tick-all )
                     (draw-app screen)
                     (get-input screen))))))) 

(defn main
    ([screen-type]  (main screen-type false))
    ([screen-type block?]
      (letfn  [(go  []
         (let  [screen  (create-screen screen-type)]
            (s/in-screen screen
              (run-app  (new-appinfo) screen))))]
               (if block?
                (go)
                (future (go))))))
