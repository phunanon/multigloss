(ns multigloss.app
  (:require [multigloss.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
