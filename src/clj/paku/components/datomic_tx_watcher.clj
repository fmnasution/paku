(ns paku.components.datomic-tx-watcher
  (:require
   [clojure.core.async :as async]
   [com.stuartsierra.component :as component]
   [datomic.api :as datomic]
   [taoensso.encore :as encore]))

(defrecord DatomicTxWatcher [datomic tx-report-ch tx-report-queue]
  component/Lifecycle
  (start [{:keys [datomic tx-report-ch tx-report-queue] :as this}]
    (if (some? tx-report-queue)
      this
      (let [conn (:conn datomic)
            tx-report-ch (or tx-report-ch (async/chan 128))
            tx-report-queue (datomic/tx-report-queue conn)]
        (future
          (loop []
            (let [tx-report (.take tx-report-queue)]
              (encore/catching (async/put! tx-report-ch tx-report))
              (recur))))
        (assoc this
               :tx-report-ch tx-report-ch
               :tx-report-queue tx-report-queue))))
  (stop [{:keys [tx-report-ch tx-report-queue] :as this}]
    (if (nil? tx-report-queue)
      this
      (do (async/close! tx-report-ch)
          (assoc this
                 :tx-report-ch nil
                 :tx-report-queue nil)))))

(defn new-datomic-tx-watcher
  ([tx-report-ch]
   (map->DatomicTxWatcher {:tx-report-ch tx-report-ch}))

  ([]
   (new-datomic-tx-watcher nil)))

