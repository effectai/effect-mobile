(ns effect-app.modules.navigation
  (:require [uix.core :refer [defui $]]
            [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx sub]]
            [goog.object :as g]
            ["@react-navigation/native-stack" :refer [createNativeStackNavigator]]
            ["@react-navigation/bottom-tabs" :refer [createBottomTabNavigator]]
            ["@react-navigation/native" :refer [NavigationContainer
                                                useNavigation
                                                StackActions
                                                createNavigationContainerRef]]))

(def navigation-ref (createNavigationContainerRef))

(reg-fx
 :navigate
 (fn [[screen-name event-id]]
   (prn "Navigating to " screen-name event-id)
   (.navigate navigation-ref screen-name event-id)))

(reg-fx
 :reset-navigate
 (fn [[screen-name]]
   (prn "Reset navigation history to " screen-name)
   ((g/get navigation-ref "dispatch") ((g/get StackActions "popToTop")))
   ((g/get navigation-ref "dispatch") ((g/get StackActions "replace") screen-name #js {}))))

(defn create-bottom-tab-nav
  "Create bottom tab nav and return a tuple of `Navigator` `Screen`."
  []
  (let [tabs (createBottomTabNavigator)]
    [(.-Navigator tabs) (.-Screen tabs)]))
