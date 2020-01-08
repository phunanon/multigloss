(ns multigloss.db.lists
  (:require
    [multigloss.db.db :refer [db]]
    [multigloss.db.stats :as stats]
    [medley.core :refer [map-kv]]))

(defn all-enrolled [user-id]
  (get-in @db [:users user-id :enrolled]))

(defn assoc-enrolment
  [user-enrolled {:keys [id] :as lang}]
  (into lang {:enrolled (user-enrolled id)}))

(defn- simplify-lang-info
  [id material]
  [id (select-keys material [:name :tags])])

(defn all-langs [& [user-id]]
  (->> (:languages @db)
    (map-kv simplify-lang-info)
    (map-kv stats/assoc-lang-stats)))
