(ns mindmap.console.ui.core
  (:require [mindmap.util :as ut]))

; Kept up to date by drawing. 
(def screen-size (ref [80 24]))

(defrecord UI [action])

; ------------- Input Buffer Functions ------------------------
;

(defn save-input
  [context input]
  (assoc 
    context
    :input-history
    (conj (:input-history context) input)))

(defn clear-input-history
  [context]
  (assoc context :input-history []))

(defn get-history-string
  "Returns the characters in the input buffer ommitting 
   the special enter character if it exists."
  [context]
  (let [history (:input-history context)]
    (if (= (count history) 1)
      (peek history)
      (apply str history))))

(defn history-has-enter-key?
  [context]
  (ut/seq-contains? (:input-history context) :escape))


; ------------ General Drawing functions ---------------------------
;

(defn truncate-str
  "Truncates the string down the size max-chars substituting 
   and elipsis when truncating."
  [string max-chars]
  (let [no-el-width (- max-chars 3)]
    (if (<= (count string) no-el-width)
      string 
      (let [tr-str (subs string 0 no-el-width)]
        (str tr-str "...")))))

; This could be generalized by type of padding required
;
; i.e. height-centered vs width-centered, alignment
(defn get-center-pad 
  "Returns the amount of padding required to center this text"
  [txt]
  (let [[width] @screen-size
        diff (- width (count txt)) ]
    ; Round up to the nearest cell
    (Math/round (float (/ diff 2)))))






