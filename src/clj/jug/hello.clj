(ns jug.hello
  (:require [clojure.string :as str]))

(defmacro WHAT [& x])
(defmacro HOW [& x])
(defmacro THIS [& x])
(defmacro JS [& x])
(defmacro AST-JSON [& x])

(WHAT is clojure?)

(THIS is
      functional LISP
      on JVM (JS, CLR)
      for concurrency)

(HOW old is clojure?)

(def clj-birth-date #inst "2007-01-01")

(->
 (.getTime (java.util.Date.))
 (- (.getTime clj-birth-date))
 (/ (* 360 24 60 60 1000))
 (float))


(WHAT is LISP?)

(JS function sum(a, b) { return a*a + b*b })

(AST-JSON node -> ["*" "a" "a"])

(AST-JSON ["function" "sum" ["a" "b"]
            [+ [* "a" "a"] [* "b" "b"]]])

'[function sum [a b]
    [+ [* a a] [* b b]]]

'(function sum [a b]
           (+ (* a a) (* b b)))

(def ast
  '(defn sum [a b]
    (+ (* a a) (* b b))))
(map (comp count str) ast)

(eval ast)

(defn sum [a b]
  (+ (* a a) (* b b)))

(sum 2 3)


(THIS is HOMOICONICITY)


(WHAT Primitives)

(type 1)

(type 1.0)

(type #"^")


(type "Text")

(type 'Text)

('add 2 3)


(type :Text)

{:a 1,:b 2}

(name :text)

(name 'text)



"Composites"

(type '(1 2 3))

(list 1 2 3)

(type [1 2 3])

(vector 1 2 3)

(type #{1 2 3})

(type {:a 1 :b 2 :c 3})

(def colls [[1 2 3] #{1 2 3} '(1 2 3)])


(map (fn [c] (conj c 4)) colls)

(map first colls)

(map rest colls)

(map #(reduce + 0 %) colls)

(map #(filter odd? %) colls)

(get [1 2 3 4] 2)

(->> (range)
     (filter odd?)
     (take 105))

(def my-map {:a 1 :b 3})

(get my-map :a)

(:a my-map)

(my-map :a)

(assoc my-map :d 5)

(reduce (fn [acc [k v]] (+ acc v)) 0 my-map)

(filter (fn [[k v]] (> v 2)) my-map)

(dissoc my-map :a)

(get-in {:a {:b 5}} [:a :b])

(update-in {:a {:b 5}} [:a :b] inc)

(merge {:a 1} {:b 2})


"Built Ins"

;; def
(def mysymbol "value")

(str "Mysymbol: " mysymbol)

;; symbols and vars

mysymbol

(type mysymbol)

(type (var mysymbol))

#'mysymbol

(->> *ns*
     (ns-publics)
     (keys)
     (sort)
     (take 10))

(->> 'clojure.core
     (ns-publics)
     (keys)
     (sort)
     (take 10))

(type *ns*)

(keys (ns-aliases *ns*))

(require '[clojure.repl :as repl])

(keys (ns-aliases *ns*))

(keys (ns-publics 'clojure.repl))

(repl/apropos "reduce")


;; if
(if true "true" "false")

;; do
(do (println "one")
    (println "two")
    "ok")

;; let
(let [a 1] (+ a 2))

;; destructring

(let [[a b] [1 2]] (+ a b))

(let [[a & rst] [1 2 3 4 5]] rst)

(let [{a :a} {:a 1 :b 2}] a)

(let [[a b] [1 2]
      {c :k} {:k 3}]
  (+ a b c))

;; quotation
(quote (+ 1 2))

'(+ 1 2)

(type '(+ 1 2))


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

(defn multi-arity
  ([x] 1)
  ([x y] 2)
  ([x y & z] (+ 2 (count z))))

(multi-arity 1)

(multi-arity 1 2)

(multi-arity 1 2 3 5)


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

"persistence"

(= (hash "hello")
   (hash "hello"))

(= (hash [1 2 3])
   (hash [1 2 3]))

"
Persistent?
Like copy, but fixed price by cpu & memory
"

clojure.lang.PersistentVector

(def prefix-tree
  {\j { \a {\v {\a {:value "java"}}}
        \v {\m {:value "jvm"}}
        \i {\t {:value "jit"}}}})

'PERSISTENCE

(def trie {:a {:aa {:aaa 1}}
              {:b  {:c 2}}
              {:aa {:aaa 3}}})

(def new-trie {:a' {:aa  {:aaa 1}}
                   {:b'  {:c' 3}}
                   {:aa  {:aaa 3}}})

'(STRUCTURE-SHARING & PATH-COPYING)

;; HAMT
;; hash array mapped trie

(def node {:bitmap   [1 0 0 1]
           :children ['node-ref 'node-ref]})

(hash \h)

(hash "hello")

(hash [1 2 3])



"STATE"

(def mystate (atom []))


(defmacro sleep-rand []
  `(java.lang.Thread/sleep (rand 100)))

(defmacro in-thread [& bd]
  `(.start (Thread. (fn [] ~@bd))))

(defmacro in-thread-rand [& bd]
  `(in-thread (sleep-rand) ~@bd))

(dotimes [i 10]
  (in-thread-rand (swap! mystate conj i)))

@mystate

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


'MULTI-METHOD
'runtime-polymorphism

(defmulti encounter (fn [x y] [(:Species x) (:Species y)]))

(defmethod encounter [:Bunny :Lion] [b l] :run-away)

(defmethod encounter [:Lion :Bunny] [l b] :eat)

(defmethod encounter [:Lion :Lion] [l1 l2] :fight)

(defmethod encounter [:Bunny :Bunny] [b1 b2] :mate)

(def b1 {:Species :Bunny :other :stuff})

(def b2 {:Species :Bunny :other :stuff})

(def l1 {:Species :Lion :other :stuff})

(def l2 {:Species :Lion :other :stuff})

(encounter b1 b2)

(encounter b1 l1)

(encounter l1 b1)

(encounter l1 l2)
