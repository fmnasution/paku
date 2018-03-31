(ns paku.components.datascript
  #?(:clj
     (:require
      [clojure.core.async :as async]
      [com.stuartsierra.component :as component]
      [datascript.core :as datascript])
     :cljs
     (:require
      [cljs.core.async :as async]
      [com.stuartsierra.component :as component]
      [datascript.core :as datascript])))

(defrecord Datascript [schema conn]
  component/Lifecycle
  (start [{:keys [schema conn] :as this}]
    (if (some? conn)
      this
      (let [conn (if (map? schema)
                   (datascript/create-conn schema)
                   (datascript/create-conn))]
        (assoc this :conn conn))))
  (stop [{:keys [conn] :as this}]
    (if (nil? conn)
      this
      (assoc this :conn nil))))

(defn new-datascript
  ([schema]
   (map->Datascript {:schema schema}))
  ([]
   (new-datascript nil)))

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
                 :started? false)))))

(defn new-datascript-tx-watcher
  ([tx-report-ch]
   (map->DatascriptTxWatcher {:tx-report-ch tx-report-ch
                              :started? false}))
  ([]
   (new-datascript-tx-watcher nil)))
