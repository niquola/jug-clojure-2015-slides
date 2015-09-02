(ns memo)

(defn init-socket []
  (let [l (.-location js/window)
        s (js/WebSocket. (str "ws://" (.-host l) "/repl"))]
    (reset! socket s)
    (.log js/console s)
    (aset s "onclose" init-socket)
    (aset s "onmessage" (fn [x] (.log js/console  "WS:" x)
                          (on-message (.-data x))))))
(fn [e]
  (when (= 13 (.-which e))
    (send-message (-> e .-target .-value))
    (aset (-> e .-target) "value" "")))

(defn repl-eval [expr]
  (try
    (with-out-str
      (println (eval (read-string expr))))
    (catch Exception e
      (with-out-str (trc/print-stack-trace e)))))

(jdbc/query db ["SELECT 1"])

(defn q [sql] (jdbc/query db sql))

(q ["SELECT 1"])

(defn hq [hsql] (q (sql/format hsql)))

(hq {:select [1]})

(defn e! [sql] (jdbc/execute! db [sql]))

(defn migrate-up []
  (e! "create table repl (id serial primary key, user_name text, expr text, result text, created_at timestamp default current_timestamp)"))

(defn migrate-down []
  (e! "drop table repl"))

(defn recreate []
  (migrate-down)
  (migrate-up))

(defn all []
  (hq {:select [:*]
       :from [:repl]
       :order-by [[:created_at :desc]]}))

(defn create [msg]
  (println "INSERT repl " msg)
  (first (jdbc/insert! db :repl msg)))

(comment
  (recreate)

  (require '[clojure.test :as t])

  (t/deftest "Simple"
    (let [cnt (count (all))
          msg {:user_name "nicola" :expr "(+ 1 1)" :result "1"}
          pmsg (create msg)
          pcnt (count (all))]
      (t/is (= pcnt (+ 1 cnt)))))

  (all)
  (create ))

(defn decode [data]
  (read-string data))

(defn encode [data]
  (pr-str data))

(defn repl-eval [expr]
  (try
    (with-out-str
      (println (eval (read-string expr))))
    (catch Exception e
      (with-out-str (trc/print-stack-trace e)))))

(decode (encode {:a 1}))

(def clients (atom #{}))

(defn b-cast [msg]
  (doseq [ch @clients]
    (ohs/send! ch msg)))

(defn- on-message [ch data]
  (println "receive " data)
  (let [msg (decode data)
        res (repl-eval (:expr msg))
        msg (assoc msg :result res)
        pmsg (db/create msg)]
    (println pmsg)
    (b-cast (encode [pmsg]))))


(defn on-connect [ch]
  (swap! clients conj ch)
  (ohs/send! ch (encode (db/all))))

(defn on-close [ch]
  (swap! clients disj ch))

(defn repl [req]
  (ohs/with-channel req ch
    (on-connect ch)
    (ohs/on-receive ch (fn [data] (on-message ch data)))
    (ohs/on-close ch (fn [_] (on-close ch)))))

(defonce socket (atom nil))

(def messages (atom []))

(defn next-id [] (str (gensym)))

(defn on-message [data]
  (let [msgs (reader/read-string data)]
    (.log js/console "Msg:" (pr-str msgs))
    (swap! messages (fn [ms] (into msgs ms)))))

(defn send-message [txt]
  (.send @socket txt))

(defn on-key-up [e]
    (when (= 13 (.-which e))
      (send-message (-> e .-target .-value))
      (aset (-> e .-target) "value" "")))

(def form (atom {}))

(defn bind [a attr]
  (fn on-change [ev]
    (swap! a (fn [f]
               (assoc f attr (-> ev .-target .-value))))))

(defn on-submit [ev]
  (.log js/console "submit" (pr-str  @form))
  (send-message (pr-str @form))
  (.preventDefault ev))

(defn home-page []
  [:div
   [:div.container
    [:p "print clj expr and press enter"]
    [:pre (pr-str @form)]
    [:form {:on-submit on-submit}
     [:input.form-control {:placeholder "name" :on-change (bind form :user_name)}]
     [:input.form-control {:on-change (bind form :expr)}]
     [:button {:type "submit"} "Submit"]]

    [:hr]
    (for [m @messages]
      [:div {:id (or (:id m) (next-id))}
       [:b (:user_name m)]
       [:pre.code (:expr m)]
       [:pre.result (:result m)]])]])

(defn init-socket []
  (let [l (.-location js/window)
        s (js/WebSocket. (str "ws://" (.-host l) "/repl"))]
    (reset! socket s)
    (.log js/console s)
    (aset s "onclose" init-socket)
    (aset s "onmessage" (fn [x] (.log js/console  "WS:" x)
                          (on-message (.-data x))))))
