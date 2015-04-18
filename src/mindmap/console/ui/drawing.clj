(ns mindmap.console.ui.drawing
  (:use [clojure.pprint :only (pprint)])
  (:require [mindmap.console.ui.core :as core])
  (:require [mindmap.console.ui.mm-draw :as mmd])
  (:require [clojure.string :as str])
  (:require [mindmap.ht :as ht])
  (:require [mindmap.mm :as mm])
  (:require [mindmap.util :as ut])
  (:require [mindmap.tree :as tree])
  (:require [clojure.zip :as z])
  (:require [lanterna.screen :as s]))

; Viewport of main drawing area ----------------------------------------
;
(defrecord Viewport [x-offset y-offset width height])

; TODO Eventually it may be nice to generalize the drawing 
;      elements s/t the header and cmd-line can be queried
;      instead of hard-coded. lol
; 
(defn get-viewport
  []
  (let [[w h] (s/get-size @core/screen)] 
    ; The Viewport is the drawable area less the 
    ; header, cmd-line and margins
    ; 
    (println "get-viewport> " w "x" h)
    (Viewport. 0 3 w (- h 1))))

; Specific drawing functions --------------------------------------------
;

  ; The Header is 2 char tall
(defn draw-header
  [context]
  (let [ txt "Hypertree Console 0.1"
        pad (core/get-center-pad txt) ]
      (s/put-string @core/screen pad 1 txt {:fg :green})))

  ; The cmd-line area is 1 char tall
(defn draw-cmdline 
  [context msg]
  (let [ [width height] @core/screen-size
        h (- height 1)
        wx (+ (count msg) 3) ]
    (s/put-string @core/screen 0 h ">" {:fg :green})
    (s/put-string @core/screen 2 h msg)
    (s/move-cursor @core/screen wx h)))

(defn find-depth
  ([tree]
   (find-depth 1 tree))
  ([depth tree]
   (let [next-depth-fn (partial find-depth (inc depth))
         node  (first tree)
         cur-node (assoc node :depth depth) ]
     (cons 
       cur-node 
        (map next-depth-fn (rest tree))))))

(defn print-depth-node
  [row node]
  (let [col (:depth node)]
    (println row "x" col "Title:" (:title node)) 
    (s/put-string @core/screen col (+ 5 row) (:title node))
    (inc row)))

(defn print-tree
  [tree]
  (let [dt (find-depth tree)
        dfs-dt (flatten dt)]
    (reduce print-depth-node 0 dfs-dt)))

(defn draw-quick-n-dirty-tree
  [context]
  (let [ msg "EZ Tree"
        mm (ht/get-head (:hyper context))
        tree (tree/to-tree mm) ]
    (print-tree tree)))

; UI Handlers ----------------------------------------------------------------
;

(defmulti draw-ui :action)

(defmethod draw-ui :default 
  [ui context]
  ())

(defmethod draw-ui :vis-hyper
  [ui context]
  (draw-header context)
  (let [style (:style context)]
   (if (= :quickndirty style)
    (draw-quick-n-dirty-tree context)
    (mmd/draw-mm (get-viewport) context))))

(defmethod draw-ui :cmd-line-inspect-node 
  [ui context]
  (draw-cmdline context (core/get-history-string context))
  ; Pull Out Node Title for display
  )

(defmethod draw-ui :exit-screen 
  [ui context]
  (draw-header context)
  (draw-cmdline context "Do you want to exit y/n?"))

(defn draw-app [context]
  "The main render loop"
   (println "draw-app screen=" @core/screen)
   (s/clear @core/screen)
   (doseq [ui (:uis context)]
      (draw-ui ui context))
   (s/redraw @core/screen))

