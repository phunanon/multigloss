(ns multigloss.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [multigloss.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[multigloss started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[multigloss has shut down successfully]=-"))
   :middleware wrap-dev})
