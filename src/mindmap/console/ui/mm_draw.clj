(ns mindmap.console.ui.mm-draw
  (:require [mindmap.console.ui.core :as core])
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

; List View implementations ----------------------------------------------------
;
; 
;Node Title 00
;  Node Title 10
;    Node Title 20
;    Node Title 21
;      Node Title 30
;  Node Title 20
;    Node Title 22
;    Node Title 23
;
; Each drawn node title as has fixed-size of 15 characters
; 


; Tree View implementations ---------------------------------------------
;
; Calculate how many nodes can fit on the screen
;
;  Height = 1 char plus vertical padding
;  Width = 12 char plus horizontal padding
;  
; NOTES:
;
;  This should fit as much of the map and children
;  on the screen as possible by substituting something
;  for a available subtree so that the subtree available 
;  from the current node is fully expressed.
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
;
; Each node has a fixed size with the following format:
;
;  [Title...]  (12 Chars)
