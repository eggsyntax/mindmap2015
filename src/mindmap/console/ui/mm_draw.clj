(ns mindmap.console.ui.mm-draw
  (:require [mindmap.console.ui.core :as core])
;  (:require [mindmap.ht :as ht])
  (:require [lanterna.screen :as s]))

; Main MM drawing function which proxies to the style-specific 
; multimethods.
;
(defn draw-mm 
  "Draws the current portion of the mindmap that fits in the 
  viewport given the currently selected node (cur-node) and style."
  [viewport context]
  (println "mm-draw/draw-mm> viewport:" viewport))

; Tree drawing multimethods ----------------------------------------------------
;

(defmulti viewable-subtree 
  "The subtree which can be drawn on the viewport given the currently
  selected node (cur-node)."
  :style)

(defmulti mindmap-dims 
  "The dimensions of the entire mind-map in relative screen coordinates given
  the viewport and the selected node (cur-node)."
  :style)

(defmulti node-dims 
  "The dimensions of the drawn node in relative screen coordinates."
  :style)

(defmulti node-pos
  "The drawing offset for the node given the viewport, 
  tree context and currently selected node (cur-node) in absolute screen 
  coordinates."
  :style)

(defmulti draw-node 
  :style)
