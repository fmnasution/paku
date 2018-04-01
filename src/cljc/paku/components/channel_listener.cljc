(ns paku.components.channel-listener
  #?(:clj
     (:require
      [clojure.core.async :as async :refer [go-loop]]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as timbre]
      [taoensso.encore :as encore])
     :cljs
     (:require
      [cljs.core.async :as async]
      [com.stuartsierra.component :as component]
      [taoensso.timbre :as timbre :include-macros true]
      [taoensso.encore :as encore :include-macros true]))
  #?(:cljs
     (:require-macros
      [cljs.core.async.macros :refer [go-loop]])))

(defprotocol Listenable
  (listenable-ch [listenable]))

(defn- listen!
  [{:keys [target callback error-callback] :as this}]
  (let [stop-ch (async/chan)
        channel (listenable-ch target)
        chs (conj [channel] stop-ch)
        stopper (fn stop!
                  []
                  (async/close! stop-ch))]
    (go-loop []
      (let [[item ch] (async/alts! chs :priority true)
            stop? (or (= stop-ch ch) (nil? item))]
        (when-not stop?
          (encore/catching
           (callback this item)
           error1
           (encore/catching
            (if (some? error-callback)
              (error-callback this item error1)
              (timbre/error error1 "`callback` error:"))
            error2
            (timbre/error error2 "`error-callback` error:")))
          (recur))))
    stopper))

(defrecord ChannelListener [target callback error-callback stopper]
  component/Lifecycle
  (start [{:keys [stopper] :as this}]
    (if (some? stopper)
      this
      (let [stopper (listen! this)]
        (assoc this :stopper stopper))))
  (stop [{:keys [stopper] :as this}]
    (if (nil? stopper)
      this
      (do (stopper)
          (assoc this :stopper nil)))))

(defn new-channel-listener
  ([callback error-callback]
   (map->ChannelListener {:callback callback
                          :error-callback error-callback}))
  ([callback]
   (new-channel-listener callback nil)))
