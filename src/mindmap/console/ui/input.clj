(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [lanterna.screen :as s]))

(defmulti process-input 
  (fn [appinfo input]
    (:kind (last (:uis appinfo)))))

(defmethod process-input [] [appinfo input]
  ())

(defmethod process-input :navigate [appinfo input]
  (if (= input :enter)
    (do 
      (assoc appinfo :uis [(->UI :win)]))
    (assoc appinfo :uis [(->UI :lose)])))

(defmethod process-input :win [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis [(->UI :navigate)])))

(defmethod process-input :lose [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis [(->UI :navigate)])))

(defn get-input [appinfo screen]
  (let [input (s/get-key-blocking screen)]
    (assoc appinfo :input input)))
