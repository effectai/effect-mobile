(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [effect-app.ui :as ui]
            [react-native :refer [Text Button View TextInput SafeAreaView Status StatusBar Image
                                  TouchableHighlight TouchableOpacity]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx sub]]
            [goog.object :as g]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]
            ["@react-navigation/native-stack" :refer [createNativeStackNavigator]]
            ["@react-navigation/native" :refer [NavigationContainer]]

            effect-app.screens.landing
            effect-app.modules.eos
            effect-app.modules.effect
            [effect-app.modules.navigation :as nav]))

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

(reg-event-fx
 :assoc-db
 (fn [db [_ k v]]
   {:db (assoc db k v)}))

(refx/reg-sub
 :get
 (fn [db [_ k]]
   (get db k)))

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
   {:db (if (empty? db)
          {:current-path "/home"
           :total-ticks 0
           :ipfs-objects {}}
          db)
    :effect-app.modules.effect/init "jungle4"}))

(reg-event-fx
 :success-ipfs-result
 (fn [{[_ ipfs-hash res] :event :keys [original-event db]}]
   {:db (assoc-in db [:ipfs-objects ipfs-hash] res)}))

(reg-event-fx
 :failure-ipfs-result
 (fn [{[_ res] :event :keys [original-event db]}]
   (prn  (count (:result res)))
   {}))

(def stack-nav (createNativeStackNavigator))

(defui home-screen [{:keys [children]}]
  ($ SafeAreaView
     ($ StatusBar {:background-color "#F0F0F0"
                   :bar-style "dark-content"})
     ($ View {:style {:padding 10}}
        ($ Text #js {:style #js {
                         :fontSize 48
                         :fontFamily "Inter-Regular"}}
           "Home"))))



(defn wrap
  "Helper that allows us to destructure javascript objects"
  [o]
  (reify
    ILookup
    (-lookup [_ k]
      (goog.object/get o (name k)))
    (-lookup [_ k not-found]
      (goog.object/get o (name k) not-found))))

(defui home-tabs []
  (let [[tab-nav tab-screen] (nav/create-bottom-tab-nav)]
    ($ tab-nav
       {:screen-options
        (fn [route]
          (let [route-name (g/getValueByKeys route "route" "name")]
            (prn "asdf " route-name)
            #js {:headerShown false
                 :tabBarActiveTintColor "black"
                 :tabBarInactiveTintColor "rgba(0, 0, 0, 0.25)"
                 :tabBarShowLabel false
                 :tabBarIcon (fn [args]
                               (let [{:keys [focused size color]} (wrap args)]
                                 (prn "asdfasdf " focused)
                                 ($ Image {:source
                                           (case route-name
                                             "Overview" (ui/icons :dollar)
                                             "Profile" (ui/icons :portrait)
                                             "Campaigns" (ui/icons :apps))
                                           :style {:width size
                                                   :height size
                                                   :tintColor color
                                                   :opacity (if focused 1.0 1.0)}})))
                 :tabBarStyle #js {:backgroundColor "#F0F0F0"
                                   :borderColor "rgba(50, 50, 50, 0.2)"
                                   :borderTopWidth 1
                                   :height 60}}))
        }
       ($ tab-screen
          {:name "Overview"
           :options #js {:headerShown true
                         :headerTitleAlign "center"
                         :headerTitleStyle #js {:fontFamily "Inter-Regular"
                                                :fontWeight "bold"}}}
          #($ home-screen))
       ($ tab-screen
          {:name "Profile"}
          #($ home-screen))
       ($ tab-screen
          {:name "Campaigns"}
          #($ home-screen)))))

(def nav-theme
  #js {:colors #js {:primary "rgb(0, 0, 0)"
                    :background "#F0F0F0"
                    :text "#000000"}})

(defn ^:export -main [& args]
  (refx/dispatch-sync [:app-load])
  ($ NavigationContainer
     {:ref nav/navigation-ref
      :theme nav-theme}
     ($ (.-Navigator stack-nav)
        #js {:screenOptions
             #js {:headerTitleAlign "center"
                  :headerStyle #js {:backgroundColor "#EEEEEE"}
                  :headerTitleStyle #js {:fontFamily "Inter-SemiBold"}}}
        ($ (.-Screen stack-nav)
           {:name "Landing"
            :options #js {:headerShown false}}
           #($ effect-app.screens.landing/landing-screen))
        ($ (.-Screen stack-nav)
           {:name "Login Overview"
            :options #js {:headerShown false}}
           #($ effect-app.screens.landing/login-overview-screen))
        ($ (.-Screen stack-nav)
           {:name "Login Options"
            :options #js {:title "Login Options"}}
           #($ effect-app.screens.landing/login-options-screen))
        ($ (.-Screen stack-nav)
           {:name "Login Private Key"
            :options #js {:title "Import Key"}}
           #($ effect-app.screens.landing/login-with-key-screen))
        ($ (.-Screen stack-nav)
           {:name "Select Authorized Account"
            :options #js {:title "Select Account"}}
           #($ effect-app.screens.landing/select-account-screen))
        ($ (.-Screen stack-nav)
           {:name "Home"
            :options #js {:headerShown false}}
           #($ home-tabs)))))
