(ns mindmap.console.ui.drawing
  (:require [lanterna.screen :as s]))

; Utility Functions -----------------------------------------------------------
;
(def screen-size (ref [80 24]))

; TODO buffer the last UI stack that was draw and re-render
;
(defn handle-resize
  [cols rows] 
  (dosync (ref-set screen-size [cols rows])))

(defn create-screen 
  [screen-type]
  (let [screen (s/get-screen screen-type {:resize-listener handle-resize})
        cur-size (s/get-size screen)
        [cols rows] cur-size]
    (handle-resize cols rows)
    screen))

(defn clear-screen  [screen]
    (let [[cols rows] @screen-size
          blank  (apply str  (repeat cols \space))]
      (doseq [row  (range rows)]
         (s/put-string screen 0 row blank))))

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

; Drawing Functions -----------------------------------------------------------
;
(defn draw-header [screen]
  (let [txt "Hypertree Console 0.1"
        pad (get-center-pad txt)
        ]
      (s/put-string screen pad 1 txt {:fg :green})
    ))

(defn draw-cmdline 
  [appinfo screen msg]
  (let [[width height] @screen-size
        pos (- height 1)
       cur-pos (+ (count msg) 2)
       ]
    (s/put-string screen 0 pos ">" {:fg :green})
    (s/put-string screen 2 pos msg)
    (s/move-cursor screen 0 pos)))

(defn draw-node
  [appinfo screen]
  (let []
    ()
   )
  )

; UI Handlers ----------------------------------------------------------------
;
(defmulti draw-ui
  (fn [ui appinfo screen]
    (:kind ui)))

(defmethod draw-ui [] [ui appinfo screen]
  ())

(defmethod draw-ui :navigate [ui appinfo screen]
  (draw-header screen)
  (s/put-string screen 10 5 "Press enter to win, anything else to lose" )
  (draw-cmdline appinfo screen "Navigation Mode")
  )

(defmethod draw-ui :win [ui appinfo screen]
  (draw-header screen)
  (s/put-string screen 10 5 "Win Mode: press escape to exit anything else to restart")
  (draw-cmdline appinfo screen "Win Mode")

  )

(defmethod draw-ui :lose [ui appinfo screen]
  (draw-header screen)
  (s/put-string screen 10 5 "Lose input: press escape to exit anything else to go")
  (draw-cmdline appinfo screen "Lose Mode")
  )

(defn draw-app [appinfo screen]
  (clear-screen screen)
  (doseq [ui (:uis appinfo)]
    (draw-ui ui appinfo screen))
  (s/redraw screen)
  appinfo)

