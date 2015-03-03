(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [lanterna.screen :as s]))

(defmulti process-input :mode)

(defmethod process-input :default 
  [context input] 
  ; push the exit mode
  (assoc context :mode :exit))

(defmethod process-input :navigate 
  [context input]
  (if (= input :enter)
    (do 
      (assoc context :uis [(->UI :win)])
      )
    (assoc context :uis [(->UI :lose)])))

; TODO If there is no direct mapping 
; then buffer the input for later
;
;  o If there is an existing input buffer
;    first check that the new input value
;    combined with the buffer triggers 
;    and action
; 
;   i.e: Edit 
(defmethod process-input :add-node
  [context input]
  (
   ))

(defn get-input [context screen]
  (let [input (s/get-key-blocking screen)]
    ;
    ; TODO 
    ; Only one character at a time. Specific input handler 
    ; uses the buffer to make complex interactions
    ; 
    (assoc (:input context) input)))
