(ns mindmap.console.ui.drawing
  (:require [mindmap.console.ui.core :as core])
  (:require [mindmap.console.ui.mm-draw :as mmd])
;  (:require [mindmap.ht :as ht])
  (:require [lanterna.screen :as s]))

; Screen functions -----------------------------------------------------
;

(defn handle-resize
  [cols rows] 
  (dosync (ref-set core/screen-size [cols rows])))

; TODO There is a bug where the intial size of in-term 
;      (:text) is badly off so there is screen cruft until 
;      you interact a bit
(defn create-screen 
  [screen-type]
  (let [screen (s/get-screen screen-type {:resize-listener handle-resize})
        cur-size (s/get-size screen)
        [cols rows] cur-size]
    (handle-resize cols rows)
    screen))

; Viewport of main drawing area ----------------------------------------
;

(defrecord Viewport [x-offset y-offset width height])

; TODO Eventually it may be nice to generalize the drawing 
;      elements s/t the header and cmd-line can be queried
;      instead of hard-coded. lol
; 
(defn get-viewport
  [screen]
  (let [[w h] (s/get-size screen)] 
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
  (let [screen (:screen context)
        txt "Hypertree Console 0.1"
        pad (core/get-center-pad txt) ]
      (s/put-string screen pad 1 txt {:fg :green})))

  ; The cmd-line area is 1 char tall
(defn draw-cmdline 
  [context msg]
  (let [screen (:screen context)
        [width height] @core/screen-size
        h (- height 1)
        wx (+ (count msg) 3) ]
    (s/put-string screen 0 h ">" {:fg :green})
    (s/put-string screen 2 h msg)
    (s/move-cursor screen wx h)))

(defn draw-quick-n-dirty-tree
  [context]
  (let [screen (:screen context)
        msg "EZ Tree"]
    (s/put-string screen (core/get-center-pad msg) 10 msg {:fg :yellow})))

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
    (mmd/draw-mm (get-viewport (:screen context)) context))))

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
  (let [screen (:screen context)]
    (s/clear screen)
    (doseq [ui (:uis context)]
      (draw-ui ui context))
    (s/redraw screen)))

