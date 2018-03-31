(ns paku.components.rum
  (:require
   [goog.dom :as gdom]
   [com.stuartsierra.component :as component]
   [rum.core :as rum]
   [paku.service :as srv]))

(defrecord Element [id rum-element node]
  component/Lifecycle
  (start [{:keys [id rum-element node] :as this}]
    (if (some? node)
      this
      (let [node (gdom/getRequiredElement id)
            prepared-this (srv/prepare this)]
        (rum/mount (rum-element prepared-this) node)
        (assoc this :node node))))
  (stop [{:keys [node] :as this}]
    (rum/unmount node)
    (assoc this :node nil))
  srv/Service
  (prepare [this]
    (srv/prepare-attached this)))

(defn new-element
  [id rum-element]
  (map->Element {:id id
                 :rum-element rum-element}))

(defn remount!
  [{:keys [rum-element node] :as element}]
  (let [prepared-this (srv/prepare element)]
    (rum/mount (rum-element prepared-this) node)))
