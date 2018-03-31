(ns paku.system
  (:require
   [com.stuartsierra.component :as component]))

(defmacro defsystem
  [system-name args & components]
  `(defn ~system-name
     ~args
     (component/system-map ~@components)))
