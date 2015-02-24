(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [lanterna.screen :as s]))

(defmulti process-input 
  (fn [appinfo input]
    (:kind (last (:uis appinfo)))))

; TODO
; 
;  Write a function that takes a seq 
; and inserts the keyword names into 
; the UI format
;
(defn wrap-cmd
  [cmd]
  [(->UI :header)
   (->UI cmd)
   ;(->UI :cmdline) 
   ])

(defmethod process-input [] [appinfo input]
  ())

(defmethod process-input :start [appinfo input]
  (if (= input :enter)
    (assoc appinfo :uis (wrap-cmd :win))
    (assoc appinfo :uis (wrap-cmd :lose))))

(defmethod process-input :win [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis (wrap-cmd :start))))

(defmethod process-input :lose [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis (wrap-cmd :start))))

; 
(defn get-input [appinfo screen]
  (let [input (s/get-key-blocking screen)]
    (println "get-input> " input)
    (assoc appinfo :input input)
    ))
