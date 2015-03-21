(ns mindmap.exampletree
  (:use
    [mindmap.util :as ut]
    [mindmap.mm :as mm]
    [mindmap.tree :as tr]
    ))

; (ut/r!)
(ut/reset-indexer)
(def rmm (mm/rand-mm :num-nodes 5 :seed 1))
(ut/ppprint rmm)
(def root (val (last (:nodes rmm))))
(ut/ppprint root)
(ut/ppprint (:title root))
(ut/ppprint (tr/to-tree rmm root 12))
; (as-tree/to-tree rmm root 12)
; (def tr (as-tree/to-tree rmm root 12))
; (ut/ppprint tr)
; (ut/ppprint rmm)
; (ut/ppprint tr)
