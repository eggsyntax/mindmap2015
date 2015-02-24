(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [lanterna.screen :as s]))

(defmulti process-input 
  (fn [appinfo input]
    (:kind (last (:uis appinfo)))))

(defmethod process-input [] [appinfo input]
  (println "No Input"))

(defmethod process-input :start [appinfo input]
  (if (= input :enter)
    (assoc appinfo :uis [(->UI :win)])
    (assoc appinfo :uis [(->UI :lose)])))

(defmethod process-input :win [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis [(->UI :start)])))

(defmethod process-input :lose [appinfo input]
  (if (= input :escape)
    (assoc appinfo :uis [(->UI [])])
    (assoc appinfo :uis [(->UI :start)])))

(defn get-input [appinfo screen]
  (assoc appinfo :input (s/get-key-blocking screen)))
