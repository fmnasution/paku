(ns paku.components.sente
  (:require
   [cljs.core.async :as async]
   [com.stuartsierra.component :as component]
   [taoensso.sente :as sente]
   [paku.components.channel-listener :as cmp.ch-lst]))

(defrecord WebsocketClient [chsk
                            recv-ch
                            send!
                            state
                            option
                            path
                            started?]
  component/Lifecycle
  (start [{:keys [path option started?] :as this}]
    (if started?
      this
      (let [{:keys [chsk ch-recv send-fn state]}
            (sente/make-channel-socket! path option)]
        (assoc this
               :chsk chsk
               :recv-ch ch-recv
               :send! send-fn
               :state state
               :started? true))))
  (stop [{:keys [recv-ch chsk started?] :as this}]
    (if-not started?
      this
      (do (async/close! recv-ch)
          (sente/chsk-disconnect! chsk)
          (assoc this :started? false))))
  cmp.ch-lst/Listenable
  (listenable-ch [this]
    (:recv-ch this)))

(defn new-websocket-client
  ([path option]
   (map->WebsocketClient {:path path
                          :option option
                          :started? false}))
  ([path]
   (new-websocket-client path {})))
