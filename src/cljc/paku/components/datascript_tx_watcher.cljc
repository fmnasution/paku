(ns paku.components.datascript-tx-watcher
  #?(:clj
     (:require
      [clojure.core.async :as async]
      [com.stuartsierra.component :as component]
      [datascript.core :as datascript]
      [paku.components.channel-listener :as cmp.ch-lst])
     :cljs
     (:require
      [cljs.core.async :as async]
      [com.stuartsierra.component :as component]
      [datascript.core :as datascript]
      [paku.components.channel-listener :as cmp.ch-lst])))

(defrecord DatascriptTxWatcher [datascript tx-report-ch started?]
  component/Lifecycle
  (start [{:keys [datascript tx-report-ch started?] :as this}]
    (if started?
      this
      (let [conn (:conn datascript)
            tx-report-ch (or tx-report-ch (async/chan 128))]
        (datascript/listen! conn ::tx-report (partial async/put! tx-report-ch))
        (assoc this
               :tx-report-ch tx-report-ch
               :started? true))))
  (stop [{:keys [tx-report-ch started?] :as this}]
    (if-not started?
      this
      (do (async/close! tx-report-ch)
          (assoc this
                 :tx-report-ch nil
                 :started? false))))
  cmp.ch-lst/Listenable
  (listenable-ch [this]
    (:tx-report-ch this)))

(defn new-datascript-tx-watcher
  ([tx-report-ch]
   (map->DatascriptTxWatcher {:tx-report-ch tx-report-ch
                              :started? false}))
  ([]
   (new-datascript-tx-watcher nil)))

