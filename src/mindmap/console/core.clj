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
    (new Appinfo
       (new Context)
       [(->UI :start)]
       nil))

; Main ------------------------------------------------------------------------
;
(defn run-app  [appinfo screen]
    (loop  [{:keys  [input uis] :as appinfo} appinfo]
          (when-not  (empty? uis)
            (draw-app appinfo screen)
            (if  (nil? input)
              (recur  (get-input appinfo screen))
              (recur  (process-input  (dissoc appinfo :input) input))))))

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
