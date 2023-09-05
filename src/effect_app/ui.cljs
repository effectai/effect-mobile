(ns effect-app.ui
  (:require
   [uix.core :refer [defui $]]
   [react-native :refer [Text Button View TextInput SafeAreaView Status StatusBar Image
                         TouchableHighlight TouchableOpacity Dimensions ActivityIndicator
                         PixelRatio Alert]]
   [goog.object :as g]))

;; https://www.flaticon.com/icon-fonts-most-downloaded?weight=bold&type=uicon
(def icons {
            :add           (js/require "./img/icon/1/add.png")
            :apps          (js/require "./img/icon/1/apps.png")
            :at            (js/require "./img/icon/1/at.png")
            :brain         (js/require "./img/icon/1/brain.png")
            :camera        (js/require "./img/icon/1/camera.png")
            :check         (js/require "./img/icon/1/check.png")
            :confetti      (js/require "./img/icon/1/confetti.png")
            :credit-card   (js/require "./img/icon/1/credit-card.png")
            :cross         (js/require "./img/icon/1/cross.png")
            :dollar        (js/require "./img/icon/1/dollar.png")
            :flame         (js/require "./img/icon/1/flame.png")
            :gem           (js/require "./img/icon/1/gem.png")
            :globe         (js/require "./img/icon/1/globe.png")
            :grin          (js/require "./img/icon/1/grin.png")
            :hashtag       (js/require "./img/icon/1/hastag.png")
            :heart         (js/require "./img/icon/1/heart.png")
            :info          (js/require "./img/icon/1/info.png")
            :key-skeleton  (js/require "./img/icon/1/key-skeleton-left-right.png")
            :laptop-mobile (js/require "./img/icon/1/laptop-mobile.png")
            :magic-wand    (js/require "./img/icon/1/magic-wand.png")
            :portrait      (js/require "./img/icon/1/portrait.png")
            :settings      (js/require "./img/icon/1/settings.png")
            :star          (js/require "./img/icon/1/star.png")
            :sun           (js/require "./img/icon/1/sun.png")
            :sunrise       (js/require "./img/icon/1/sunrise.png")
            :unlock        (js/require "./img/icon/1/unlock.png")
            })

(def logo-landing
  (js/require "./img/logo/logo-landing-400x400.png"))

(def status-bar-height (g/get StatusBar "currentHeight"))
(def window-width  (goog.object/get (.get Dimensions "window") "width"))
(def window-height (goog.object/get (.get Dimensions "window") "height"))
(prn "*** " window-height)

(defui text [{:keys [children style]}]
  ($ Text {:style (merge
                   {:font-family "Inter-Regular"
                    :color "#606060"}
                   style)}
     children))

(defui h1 [args] ($ text (assoc-in args [:style :font-size] 18)))

(defui text-bold [{:keys [children style]}]
  ($ text {:style (assoc style :font-family "Inter-SemiBold")} children))

(defui loader [args]
  ($ View (merge {:align-items "center" :justify-content "center" } args)
     ($ ActivityIndicator {:size "large" :color "white"})))

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
                                :padding-vertical 9
                                :flex-direction "row"
                                :justify-content "center"}
                               style)}
     (if title
       ($ text-bold {:style (merge {:font-size 21} title-style)} title)
       children)))

(defui button-dark [{:keys [on-click title children style title-style]}]
  ($ button {:style (merge {:background-color "#333333"} style)
             :title title
             :title-style (merge {:color "white"} title-style)
             :on-click on-click}
     children))

(defui button-with-icon [{:keys [style image title description
                                 on-click disabled?]}]
  ($ button {:on-click on-click
             :disabled? disabled?
             :style (merge
                     {:justify-content "flex-start"
                      :padding-vertical 18
                      :align-items "center"
                      :width "100%"
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

(defui text-input [{:keys [number-of-lines style on-change-text value inactive? password?]}]
  (let [[is-focused? set-is-focused!] (uix.core/use-state false)
        multiline? (not (nil? number-of-lines))]
    ($ TextInput
       {:multiline multiline?
        :number-of-lines number-of-lines
        :on-blur #(set-is-focused! false)
        :on-change-text on-change-text
        :on-focus #(set-is-focused! true)
        :secure-text-entry password?
        :style (merge {:border-width 1
                       :padding-horizontal 10
                       :padding-vertical 10
                       :background-color (cond
                                           inactive? "#AAAAAA"
                                           is-focused? "#FFFFFF"
                                           :else "#F6F6F6")
                       :text-align-vertical (if multiline? "top" "center")
                       :border-radius 10}
                      style)} "")))
