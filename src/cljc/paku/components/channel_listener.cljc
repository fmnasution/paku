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

(defrecord ChannelListener [target
                            callback
                            error-callback
                            pre-start
                            post-start
                            pre-stop
                            post-stop
                            stopper]
  component/Lifecycle
  (start [{:keys [pre-start post-start stopper] :as this}]
    (if (some? stopper)
      this
      (let [pre-start (or pre-start identity)
            post-start (or post-start identity)
            this (pre-start this)
            stopper (listen! this)]
        (post-start (assoc this :stopper stopper)))))
  (stop [{:keys [pre-stop post-stop stopper] :as this}]
    (if (nil? stopper)
      this
      (let [pre-stop (or pre-stop identity)
            post-stop (or post-stop identity)
            this (pre-stop this)]
        (stopper)
        (post-stop (assoc this :stopper nil))))))

(defn new-channel-listener
  ([callback option]
   (map->ChannelListener (assoc option :callback callback)))
  ([callback]
   (new-channel-listener callback nil)))
