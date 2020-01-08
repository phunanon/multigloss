(ns multigloss.db.db
  (:require
    [multigloss.db.init :as init]
    [duratom.core :refer [duratom]]))

(def db
  (duratom
    :local-file
    :file-path "multigloss-db.edn"
    :init init/schema))
