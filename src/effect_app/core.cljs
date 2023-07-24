(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [uix.dom]
            [react-native :refer [Text Button View TextInput SafeAreaView]]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]))

(defui button [{:keys [on-click children]}]
  ($ Button {:on-press on-click :title "load campaigns"}))

(def eos
  (-> (APIClient. #js {:provider (FetchProvider. "https://eos.greymass.com")})
      (goog.object/get "v1")
      (goog.object/get "chain")))

(defui main-screen []
  (let [[value set-value!] (uix.core/use-state "demo text field")
        [camps set-camps!] (uix.core/use-state [])]
    ($ View
       ($ TextInput {:value value
                     :on-change-text #(set-value! %)})
       ($ button {:on-click
                  #(do
                     (prn "loading campaigns...")
                     (.then
                      (.get_table_rows eos
                                       #js {:code "force.efx"
                                            :table "campaign"
                                            :scope "force.efx"})
                      (fn [res]
                        (let [campaigns (:rows (js->clj res
                                                        :keywordize-keys true))]
                          (set-camps! campaigns)))))})
       (for [c camps]
         ($ Text (str (get-in c [:owner 0]) " - " (get-in c [:owner 1])))))))

(defn ^:export -main [& args]
  ($ SafeAreaView
     ($ Text "bare bones demo of effect network native app")
     ($ main-screen)))
