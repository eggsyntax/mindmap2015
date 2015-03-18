(ns mindmap.console.ui.input
  (:use mindmap.console.ui.core )
  (:import [mindmap.console.ui.core UI])
  (:require [mindmap.util :as ut])
  (:require [lanterna.screen :as s]))

(defn- get-buffer-string
  "Returns the characters in the input buffer ommitting 
   the special enter character if it exists."
  [context]
  (let [buffer (:input-buffer context)]
    (if (= (count buffer) 1)
      (peek buffer)
      (apply str buffer))))

(defn- clear-input-buffer
  [context]
  (assoc context :input-buffer []))

(defn- buffer-input
  [context input]
  (assoc 
    context
    :input-buffer
    (conj (:input-buffer context) input)))

(defn- buffer-has-enter-key?
  [context]
  (ut/seq-contains? (:input-buffer context) :escape))

(defn get-input 
  [context]
  (let [screen (:screen context)
        ; Get one character at a time accumulating it
        ; unless its fully processed
        input (s/get-key-blocking screen)]
    (println "get-input> input=" input)
    (assoc context :input input)))

; Processes the input given the input mode 
; 
(defmulti process-input :mode)

(defmethod process-input :default 
  [context input] 
  (assoc context :mode :exit))

(defmethod process-input :navigate 
  [context input]
  (let [cleared-context (clear-input-buffer context)]
    (case input
     \q  (-> cleared-context
            (assoc :uis [(->UI :exit-screen)])
            (assoc :mode :exit-validate)
            )

     ; Default  
     (-> cleared-context
        (assoc :uis [ (->UI :vis-hyper)
                      (->UI :cmd-line-inspect-node) ])
        (buffer-input input cleared-context)))))

(defmethod process-input :exit-validate
  [context input]
  (if (= input \y)
    (assoc context :mode :exit)
    (-> context
        (assoc :uis [(->UI :vis-hyper)
                     (->UI :cmd-line-inspect-node) ])
        (assoc :mode :navigate))))

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
  ())



