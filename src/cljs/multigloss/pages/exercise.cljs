(ns multigloss.pages.exercise
  (:require
    [multigloss.common :refer [content-div]]
    [re-frame.core :as rf]))

(defn <exerciser> [material memory point memorise! correct?]
  (let [next-point (get material @point)]
    (str @point next-point memory)))

(defn <lex-exercise> [lang-id type]
  [content-div
    [:h2 "Lexicon exercise: " type]
    (let [lexicon   @(rf/subscribe [:lexicon-of-type lang-id type])
          memory    @(rf/subscribe [:db :user :memory lang-id :lexicon])
          point     (rf/subscribe [:db :exercise :point])
          memorise! #(rf/dispatch [:memorise lang-id %])]
      ;init point
      (if-not @point
        (rf/dispatch [:db :exercise :point (first (keys lexicon))]))
      ;display
      [<exerciser> lexicon memory point memorise! identity])])

(defn exercise-page []
  (let [{:keys [id nav]}
               @(rf/subscribe [:db :exercise])
        is-lex (string? nav)]
    (if is-lex
      [<lex-exercise> id nav]
      "Nothing.")))
