(ns jug.hello)



"Primitives"

(type 1)

(type 1.0)

(type #"^")





"Strings"

(type "Text")

(type 'Text)

(type :Text)




"Composites"



(type '(1 2 3))

(type [1 2 3])

(type #{1 2 3})

(type {:a 1 :b 2 :c 3})


"Built Ins"

(def mysymbol "value")

(str "Mysymbol: " mysymbol)


(if true "true" "false")

(do (println "one") (println "two") "ok")

(let [a 1] (+ a 2))

;; destructring
(let [[a b] [1 2]
      {c :k} {:k 3}]
  (+ a b c))


(quote (+ 1 2))

'(+ 1 2)

(type '(+ 1 2))


;; symbols and vars

(var mysymbol)

(def ups "ups")

mysymbol

#'mysymbol

(keys (ns-publics 'jug.hello))


;; functions

(def myfn (fn [a b] (+ a b)))

(myfn 1 2)

(defn myfn [a b] (+ a b))

(defn myfn
  "my function"
  [x & xs]
  {:pre [(pos? x)]}
  {:x x :xs xs})

(myfn 2 3)

(myfn 2 3 4)

#_(myfn -2 3 4)

(meta #'myfn)

(defn strlen [x] (.length x))

(strlen "hello")

;; type hints
(defn strlen [^String x] (.length x))

(strlen "hello")


"loop/recur"

(loop [acc 0
       [x & xs] [1 2 3 4]]
  (if xs
    (recur (+ acc x) xs)
    acc))

(try
  (throw (Exception. "ups"))
  (catch Exception e
      (.toString e)))


"MACRO"


(defmacro unless [p t f]
  (list 'if (list 'not p) t f))

(unless true false true)

(unless false false true)

(defmacro unless [p t f]
  `(if (not ~p) ~t ~f))

(-> 5 inc (+ 5) dec)

(defn myfn [])

(for [x [1 2 3]
      :when (odd? x)]
  (inc x))

"Composites"

(first [1 2 3])

(rest [1 2 3])

(conj [1] 2)

(conj #{1} 2)

(conj '(1) 2)

(map inc [1 2 3])

(reduce (fn [a x] (+ a x)) 0 [1 2 3])

((juxt inc dec #(* 5 %)) 1)

(let [xs [1 2 3]]
  {:xs xs
   :uxs (conj xs 4)})

(def man {:name "Nicola"})

(:name man)

(man :name)

(get man :name)

(assoc man :age 35)

(dissoc man :name)

(def man {:name "Nicola" :contacts {:home "home"}})

(get-in man [:contacts :home])

(update-in man [:contacts :home] str "!")

(merge {:a 1} {:b 2})

"persistence"

(= (hash "hello")
   (hash "hello"))

(= (hash [1 2 3])
   (hash [1 2 3]))

"
Persistent?
Like copy, but fixed price cpu & memory
"

clojure.lang.PersistentVector

(def prefix-tree
  {\j { \a {\v {\a {:present true}}}
        \v {\m {:present true}}
        \i {\t {:present true}}}})

(defn in? [tree el]
  (loop [node tree
         [ch & chs] (seq el)]
    (if-not chs
      (get-in node [ch :present])
      (if-let [next-node (get node ch)]
        (recur next-node chs)
        false))))

(in? prefix-tree "jit")

(in? prefix-tree "jito")

(hash \h)

(hash "hello")

(hash [1 2 3])



"STATE"

(def mystate (atom []))

(defmacro in-thread [& bd]
  `(.start (Thread. (fn [] ~@bd))))

(dotimes [i 10]
  (in-thread
   (java.lang.Thread/sleep (rand 100))
   (swap! mystate conj i)))

(reset! mystate [])

@mystate


(def myagent (agent 100 :validator pos?))

(send myagent - 50)

@myagent


(def acc1 (ref 100))

(def acc2 (ref 100))

(dosync
 (let [sum 89]
   (alter acc1 - sum)
   (alter acc2 + sum)
   #(send myagent 'some-io)))

(+ @acc1 @acc2)


"Polimorphism"

(defprotocol Sqlable
  (to-sql [this]))

(extend-protocol Sqlable
  nil
  (to-sql [_] "NULL"))

(extend-protocol Sqlable
  java.util.Date
  (to-sql [dt] (.toGMTString dt)))

(defn to-json [_] "{\"a\":1}")

(extend-protocol Sqlable
  clojure.lang.PersistentArrayMap
  (to-sql [mp] (to-json mp)))

(to-sql nil)

(to-sql #inst"2011-01-01")

(to-sql {:a 1})
