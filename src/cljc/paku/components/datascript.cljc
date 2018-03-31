(ns paku.components.datascript
  #?(:clj
     (:require
      [com.stuartsierra.component :as component]
      [datascript.core :as datascript])
     :cljs
     (:require
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
