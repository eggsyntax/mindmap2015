(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [lanterna.screen :as s]))

(defmulti process-input :mode)

(defmethod process-input :default 
  [context input] 
  ; push the exit mode
  (println "process-input> input=" input)
  (assoc context :mode :exit))

(defmethod process-input :navigate 
  [context input]
  (if (= input :escape)
    (assoc context :mode :exit)
    (assoc context :uis [ (->UI :header)
                          (->UI :vis-hyper)
                          (->UI :cmd-line-inspect-node) ])))

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
    (println "process-input> :add-node input=" input)
   ; Abort add
   ; Add-title s/t user-input is reflected on the cmd-line...
   ;
   ; 
   ))

(defn get-input [context]
  (let [screen (:screen context)
        input (s/get-key-blocking screen)]
    ;
    ; TODO 
    ; Only one character at a time. Specific input handler 
    ; uses the input buffer to make complex interactions
    ; and to enable mirroring the user input to the screen
    ; when appropriate
    ; 
    (println "get-input> input=" input)
    (assoc context :input input)))
