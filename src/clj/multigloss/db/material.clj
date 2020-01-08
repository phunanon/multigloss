(ns multigloss.db.material
  (:require
    [multigloss.db.db :refer [db]]
    [multigloss.db.stats :as stats]))

(defn lang-material [lang-id]
  (second
    (stats/assoc-lang-stats
      lang-id (get-in @db [:languages lang-id]))))
