(ns multigloss.pages.lang
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [medley.core :refer [filter-keys filter-kv]]
    [clojure.string :as str]
    [multigloss.common :refer [md->div lang-info-tags]]))

(defn <flat-menu>
  [items nav-atom]
  [:ul.menu-list
    (for [item items]
      (let [is-selected (= @nav-atom item)]
        ^{:key item}
        [:li
          [(if is-selected :a.is-active :a)
            {:onClick #(reset! nav-atom item)}
            item]]))])

(defn <tree-menu>
  [tree nav-atom path? make-label & [prev-ids]]
  [:ul.menu-list
    (for [[id data] tree]
      (let [this-id     (concat prev-ids [id])
            is-nav-seq  (seq? @nav-atom)
            is-selected (and is-nav-seq
                             (= @nav-atom this-id))
            ;If this item is in the selected item's lineage
            is-child-selected
              (and is-nav-seq
                   (= (take (count this-id) @nav-atom) this-id))
            is-open (or is-selected is-child-selected)]
        ^{:key id}
        [:li
          [(if is-selected :a.is-active :a)
            {:onClick #(reset! nav-atom this-id)}
            (make-label this-id data)]
          (if is-open
            (let [paths (filter-kv path? data)]
              (when (seq paths)
                [<tree-menu> paths nav-atom path? make-label this-id])))]))])

(defn right-span [& body]
  (apply (partial vector :span {:style {:float "right"}}) body))

(defn <concept-label>
  [lang-id ids {:keys [name]}]
  [:span name
    (if @(rf/subscribe [:db :user])
      [right-span
        (let [completion @(rf/subscribe [:concept% lang-id ids])
              completion (* completion 100)]
           (str completion "%"))])])

;;TODO unused
(defn <lexicon-label>
  [lang-id ids item]
  [:span (first ids)
    (if @(rf/subscribe [:db :user])
      [right-span
        (let [completion @(rf/subscribe
               [:lexicon% lang-id (:type (first (vals item)))])
              completion (* completion 100)]
           (str completion "%"))])])

(defn <material-menu>
  [lang-id material nav]
  (let [concept-path? (fn [k v] (and (int? k) (:name v)))
        concepts      (filter-keys int? material)
        lexicon       (set (map :type (vals (:lexicon material))))
        concept-make-label
          (partial <concept-label> lang-id)
        make-%        #(int (* 100 @(rf/subscribe [% lang-id])))
        concepts%     (make-% :concept%)
        lexicon%      (make-% :lexicon%)]
    [:aside.menu.column.is-3
      [:ul.menu-list
        [:li
          [(if (= @nav :overview) :a.is-active :a)
            {:onClick #(reset! nav :overview)}
            "Overview"]]]
      [:p.menu-label "Concepts"
        [right-span concepts% "%"]]
      (<tree-menu> concepts nav concept-path? concept-make-label)
      [:p.menu-label "Lexicon"
        [right-span lexicon% "%"]]
      (<flat-menu> lexicon nav)]))

(defn <lexicon-view>
  [lexicon]
  [:table
    [:tbody
      [:tr [:th "Native"] [:th "Foreign"] [:th "Learned"]]
      (for [[id word] lexicon]
        (let [natives  (remove :d (:native word))
              foreigns (remove :d (:foreign word))
              natives  (map :w natives)
              foreigns (map :w foreigns)]
          ^{:key id}
          [:tr
            [:td (str/join ", " natives)]
            [:td (str/join ", " foreigns)]]))]])

(defn <material-view>
  [lang-id material nav]
  (let [is-lexicon  (string? nav)
        is-concept  (seq? nav)
        is-overview (= nav :overview)
        material
          (if is-lexicon
            (:lexicon material)
            (if is-concept
              (get-in material nav)
              material))]
    [:div.container.column.is-9>div.content
      (if-not is-overview
        [:a.button
          {:style {:float "right"}
           :href "/#/exercise"
           :onClick #(rf/dispatch [:exercise lang-id nav])}
          "Start exercise"])
      [:h3
        (if is-lexicon
          (str "Words of type: " nav)
          (if is-concept (:name material)))]
      (if is-lexicon
        [<lexicon-view>
          (filter (fn [[id {t :type}]] (= t nav)) material)]
        (md->div (:doc material)))]))

(defn <material-page>
  [lang-id {:keys [name] :as material}]
  [:section.section>div.container
    [:div.content
      [:h2 name]
      (lang-info-tags material)]
    (r/with-let [nav       (r/atom :overview)
                 prev-lang (r/atom nil)]
      (when-not (= lang-id @prev-lang) ;Catch switching between languages
        (reset! prev-lang lang-id)
        (reset! nav :overview))
      [:div.columns
        [<material-menu> lang-id material nav]
        [<material-view> lang-id material @nav]])])

(defn lang-page [{:keys [id]}]
  [:div
    (let [lang-id (js/parseInt id)
          material @(rf/subscribe [:db :material lang-id])]
      (when-not (:lexicon material)
                (rf/dispatch [:fetch-material lang-id]))
      [<material-page> lang-id material])])
