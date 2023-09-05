(ns effect-app.modules.eos
  (:require
   [goog.object :as g]
   ["@wharfkit/antelope" :refer [APIClient FetchProvider]]
   [refx.alpha :as refx :refer [reg-event-fx reg-fx dispatch]]))

(def api-client (atom nil))

;; (def eos
;;   (-> (APIClient. #js {:provider (FetchProvider. "https://eos.greymass.com")})
;;       (goog.object/get "v1")
;;       (goog.object/get "chain")))

(defn call-chain [method args]
  (let [chain (-> @api-client (g/get "v1") (g/get "chain"))]
    (.call (g/get chain method) chain (clj->js args))))

(reg-fx
 ::get-table-rows
 (fn [{:keys [on-success] :as args}]
   (-> (call-chain "get_table_rows" args)
       (.then
        (fn [res]
          (let [rows (:rows (js->clj res :keywordize-keys true))]
            (dispatch (conj on-success rows)))))
       (.catch (fn [res] (prn "Error in get_table_rows" res))))))

(reg-fx
 ::get-accounts-for-public-key
 (fn [{:keys [on-success public-key] :as args}]
   (-> (call-chain "get_accounts_by_authorizers" {:keys [public-key]})
       (.then
        (fn [res]
          (let [rows (mapv (fn [a]
                             (reduce #(assoc %1 (keyword %2) (.toString (g/get a %2)))
                                     {}
                                     ["account_name" "permission_name" "authorizing_key" "weight" "threshold"]))
                           (g/get res "accounts"))]
            (dispatch (conj on-success [:ok rows])))))
       (.catch (fn [res] (prn "Error in get_accounts_by_authorizers" res))))))
