(ns effect-app.core
  (:require [uix.core :refer [defui $]]
            [react-native :refer [Text Button View TextInput SafeAreaView Status StatusBar Image]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx sub]]
            [goog.object :as g]
            ["@wharfkit/antelope" :refer [APIClient FetchProvider]]
            ["@react-navigation/native-stack" :refer [createNativeStackNavigator]]
            ["@react-navigation/native" :refer [NavigationContainer]]
            effect-app.modules.eos
            effect-app.modules.effect
            [effect-app.modules.navigation :as nav]))


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

(reg-event-fx
 :proceed
 (fn [_]
   {:navigate ["Login" nil]}))

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
       ($ View {:style {:margin-top 20}}
          ($ button {:on-click
                     #(do
                        (dispatch [:proceed]))}
             (str "Proceed")))
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

(def stack-nav (createNativeStackNavigator))

(defui landing-screen [{:keys [children]}]
  ($ SafeAreaView
     ($ StatusBar {:background-color "#F0F0F0"
                   :bar-style "dark-content"})
     ($ View {:style {:padding 10}}
        ($ Text {:style {:font-family "Inter-Regular"
                         :font-size 24}}
           "This is a header"))
     ($ main-screen)))

(defui login-screen [{:keys [children]}]
  ($ SafeAreaView
     ($ StatusBar {:background-color "#F0F0F0"
                   :bar-style "dark-content"})
     ($ View {:style {:padding 10}}
        ($ Text {:style {:font-family "Inter"
                         :color "black"
                         :font-size 24}}
           "LOGIN")
        ($ button {:on-click #(dispatch [:login])}
           "log i n"))))

(defui home-screen [{:keys [children]}]
  ($ SafeAreaView
     ($ StatusBar {:background-color "#F0F0F0"
                   :bar-style "dark-content"})
     ($ View {:style {:padding 10}}
        ($ Text #js {:style #js {
                         :fontSize 48
                         :fontFamily "Inter-Regular"}}
           "Home"))))

;; https://www.flaticon.com/icon-fonts-most-downloaded?weight=bold&type=uicon
(def icons {:dollar (js/require "./img/icon/1/dollar.png")
            :info (js/require "./img/icon/1/info.png")})

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
                 :tabBarInactiveTintColor "rgba(0, 0, 0, 0.4)"
                 :tabBarShowLabel false
                 :tabBarIcon (fn [args]
                               (let [{:keys [focused size color]} (wrap args)]
                                 (prn "asdfasdf " focused)
                                 ($ Image {:source
                                           (case route-name
                                             "Overview" (:dollar icons)
                                             "Profile" (:info icons))
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
          #($ login-screen)))))

(reg-event-fx
 :login
 (fn [_]
   {:reset-navigate ["Home"]}))

(def nav-theme
  #js {:colors #js {:primary "rgb(0, 0, 0)"
                    :background "#F0F0F0"
                    :text "#000000"}})

(defn ^:export -main [& args]
  (refx/dispatch-sync [:app-load])
  ($ NavigationContainer
     {:ref nav/navigation-ref
      ;; :theme nav-theme
      }
     ($ (.-Navigator stack-nav)
        ($ (.-Screen stack-nav)
           {:name "Landing"
            :options #js {:headerShown false}}
           #($ landing-screen))
        ($ (.-Screen stack-nav)
           {:name "Login"
            :options #js {:title "Login"}}
           #($ login-screen))
        ($ (.-Screen stack-nav)
           {:name "Home"
            :options #js {:headerShown false}}
           #($ home-tabs)))))
