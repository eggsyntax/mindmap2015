(ns mindmap.console.ui.mm-draw
  (:require [mindmap.console.ui.core :as core])
;  (:require [mindmap.ht :as ht])
  (:require [lanterna.screen :as s]))


(defmulti node-bounds :style)
(defmulti tree-bounds :style)
(defmulti anchor-node :style)
(defmulti viewable-subtree :style)
(defmulti draw-node :style)
(defmulti draw-mm :style)

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
