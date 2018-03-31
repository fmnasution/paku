(ns paku.components.datomic
  (:require
   [com.stuartsierra.component :as component]
   [datomic.api :as datomic]))

(defrecord Datomic [uri conn]
  component/Lifecycle
  (start [{:keys [uri conn] :as this}]
    (if (some? conn)
      this
      (let [created? (datomic/create-database uri)
            conn (datomic/connect uri)]
        (assoc this :conn conn))))
  (stop [{:keys [conn] :as this}]
    (if (nil? conn)
      this
      (do (datomic/release conn)
          (assoc this :conn nil)))))

(defn new-datomic
  [uri]
  (map->Datomic {:uri uri}))
