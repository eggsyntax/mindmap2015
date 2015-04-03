(ns mindmap.temp
  (:require [mindmap.util :as ut]
            [mindmap.mm :as mm]
            [mindmap.tree :as tr]
            [clojure.zip :as z]
        )
  )

; (ut/r!)

; Turn an example tree into a zipper...?
(let [_ (ut/reset-indexer)
      _ (ut/reset-epoch)
      rmm (mm/rand-mm :num-nodes 8 :seed 3)
      rmm-root (mm/get-root rmm (mm/get-cur rmm))
      example-tree (tr/to-tree rmm rmm-root)]
  (ut/ppprint example-tree)
  (ut/ppprint (str "Root: " (:title (first example-tree))))

  (let [root (first example-tree)
        zm (z/seq-zip example-tree)]
    (ut/ppprint zm)

    (ut/ppprint
      (-> zm
        z/down
        z/right
        z/right
        z/down
        z/right
        z/down
        z/node
        ))
;     (ut/ppprint (z/rightmost zm))
    )
  )

(defn zipper-at
  "Turn a mindmap into a zipper with a particular edit location already set.
  Finds edit location by context, so O(n)."
  [mindmap node]
  ; Do we want to find it by position (in some sense of that) or by
  ; content/id? I'm not sure. Is there an efficient way to do the latter?
  (let [root (mm/get-root mindmap (mm/get-cur mindmap))
        zipper (z/seq-zip (tr/to-tree mindmap root))
        zipper-opened (-> zipper

                          )

            ;TODO YOUAREHERE write a recursive fn (maybe using walk?)
            ; which stops when the current node matches the node param
            ; (returning the zipper at that point)

            ; Something like:
            (while (not= (:node zipper) node)
              (recur (z/next (z/node)))
            ; And then HOPEFULLY the return value is the zipper, with the cursor set?

              )
        ]
    zipper-opened
    )
  )

(let [_ (ut/reset-indexer)
      _ (ut/reset-epoch)
      rmm (mm/rand-mm :num-nodes 8 :seed 3)
      rmm-root (mm/get-cur rmm)
;       _ (println (:nodes rmm))
      a-node (nth (vals (:nodes rmm)) 5)
      last-node (last (vals (:nodes rmm)))
      ;example-tree (tr/to-tree rmm rmm-root)
      zipper (zipper-at rmm a-node)
      ]
  (ut/ppprint  zipper)
  ;(ut/ppprint (zipper-at rmm last-node))
  )

#( (fn [a] ( (fn [b] (* a b)) (inc a))) 1)
