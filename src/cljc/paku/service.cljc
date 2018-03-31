(ns paku.service)

(defprotocol Service
  (prepare [service]))

(defn prepare-attached
  [m ks]
  (reduce (fn [m k]
            (let [v (get m k)]
              (if (satisfies? Service v)
                (update m k prepare)
                m)))
          m
          ks))
