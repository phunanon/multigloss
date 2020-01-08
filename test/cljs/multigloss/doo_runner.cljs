(ns multigloss.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [multigloss.core-test]))

(doo-tests 'multigloss.core-test)

