(ns multigloss.core
  (:require
    [multigloss.common :refer [md->div lang-info-tags content-div]]
    [multigloss.pages.lang :as lang]
    [multigloss.pages.exercise :as exer]
    [multigloss.ajax :as ajax] ;;;;
    [multigloss.events :as events] ;;;;
    [day8.re-frame.http-fx] ;;;;
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as goog-events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [reitit.core :as reitit]
    [clojure.string :as str])
  (:import goog.History))

(defn nav-link [is-open uri title page]
  [:a.navbar-item
   {:href    uri
    :class   (when (= page @(rf/subscribe [:page])) :is-active)
    :onClick #(reset! is-open false)}
   title])

(defn navbar []
  (r/with-let [is-open  (r/atom false)
               nav-link (partial nav-link is-open)]
    [:nav.navbar
      {:class (if @(rf/subscribe [:db :http-bad]) "is-danger" "is-info")
       :style {:transition "all .05s ease-in-out"}}
      [:div.container
        [:div.navbar-brand
          [nav-link "#/" "Multigloss" :home]
          [:progress.progress.is-small.is-primary
            {:style
              {:margin "auto"
               :width "5rem"
               :opacity (if @(rf/subscribe [:db :http-wait]) 1 0)}}]
          [:span.navbar-burger.burger
            {:data-target :nav-menu
             :on-click #(swap! is-open not)
             :class (when @is-open :is-active)}
            [:span][:span][:span]]]
        [:div#nav-menu.navbar-menu
          {:class (when @is-open :is-active)}
          [:div.navbar-start
            [:div.navbar-item.has-dropdown.is-hoverable
              [:a.navbar-link "Learn"]
              (let [enrolled @(rf/subscribe [:enrolled-languages])]
                [:div.navbar-dropdown
                  [nav-link "#/langs" "All languages" :langs]
                  (if (seq enrolled) [:hr {:class "navbar-divider"}])
                  (for [[id {:keys [name] :as lang-info}]
                          enrolled]
                    ^{:key id}
                    [:a.navbar-item
                      {:href (str "#/lang/" id)
                       :onClick #(reset! is-open false)}
                       name])])]
            [nav-link "#/teach" "Teach" :teach]
            (let [{:keys [name]} @(rf/subscribe [:db :user])
                  name (if name name "Profile")]
              [nav-link "#/profile" name :profile])]]]]))



(defn get-form-data [el-name]
  (->> el-name
    (.querySelector js/document)
    (js/FormData.)
    (.fromEntries js/Object)
    (js->clj)))

(defn home-page []
  [content-div
   (when-let [home-md @(rf/subscribe [:db :home-md])]
     (md->div home-md))])





(defn langs-page []
  (let [user     @(rf/subscribe [:db :user])
        enrolled @(rf/subscribe [:db :user :enrolled])]
    [content-div
      (for [[id {:keys [name] :as lang-info}]
              @(rf/subscribe [:db :material])]
        (let [is-enrolled (and user (enrolled id))]
          ^{:key id}
          [:div.list
            [:div.list-item.has-background-light
              (if user
                (if is-enrolled
                  [:span
                    [:span.button
                      {:style {:float "right" :top "1rem"}
                       :disabled true}
                      "enrolled"]
                    [:a.button
                      {:style {:float "right" :top "1rem"}
                       :href (str "/#/lang/" id)}
                      "learn"]]
                  [:button.button.is-link
                    {:style   {:right "1rem" :top "1rem" :position "absolute"}
                     :onClick #(rf/dispatch [:enrol id])}
                    "enrol"]))
              [:h4 name]
              (lang-info-tags lang-info)]]))]))

(defn teach-page []
  [content-div
   "nothing"])

(defn profile-page []
  (let [status       @(rf/subscribe [:db :signin])
        {:keys [name] :as user}
                     @(rf/subscribe [:db :user])
        form-data    #(get-form-data "form#signin")
        sign-or-reg! #(rf/dispatch [:sign-or-reg % (form-data)])
        signin!      #(sign-or-reg! "/signin")
        register!    #(sign-or-reg! "/register")
        signout!     #(rf/dispatch [:signout])]
    [:section.section>div.container>div.content
      (if (or (#{"bad-email" "bad-pass"} user)
              (nil? user))
        [:div
          (if status [:p.notification.is-warning status])
          [:form#signin
            {:method "post" :onSubmit (fn [e] (.preventDefault e))}
            [:input.input {:name "email" :placeholder "email"
                           :required true}] [:br]
            [:input.input {:name "pass" :placeholder "password" :type "password"
                          :required true}]
            [:br] [:br]
            [:button.button {:type "button" :onClick signin!} "sign in"]
            [:button.button {:type "button" :onClick register!} "register"]]]
        [:div
          [:div.tabs
            [:ul
              [:li [:a "Stuff"]]
              [:li [:a "Things"]]]]
          [:p [:b "Hello, " name]]
          [:button.button {:onClick signout!} "sign out"]])]))



; -- pages -- ;

(def pages
  {:home     #'home-page
   :langs    #'langs-page
   :lang     lang/lang-page
   :exercise exer/exercise-page
   :teach    #'teach-page
   :profile  #'profile-page})

(defn page []
  [:div
    [navbar]
    (let [route   @(rf/subscribe [:route])
          handler (-> route :data :name pages)
          params  (-> route :path-params)]
      [handler params])])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/"         :home]
     ["/langs"    :langs]
     ["/lang/:id" {:name :lang :parameters {:path {:id int?}}}]
     ["/exercise" :exercise]
     ["/teach"    :teach]
     ["/profile"  :profile]]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (goog-events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (let [uri (or (not-empty (str/replace (.-token event) #"^.*#" "")) "/")]
          (rf/dispatch
            [:navigate (reitit/match-by-path router uri)]))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:navigate (reitit/match-by-name router :home)])
  (ajax/load-interceptors!)
  (rf/dispatch [:http "/init"])
  (hook-browser-navigation!)
  (mount-components))
