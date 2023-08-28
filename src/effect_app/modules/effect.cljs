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
 :efx/init
 (fn [args]
   (prn "Initializing Effect client with " args)
   (reset! client (Client.))
   (reset! eos/api-client (g/get @client "eos"))))

(reg-fx
 :efx/login
 (fn [hi]
   (prn @client)
   (prn "Logging in with " hi)
   (.call (g/get @client "login")
          @client
          "efxefxefxefx"
          "active"
          "5JSzMa3SXASiS14mZxsV36m4NdWKQCY9dctp3t5H1VhcVXUr4my")
   (prn (.-session @client))))
