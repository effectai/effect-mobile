(ns effect-app.modules.effect
  (:require [refx.alpha :as refx :refer [reg-event-fx reg-fx]]
            [goog.object :as g]
            ["@effectai/effect-js" :refer [Client]]
            [effect-app.modules.eos :as eos]))

(def client (atom nil))

;; (def vaccount (g/get effect "vaccount"))

#_(.catch
 (.then (.call (g/get vaccount "vtransfer") vaccount 12 4 "0.0024 EFX")
        #(prn "Succie " %))
 #(prn "ERR " %))



#_(.catch
 (.then (.getCampaigns effect.tasks)
        #(prn "118181818" %))
 #(prn "errororrrro!" %))


;; (prn "--" (.-session effect))
(reg-fx
 ::init
 (fn [args]
   (prn "Initializing Effect client with " args)
   (reset! client (Client.))
   (reset! eos/api-client (g/get @client "eos"))))

(reg-fx
 ::login
 (fn [{:keys [actor permission key]}]
   (prn @client)
   (prn "Logging in with " hi)
   (.call (g/get @client "login")
          @client
          actor
          permission
          key)
   (prn (.-session @client))))
