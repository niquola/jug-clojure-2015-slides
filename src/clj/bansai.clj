(ns bansai)

(defn cget [enums what key] (get-in enums [what key]))

(defn mk-attr [enums attr default]
  (fn -tmp
    ([m] (-tmp m default))
    ([m val] (assoc m attr (if (keyword? val) (cget attr val) val)))))

(defn mk-const
  ([mm] (fn [m] (merge m mm)))
  ([k a] (fn [m] (assoc m k a))))

(defmacro rules [& body] `(-> {} ~@body))

(defn process-rule [opts]
  (if (map? opts)
    opts
    (reduce (fn [acc mixin]
              (cond
                (vector? mixin) (apply (first mixin) acc (rest mixin))
                (map? mixin) (merge acc mixin)
                :else (mixin acc)))
            {} opts)))

(def style [:.wrap []])

(defn dsl->garden [[rule opts & nested]]
  (if (keyword? rule) 
    (into [rule (process-rule opts)] (map dsl->garden nested))
    (rule opts (map dsl->garden nested))))


