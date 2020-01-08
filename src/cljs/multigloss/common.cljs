(ns multigloss.common
  (:require
    [markdown.core :refer [md->html]]))

(def content-div :section.section>div.container>div.content)

(defn md->div [content]
  [:div {:dangerouslySetInnerHTML {:__html (md->html content)}}])

(defn lang-info-tags
  [{:keys [tags num-learn num-words num-authors]}]
  [:div.tags
    [:span.tag.is-info num-learn " learners"]
    [:span.tag.is-info num-words " words"]
    [:span.tag.is-info num-authors " authors"]
    (for [text tags]
      ^{:key text}
      [:span.tag.is-primary text])])
