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
    (.call (g/get chain "get_table_rows") chain (clj->js args))))

(reg-fx
 :eos/get-table-rows
 (fn [{:keys [on-success] :as args}]
   (-> (call-chain "get_table_rows" args)
       (.then
        (fn [res]
          (let [rows (:rows (js->clj res :keywordize-keys true))]
            (dispatch (conj on-success rows)))))
       (.catch (fn [res] (prn "Error in get_table_rows" res))))))
