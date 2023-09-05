(ns effect-app.ui
  (:require
   [uix.core :refer [defui $]]
   [react-native :refer [Text Button View TextInput SafeAreaView Status StatusBar Image
                         TouchableHighlight TouchableOpacity]]))

;; https://www.flaticon.com/icon-fonts-most-downloaded?weight=bold&type=uicon
(def icons {:dollar (js/require "./img/icon/1/dollar.png")
            :info (js/require "./img/icon/1/info.png")
            :add (js/require "./img/icon/1/add.png")
            :apps (js/require "./img/icon/1/apps.png")
            :gem (js/require "./img/icon/1/gem.png")
            :confetti (js/require "./img/icon/1/confetti.png")
            :hashtag (js/require "./img/icon/1/hastag.png")
            :magic-wand (js/require "./img/icon/1/magic-wand.png")
            :sunrise (js/require "./img/icon/1/sunrise.png")
            :portrait (js/require "./img/icon/1/portrait.png")
            :camera (js/require "./img/icon/1/camera.png")
            :credit-card (js/require "./img/icon/1/credit-card.png")
            :at (js/require "./img/icon/1/at.png")
            :unlock (js/require "./img/icon/1/unlock.png")
            :brain (js/require "./img/icon/1/brain.png")
            :key-skeleton (js/require "./img/icon/1/key-skeleton-left-right.png")
            :laptop-mobile (js/require "./img/icon/1/laptop-mobile.png")})

(def logo-landing
  (js/require "./img/logo/logo-landing-400x400.png"))

(defui text [{:keys [children style]}]
  ($ Text {:style (merge
                   {:font-family "Inter-Regular"
                    :color "#606060"}
                   style)}
     children))

(defui text-bold [{:keys [children style]}]
  ($ text {:style (assoc style :font-family "Inter-SemiBold")} children))

(defui button [{:keys [on-click title children style title-style disabled?]}]
  ($ TouchableOpacity {:on-press (when (not disabled?) on-click)
                       :underlay-color "#AAAAAA"
                       :active-opacity 0.1
                       :style (merge
                               {:background-color "#E3E3E3"
                                :border-color "#AAAAAA"
                                :border-width 1.0
                                :opacity (if disabled? 0.3 1.0)
                                :border-radius 10
                                :padding-horizontal 36
                                :width "80%"
                                :padding-vertical 18
                                :flex-direction "row"
                                :justify-content "center"}
                               style)}
     (if title
       ($ text-bold {:style (merge {:font-size 21} title-style)} title)
       children)))

(defui button-dark [{:keys [on-click title children style title-style]}]
  ($ button {:style {:background-color "#333333"}
             :title title
             :title-style (merge {:color "white"} title-style)
             :on-click on-click}
     children))

(defui button-with-icon [{:keys [style image title description on-click disabled?]}]
  ($ button {:on-click on-click
             :disabled? disabled?
             :style (merge
                     {:justify-content "flex-start"
                      :align-items "center"
                      :padding-left 0}
                     style)}
     ($ View {:style {:border-color "#AAAAAA"
                      :background-color "#EEEEEE"
                      :border-width 1
                      :border-radius 10
                      :align-self "flex-start"
                      :margin-horizontal 18
                      :padding 10}}
        ($ Image {:source image
                  :style {:width 32
                          :height 32
                          :tint-color "#000000FF"}}))
     ($ View {:flex-shrink 1}
        ($ text-bold {:style {:font-size 21
                              :color "black"}} title)
        ($ text description))))

(defui status-bar []
  ($ StatusBar {:background-color "#F0F0F0"
                :bar-style "dark-content"}))

(defui image [args] ($ Image args))
