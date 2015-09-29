(ns jug.prod
  (:require [jug.core-ready :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
