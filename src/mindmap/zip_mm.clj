(ns mindmap.zip-mm
  (:require [clojure.zip :as z]
            [mindmap.tree :as tr]
            [mindmap.mm :as mm]))

(defn unzip-to
  "Return a zipper unzipped to a particular node, or nil if not found"
  [zipper node]
  (loop [loc zipper]
    (let [cur (z/node loc)]
      (cond
        (= cur node) loc
        (z/end? loc) nil
        :else (recur (z/next loc))))))

(defn zipper-at
  "Turn a mindmap into a zipper with a particular edit location already set.
  Finds edit location by content, so O(n)."
  [mindmap node]
  ; Do we want to find it by position (in some sense of that) or by
  ; content/id? I'm not sure. Is there an efficient way to do the latter?
  (let [root (mm/get-root mindmap (mm/get-cur mindmap))
        zipper (z/seq-zip (tr/to-tree mindmap root))
        unzipped-zipper (unzip-to zipper node)]
    (unzip-to zipper node)))
