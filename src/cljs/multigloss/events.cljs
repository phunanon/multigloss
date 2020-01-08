(ns multigloss.events
  (:require
    [re-frame.core :as rf]
    [ajax.edn :as edn]
    [medley.core :refer [filter-keys filter-vals]]))

; Utils

(defn deep-merge [v & vs]
  (letfn [(rec-merge
            [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (when (some identity vs)
      (reduce #(rec-merge %1 %2) v vs))))

;;dispatchers

(rf/reg-event-db
  :navigate
  (fn [db [_ route]]
    (assoc db :route route)))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-db
  :dissoc
  (fn [db [_ key]]
    (dissoc db key)))

(rf/reg-event-db
  :db
  (fn [db [_ & keys-val]]
    (assoc-in db (butlast keys-val) (last keys-val))))

(rf/reg-event-db
  :into-db
  (fn [db [_ hmap]]
    (into db hmap)))

; -- HTTP stuff --

(rf/reg-event-fx
  :http-ok
  (fn [{db :db} [_ callback result]]
    (let [db (deep-merge db result)
          db (assoc db :http-wait false)
          fx {:db db}
          fx (if callback (assoc fx :dispatch callback) fx)]
    fx)))

(rf/reg-event-db
  :http-bad
  (fn [db _]
    (js/setTimeout #(rf/dispatch [:dissoc :http-bad]) 2000)
    (into db {:http-bad true :http-wait false})))

(rf/reg-event-fx
  :http
  (fn [{:keys [db]} [_ & [uri params callback]]]
    {:db         (assoc db :http-wait true)
     :http-xhrio
       {:method          "post"
        :uri             uri
        :on-success      [:http-ok callback]
        :on-failure      [:http-bad]
        :params          (assoc params :__anti-forgery-token js/csrfToken)
        ;:headers         {"X-CSRF-Token" js/csrfToken}
        :format          (edn/edn-request-format)
        :response-format (edn/edn-response-format)}}))

; -- signin --

(rf/reg-event-fx
  :sign-or-reg
  (fn [_ [_ endpoint form]]
    {:dispatch [:http endpoint form [:after-signin]]}))

(rf/reg-event-db
  :after-signin
  (fn [db _]
    (if (:user db)
      (dissoc db :signin)
      db)))

(rf/reg-event-fx
  :signout
  (fn [{db :db} _]
    {:db       (dissoc db :user)
     :dispatch-n [[:http "/signout" nil]
                  [:dissoc :learning]]}))

; -- lang --

(rf/reg-event-fx
  :enrol
  (fn [_ [_ lang-id]]
    {:dispatch [:http "/enrol" {:lang-id lang-id}]}))

(rf/reg-event-fx
  :fetch-material
  (fn [{db :db} [_ lang-id]]
    {:dispatch [:http "/material" {:lang-id lang-id}]}))

; -- exercise --

(rf/reg-event-fx
  :exercise
  (fn [{db :db} [_ lang-id nav]]
    {:db (assoc db :exercise {:id lang-id :nav nav})}))


;;subscriptions

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-sub
  :db
  (fn [db [_ & keys]]
    (get-in db keys)))

; -- lists --

(rf/reg-sub
  :enrolled-languages
  (fn [db _]
    (let [enrolled (-> db :user :enrolled)
          enrolled (or enrolled #{})]
      (filter-keys enrolled (:material db)))))

(rf/reg-sub
  :lexicon-of-type
  (fn [db [_ lang-id type]]
    (let [lexicon (get-in db [:material lang-id :lexicon])]
      (filter-vals #(= (:type %) type) lexicon))))

; -- stats --

(defn num-leaves
  ([tree] (num-leaves tree identity))
  ([tree count?] (num-leaves 0 tree count?))
  ([n tree count?]
    (reduce
      (fn [n [k v :as m]]
        (if (map? v)
          (num-leaves n v count?)
          (if (count? m) (inc n) n)))
      n tree)))

(rf/reg-sub
  :concept%
  (fn [db [_ & [lang-id ids]]]
    (let [mem       (get-in db (concat [:user :memory lang-id :concepts] ids))
          num-mem   (num-leaves mem)
          material  (get-in db (concat [:material lang-id] ids))
          num-to-do (num-leaves material #(= :native (first (vec %))))]
      (if (zero? num-mem)
          0
          (/ num-mem num-to-do)))))

;;TODO
(rf/reg-sub
  :lexicon%
  (fn [db [_ & [lang-id type]]]
    (let [mem (get-in db (concat [:user :memory lang-id :lexicon]))]
(println mem)
1)))
