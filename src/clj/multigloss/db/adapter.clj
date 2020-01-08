(ns multigloss.db.adapter
  (:require
    [multigloss.db.account  :as account]
    [multigloss.db.material :as material]
    [multigloss.db.lists    :as lists]
    [multigloss.db.stats    :as stats]))

(def all-langs        lists/all-langs)
(def search-user      account/search-user)
(def email+pass->user account/email+pass->user)
(def new-user!        account/new-user!)
(def lang-enrol!      account/lang-enrol!)
(def lang-material    material/lang-material)
