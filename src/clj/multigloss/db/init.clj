(ns multigloss.db.init)

(def schema
{
  :languages {
    0 {
      :name "wqle"
      :tags ["englang"]
      :authors #{0}
      :doc "my conlang, *mate*"
      0 {
        :name "Nouns"
        :doc  "nouns, *mate*"
        0 {
          :name "identify the noun"
          :doc  "This exercise will help you identify nouns."
          0 {
            :native  [{:w "wqle ai"}]
            :foreign [{:w "past speech"}]}
          1 {
            :native  [{:w "whatever, man"}]
            :foreign [{:w "aye"}]}}
        1 {
          :name "identify the pronouns"
          :doc  "This exercise will help you identify pronouns."
          0 {
            :native  [{:w "wqle bitch"}]
            :foreign [{:w "bitch speech"}]}
          1 {
            :native  [{:w "whatever, man"}]
            :foreign [{:w "aye"}]}}}
       1 {:name "Verbs"
          :doc  "**verbs**, darling"
          0 {
            :name "identify the verb"
            :doc  "md"
            :sentences
              {0 {:native  [{:w "lqi"}]
                  :foreign [{:w "speak"}]}}}}
      :lexicon {
        0 {:type    "noun"
           :native  [{:w "wqle"}]
           :foreign [{:w "speech"} {:w "amazing" :d true}]}}}
    1 {
      :name "Cet"
      :tags ["conlang"]
      :authors #{0}
      :doc "It's very German."
      0 {
        :name "Grammar"
        :doc "Mmmm"
        0 {
          :name "Mood"
          :doc "*Indicative*  
| desti [CET] - I eat [ENG]  
> factual statement, only realis mood in Cet."}}
      :lexicon {
        0 {:type "noun"
           :native  [{:w "hyl"} {:w "montja"} {:w "monta" :d true}]
           :foreign [{:w "hill"}]}
        1 {:type "noun"
           :native  [{:w "samgensev"}]
           :foreign [{:w "homosexual"}]}
        2 {:type "noun"
           :native  [{:w "blikspel"}]
           :foreign [{:w "video game"}]}
        3 {:type "noun"
           :native  [{:w "flask"}]
           :foreign [{:w "bottle"}]}
        4 {:type "noun"
           :native  [{:w "windok"}]
           :foreign [{:w "window"}]}
        5 {:type "noun"
           :native  [{:w "hert"} {:w "blodpump"}]
           :foreign [{:w "heart"}]}
        6 {:type "noun"
           :native  [{:w "bóm"}]
           :foreign [{:w "tree"}]}
        7 {:type "noun"
           :native  [{:w "bómja"}]
           :foreign [{:w "sapling"}]}
        8 {:type "noun"
           :native  [{:w "ðisdag"} {:w "ðd"}]
           :foreign [{:w "today"}]}
        9 {:type "noun"
           :native  [{:w "klag"}]
           :foreign [{:w "complaint"} {:w "protest"}]}
        10 {:type "noun"
           :native  [{:w "folklag"}]
           :foreign [{:w "protest"}]}
        11 {:type "noun"
           :native  [{:w "kind"}]
           :foreign [{:w "child"} {:w "kid"}]}
        12 {:type "noun"
           :native  [{:w "glad"}]
           :foreign [{:w "glad"} {:w "happy"}]}
        13 {:type "capital"
           :native  [{:w "Moskva"}]
           :foreign [{:w "Moscow"}]}
        14 {:type "country"
           :native  [{:w "Rosí"} {:w "Rosí(a)"}]
           :foreign [{:w "Russia"}]}
         }}}
    :next-user-id 1
    :users
      {0 {:id       0
          :email    "phunanon@gmail.com"
          :name     "Patrick"
          :enrolled #{0}
          :memory   {0 {:concepts {0 {0 {0 [0 0]}}} :lexicon {}}
                     1 {:concepts {} :lexicon {0 [0 0] 1 [0 0] 2 [0 0]}}}
          :pass     "37a8eec1ce19687d132fe29051dca629d164e2c4958ba141d5f4133a33f0688f"
          :salt     ""}
       1 {:id       1
          :email    "aqua@gmail.com"
          :name     "Aqua"
          :enrolled #{0 1}
          :memory   {}
          :pass     "37a8eec1ce19687d132fe29051dca629d164e2c4958ba141d5f4133a33f0688f"
          :salt     ""}}})
