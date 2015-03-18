(ns mindmap.console.ui.drawing
  (:require [mindmap.console.ui.core :as core])
;  (:require [mindmap.ht :as ht])
  (:require [lanterna.screen :as s]))

; Utility Functions -----------------------------------------------------------
;
(def screen-size (ref [80 24]))

; Padding for the Header
;(def top-pad 2)

; Padding for the command-line
;(def bottom-pad 2)

; TODO buffer the last UI stack that was draw and re-render
;
(defn handle-resize
  [cols rows] 
  (dosync (ref-set screen-size [cols rows])))

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

; The Header is 2 Lines tall
(defn draw-header
  [context]
  (let [screen (:screen context)
        txt "Hypertree Console 0.1"
        pad (get-center-pad txt) ]
      (s/put-string screen pad 1 txt {:fg :green})))

(defn draw-cmdline 
  [context msg]
  (let [screen (:screen context)
        [width height] @screen-size
        h (- height 1)
        wx (+ (count msg) 3) ]
    (s/put-string screen 0 h ">" {:fg :green})
    (s/put-string screen 2 h msg)
    (s/move-cursor screen wx h)))

; Truncates the string down to the max-chars
; with an elipsis 
(defn truncate-str
  [string max-chars]
  (let [no-el-width (- max-chars 3)]
    (if (<= (count string) no-el-width)
      string 
      (let [tr-str (subs string 0 no-el-width)]
        (str tr-str "...")))))

; The viewport for drawing the main area 
; of the current console area in between the 
; Header and the Command Line 
; 
(defrecord Viewport [x-offset y-offset width height])

; TODO Eventually it may be nice to generalize the drawing 
;      elements s/t the header and cmd-line can be queried
;      instead of hard-coded. lol
; 
(defn get-viewport
  [screen]
  (let [[w h] (s/get-size screen)] 
    (println "get-viewport> " w "x" h)
    (Viewport. 0 3 w (- h 1))))

;  List View
; 
;Node 00
;  Node 10
;    Node 20
;    Node 21
;      Node 30
;  Node 20
;    Node 22
;    Node 23
;
; Fixed-size? 
; 
; NOTE: The viewport of the graph representation is 
;       padded by 1 line above and 1 line below 
;       of the current screen dims
;
(defn draw-list-node
  [depth h screen]
  (let [viewport (get-viewport screen)]
    (println "draw-list-node> viewport=" viewport)
    ))

; 
; NOTE: The viewport of the graph representation is 
;       padded by 1 line above and 1 line below 
;       of the current screen dims
;
(defn draw-list
  [context]
  (let [screen (:screen context)
        viewport (get-viewport screen) ]
    (println "draw-list> viewport=" viewport)
    (s/put-string screen 10 5 "DRAW LIST"))) 

; Tree View 
;
; Calculate how many nodes can fit on the screen
;
;  Height = 1 char plus vertical padding
;  Width = 12 char plus horizontal padding
;  
; NOTES:
;
;  This should fit as much of the map and children
;  on the screen as possible...
;
;  General Example:
;
;[Node -1   ] 
;             \
;[Node 0    ]  \
;             \ \
;[Node 1    ]  \ \               [Node B     ]
;             \ \ \            /  
;[Node 2    ] -- [Node 3    ] -- [Node A     ]
;             / / /            \
;[Node 5    ]  / /               [Node C     ] 
;             / /
;[Node 6    ]  /
;             /
;[Node 7    ]


; Each node has a fixed size with the following format:
;
;  [Title...]  (12 Chars)
;
; The head of the mindmap is green
; 
; TODO By Convention use :title for now
;      eventually it would be great for this 
;      to be smarter about displaying attributes
;
;[def max-node-width 12]
(defn draw-tree-node
  [node x y screen]
  (let [title (:title node)
        t-title (truncate-str title 12) ]
    (s/put-string screen x y (str "[" t-title "]"))))

(defn draw-tree
  [context]
  (let [screen (:screen context)]
    (s/put-string screen 10 5 "DRAW TREE")))


; UI Handlers ----------------------------------------------------------------
;
(defmulti draw-ui :action)

; This is great but should end up being mapped to actual drawing tasks
; like :tree-view/:list-view, :edit, :inspect, etc.
;
;  NOTE: Each drawing command (like :edit) can also prompt for 
;        task-specific input from the user during its rendering
; 
(defmethod draw-ui :default 
  [ui context]
  ())

(defmethod draw-ui :vis-hyper
  [ui context]
  (draw-header context)
  (let [style (:style context)]
    (case style
      :tree (draw-tree context)
      :list (draw-list context)
      )))

(defmethod draw-ui :cmd-line-inspect-node 
  [ui context]
  ;(println "draw-ui> :cmd-line-inspect-node")
  (draw-cmdline context (core/get-buffer-string context))
  ; Pull Out Node Title for display
  )

(defmethod draw-ui :exit-screen 
  [ui context]
  (draw-header context)
  (draw-cmdline context "Do you want to exit y/n?"))

(defn draw-app [context]
  (let [screen (:screen context)]
    (s/clear screen)
    (doseq [ui (:uis context)]
      (draw-ui ui context))
    (s/redraw screen)))

