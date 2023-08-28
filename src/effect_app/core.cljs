(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [react-native :refer [Text Button View TextInput SafeAreaView Status StatusBar]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx sub]]
            [goog.object :as g]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]
            effect-app.modules.eos
            effect-app.modules.effect))


(defui button [{:keys [on-click children]}]
  ($ Button {:on-press on-click :title children}))

(reg-event-fx
 :success-load-campaigns
 (fn [{[_ campaigns] :event :keys [db]}]
   (let [ipfs-hash (get-in campaigns [0 :content :field_1])]
     {:db (assoc db :campaigns campaigns)})))

(reg-event-fx
 :click-load-campaigns
 (fn [{:keys [db]} _]
   {:db (update db :total-ticks inc)
    :eos/get-table-rows {:code "effecttasks2"
                         :table "campaign"
                         :scope "effecttasks2"
                         :on-success [:success-load-campaigns]}
    :efx/login {}}))

(defui campaign-box [{c :children}]
  (let [{:keys [description category title image]} (use-sub [:campaign-content c])]
    ($ Text  (str "*" title "*" (get-in c [:owner 0]) " x~~ " (get-in c [:owner 1])))))

(defui main-screen []
  (let [[value set-value!] (uix.core/use-state "demo text field")
        ;;[camps set-camps!] (uix.core/use-state [])
        total-ticks (use-sub [:total-ticks])
        campaigns (use-sub [:campaigns])]
    ($ View
       ($ TextInput {:value value
                     :on-change-text #(set-value! %)})
       ($ button {:on-click
                  #(do
                     (dispatch [:click-load-campaigns])
                     (prn "loading campaigns..."))}
          (str "load campaigns: " total-ticks))
       (for [c campaigns]
         ($ campaign-box {:key (:id c)} c)))))

(refx/reg-sub
  :total-ticks
  (fn [db _]
    (:total-ticks db)))

(refx/reg-sub
  :campaigns
  (fn [db _]
    (:campaigns db)))

(refx/reg-sub
  :campaigns
  (fn [db _]
    (:campaigns db)))

(refx/reg-sub
  :ipfs-object
  (fn [db [_ hash]]
    (get (:ipfs-objects db) hash)))

(refx/reg-sub
  :campaign-content
  (fn [[_ {:keys [content]}]] [(sub [:ipfs-object (:field_1 content)])])
  (fn [[content] campaign]
    content))

(reg-event-fx
 :app-load
 (fn [{:keys [db]} _]
   {:db {:current-path "/home"
         :total-ticks 0
         :ipfs-objects {}}
    :efx/init "jungle4"}))

(reg-event-fx
 :success-ipfs-result
 (fn [{[_ ipfs-hash res] :event :keys [original-event db]}]
   {:db (assoc-in db [:ipfs-objects ipfs-hash] res)}))

(reg-event-fx
 :failure-ipfs-result
 (fn [{[_ res] :event :keys [original-event db]}]
   (prn  (count (:result res)))
   {}))

(defn ^:export -main [& args]
  (refx/dispatch-sync [:app-load])
  ($ SafeAreaView
     ($ StatusBar {:background-color "#F0F0F0"
                   :bar-style "dark-content"})
     ($ View {:style {:padding 10}}
        ($ Text {:style {:font-family "Inter-Regular"
                         :font-size 24}}
           "This is a header"))
     ($ main-screen)))
