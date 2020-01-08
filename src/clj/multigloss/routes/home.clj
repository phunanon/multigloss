(ns multigloss.routes.home
  (:require
    [multigloss.layout :as layout]
    [multigloss.db.adapter :as db]
    [clojure.java.io :as io]
    [multigloss.middleware :as middleware]
    [ring.util.http-response :as resp]
    ;[ring.middleware.json :refer [wrap-json-response]]
    [ring.middleware.format :refer [wrap-restful-format]]
    [ring.middleware.gzip :refer [wrap-gzip]]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn handle-init
  [{{:keys [user-id]} :session}]
  (resp/ok {:home-md (-> "docs/home.md" io/resource slurp)
            :material (db/all-langs user-id)
            :user (db/search-user :id user-id)}))

(defn handle-signin
  [{{:strs [email pass]} :params
    session              :session :as req}]
  (let [{:keys [id] :as user-result}
                 (db/email+pass->user email pass)
        response (resp/ok {(if id :user :signin) user-result})]
    (->
      (if-not (#{:bad-email :bad-pass} user-result)
        (assoc response :session (into session {:user-id id}))
        response)
      (assoc-in [:body :material] (db/all-langs id)))))

(defn handle-signout
  [{:keys [session]}]
  (-> (resp/ok {:material (db/all-langs)
                :session   (dissoc session :user-id)})))

(defn handle-register
  [{{:keys [email pass]} :params}]
  (let [new-id (db/new-user! email pass)]
    (if new-id
      (handle-signin
        {:params  {:email email :pass pass}}))))

(defn handle-enrol
  [{{:keys [lang-id]} :params
    {:keys [user-id]} :session}]
  (db/lang-enrol! user-id lang-id)
  (resp/ok {:material (db/all-langs user-id)}))

(defn handle-material
  [{{:keys [lang-id]} :params}]
  (let [lang-id (Integer. lang-id)]
    (resp/ok
      {:material
        {lang-id
          (db/lang-material lang-id)}})))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 #(wrap-restful-format % :formats [:edn]) ;wrap-json-response
                 wrap-gzip]}
   ["/"         {:get  home-page}]
   ["/init"     {:post handle-init}]
   ["/signin"   {:post handle-signin}]
   ["/signout"  {:post handle-signout}]
   ["/register" {:post handle-register}]
   ["/enrol"    {:post handle-enrol}]
   ["/material" {:post handle-material}]
   ["/docs" {:get (fn [_]
                    (-> (resp/ok (-> "docs/docs.md" io/resource slurp))
                        (resp/header "Content-Type" "text/plain; charset=utf-8")))}]])

