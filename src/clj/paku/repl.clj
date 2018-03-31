(ns paku.repl
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]
   [com.stuartsierra.component :as component]))

(def system
  nil)

(def system-fn
  nil)

(defn set-init!
  [f]
  (alter-var-root #'system-fn (constantly f)))

(defn init!
  []
  (alter-var-root #'system (fn [_]
                             (system-fn))))

(defn boot!
  []
  (init!)
  (alter-var-root #'system component/start)
  :ok)

(defn shutdown!
  []
  (alter-var-root #'system (fn [system]
                             (when (some? system)
                               (component/stop system))))
  :ok)

(defn reboot!
  []
  (shutdown!)
  (refresh :after `paku.repl/boot!))
