(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [react-native :refer [Text Button View TextInput SafeAreaView]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx sub]]
            [goog.object :as g]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]
            [refx.http]
            [ajax.core :as ajax]))

(def eos
  (-> (APIClient. #js {:provider (FetchProvider. "https://eos.greymass.com")})
      (goog.object/get "v1")
      (goog.object/get "chain")))

(defui button [{:keys [on-click children]}]
  ($ Button {:on-press on-click :title children}))

(reg-event-fx
 :success-load-campaigns
 (fn [{[_ campaigns] :event :keys [db]}]
   (let [ipfs-hash (get-in campaigns [0 :content :field_1])]
     ;; if ipfs-hash not in cash, load it
     {:db (assoc db :campaigns campaigns)
      :http {:uri (str "https://ipfs.effect.ai/ipfs/" ipfs-hash)
             :timeout 10000
             :format (ajax/json-request-format)
             :response-format (ajax/json-response-format {:keywords? true})
             :method :get
             :on-success [:success-ipfs-result ipfs-hash]
             :on-failure [:failure-ipfs-result]}})))

(reg-event-fx
 :click-load-campaigns
 (fn [{:keys [db]} _]
   {:db (update db :total-ticks inc)
    :eos/get-table-rows {:code "force.efx"
                         :table "campaign"
                         :scope "force.efx"
                         :on-success [:success-load-campaigns]}}))

(defui campaign-box [c]
  (let [{:keys [description category title image]} (use-sub [:campaign-content c])]
    ($ Text {:key (:id c)} (str "*" title "*" (get-in c [:owner 0]) " x~~ " (get-in c [:owner 1])))))

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
         ($ campaign-box c)))))

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
 :launch-app
 (fn [{:keys [db]} _]
   {:db {:current-path "/home"
         :total-ticks 0
         :ipfs-objects {}}}))

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
  (refx/dispatch-sync [:launch-app])
  ($ SafeAreaView
     ($ Text "bare bones demo of effect network native app")
     ($ main-screen)))

;; EFFECT HANDLERS
(reg-fx
 :eos/get-table-rows
 (fn [{:keys [on-success] :as args}]
   (-> (.call (goog.object/get eos "get_table_rows") eos (clj->js args))
       (.then
        (fn [res]
          (let [rows (:rows (js->clj res :keywordize-keys true))]
            (dispatch (conj on-success rows)))))
       (.catch (fn [res] (prn "Error in get_table_rows" res))))))
