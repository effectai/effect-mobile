(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [react-native :refer [Text Button View TextInput SafeAreaView]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub]]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]))

(defui button [{:keys [on-click children]}]
  ($ Button {:on-press on-click :title children}))

(def eos
  (-> (APIClient. #js {:provider (FetchProvider. "https://eos.greymass.com")})
      (goog.object/get "v1")
      (goog.object/get "chain")))

(defui main-screen []
  (let [[value set-value!] (uix.core/use-state "demo text field")
        [camps set-camps!] (uix.core/use-state [])
        total-ticks (use-sub [:total-ticks])]
    ($ View
       ($ TextInput {:value value
                     :on-change-text #(set-value! %)})
       ($ button {:on-click
                  #(do
                     (dispatch [:click-load-campaigns])
                     (prn "loading campaigns...")
                     (.then
                      (.get_table_rows eos
                                       #js {:code "force.efx"
                                            :table "campaign"
                                            :scope "force.efx"})
                      (fn [res]
                        (let [campaigns (:rows (js->clj res
                                                        :keywordize-keys true))]
                          (set-camps! campaigns)))))}
          (str "load campaigns: " total-ticks))
       (for [c camps]
         ($ Text (str (get-in c [:owner 0]) " - " (get-in c [:owner 1])))))))

(refx/reg-sub
  :total-ticks
  (fn [db _]
    (:total-ticks db)))

(reg-event-fx
 :launch-app
 (fn [{:keys [db]} _]
   {:db {:current-path "/home"
         :total-ticks 0}}))

(reg-event-fx
 :click-load-campaigns
 (fn [{:keys [db]} _]
   {:db (update db :total-ticks inc)}))

(defn ^:export -main [& args]
  (refx/dispatch-sync [:launch-app])
  ($ SafeAreaView
     ($ Text "bare bones demo of effect network native app")
     ($ main-screen)))
