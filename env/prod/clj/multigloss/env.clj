(ns multigloss.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[multigloss started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[multigloss has shut down successfully]=-"))
   :middleware identity})
