(ns paku.components.sente
  (:require
   [clojure.core.async :as async]
   [com.stuartsierra.component :as component]
   [ring.util.http-response :as ring.response]
   [taoensso.sente :as sente]
   [taoensso.encore :as encore]
   [paku.components.bidi :as cmp.bd]
   [paku.components.channel-listener :as cmp.ch-lst]))

(defrecord WebsocketServer [ring-ajax-get
                            ring-ajax-post
                            recv-ch
                            send!
                            connected-uids
                            web-server-adapter
                            option
                            started?]
  component/Lifecycle
  (start [{:keys [web-server-adapter option started?] :as this}]
    (if started?
      this
      (let [{:keys [ajax-get-or-ws-handshake-fn
                    ajax-post-fn
                    connected-uids
                    ch-recv
                    send-fn]}
            (sente/make-channel-socket! web-server-adapter option)]
        (assoc this
               :ring-ajax-get ajax-get-or-ws-handshake-fn
               :ring-ajax-post ajax-post-fn
               :recv-ch ch-recv
               :send! send-fn
               :connected-uids connected-uids
               :started? true))))
  (stop [{:keys [recv-ch started?] :as this}]
    (if-not started?
      this
      (do (async/close! recv-ch)
          (assoc this :started? false))))
  cmp.ch-lst/Listenable
  (listenable-ch [this]
    (:recv-ch this)))

(defn new-websocket-server
  ([web-server-adapter option]
   (map->WebsocketServer {:web-server-adapter web-server-adapter
                          :option option
                          :started? false}))
  ([web-server-adapter]
   (new-websocket-server web-server-adapter {})))

(defn- ring-resource
  [websocket-server-key {:keys [request-method component] :as request}]
  (encore/cond
    :let [websocket-server (get component websocket-server-key)]

    (nil? websocket-server)
    (ring.response/service-unavailable)

    (= :get request-method)
    ((:ring-ajax-get websocket-server) request)

    (= :post request-method)
    ((:ring-ajax-post websocket-server) request)

    :else (ring.response/method-not-allowed)))

(defn new-sente-route-config
  [path websocket-server-key]
  (let [routes {path ::ring}
        resources {::ring (partial ring-resource websocket-server-key)}]
    (cmp.bd/new-route-config routes resources)))
