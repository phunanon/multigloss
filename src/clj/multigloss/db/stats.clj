(ns multigloss.db.stats
  (:require
    [multigloss.db.db :refer [db]]))

(defn lang-num-learn [lang-id]
  (let [enrolments (map :enrolled (vals (:users @db)))
        enrolments (filter #(% lang-id) enrolments)]
    (count enrolments)))

(defn lang-attr-count [lang-id attr]
  (count (get-in @db [:languages lang-id attr])))

(defn- lang-stats [id]
  {:num-learn   (lang-num-learn id)
   :num-words   (lang-attr-count id :lexicon)
   :num-authors (lang-attr-count id :authors)})

(defn assoc-lang-stats
  [id material]
  [id (into material (lang-stats id))])
