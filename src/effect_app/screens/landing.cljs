(ns effect-app.screens.landing
  (:require
   [uix.core :refer [defui $]]
   [effect-app.ui :as ui]
   [refx.alpha :as refx :refer [reg-event-fx dispatch use-sub reg-fx reg-sub sub]]
   [react-native :refer [View TextInput SafeAreaView Image ScrollView
                         KeyboardAvoidingView]]
   [effect-app.modules.navigation :as nav]
   [effect-app.modules.eos :as eos]
   ["@wharfkit/antelope" :refer [PrivateKey]]))

(def db
  {;; string
   ::public-key nil
   ;; [status, [vector-of-accounts]]
   ::authorized-accounts [nil []]})


(reg-event-fx
 ::proceed-from-landing
 (fn [_]
   {::nav/navigate ["Login Overview" nil]}))

(reg-event-fx
 ::login
 (fn [_]
   {::nav/navigate ["Login Options"]}))

(reg-event-fx
 ::login-with-key
 (fn [_]
   {::nav/navigate ["Login Private Key"]}))

(reg-event-fx
 ::found-authorized-accounts
 (fn [{:keys [db]} [_ res]]
   {:db (assoc db ::authorized-accounts
               (update res 1
                       (fn [accs]
                         (filterv #(not (= (:permission_name %) "owner")) accs))))
    ::nav/navigate ["Select Authorized Account"]}))

(reg-event-fx
 ::register
 (fn [_]
   {::nav/reset-navigate ["Home"]}))


(defn load-private-key [k]
  (try [:ok (PrivateKey.from k)]
       (catch js/Error e
         [:error (.-message e)])))

(defn is-error? [val]
  (= :error (first val)))

(def is-ok? (complement is-error?))

(reg-event-fx
 ::import-key
 (fn [{:keys [db]} [_ private-key]]
   (let [[status key] (load-private-key private-key)]
     (case status
       :error {:db (assoc db ::public-key [:error key])}
       :ok
       (let [public-key (.. key toPublic toString)]
         ;; TODO: securely store key and move below to a future event
         ;; TODO: store internal key to wallet
         {:db (assoc db
                     ::authorized-accounts [:loading]
                     ::public-key [:ok public-key])
          ::eos/get-accounts-for-public-key
          {:public-key public-key
           :on-success [::found-authorized-accounts]}})))))

(defui base-view [{:keys [children style align]}]
  ($ SafeAreaView
     ($ ui/status-bar)
     ($ View {:style {:height "100%"
                      :width "100%"}}
        ($ ScrollView
           {:style {:flexGrow 1
                    :width "100%"}
            :contentContainerStyle #js {:flexGrow 1
                                        :flexDirection "column"
                                        :alignItems "center"
                                        :justifyContent (or align "center")}}
           ($ KeyboardAvoidingView
              {:style {:width "80%"}}
              children)))))

(defui landing-screen [_]
  ($ base-view
     ($ View {:align-items "center"}
        ($ ui/text {:style {:font-size 28
                            :margin-bottom "15%"}}
           "be the effect")
        ($ Image
           {:source ui/logo-landing
            :style {:width 290
                    :height 290
                    :margin-bottom "15%"}})
        ($ ui/button  {:on-click #(dispatch [::proceed-from-landing])
                       :title "this is the way"
                       :style {:width 250}}))))

(defui login-overview-screen [_]
  ($ base-view
     ($ ui/button-with-icon
        {:on-click #(dispatch [::register])
         :style {:margin-bottom 30}
         :title "new account"
         :image (ui/icons :sunrise)
         :description "it will only take a minute"})

     ($ ui/button-with-icon
        {:on-click #(dispatch [::login])
         :title "existing account"
         :image (ui/icons :gem)
         :description "import your EOS account"})))

(defui login-options-screen [_]
  ($ base-view
     ($ ui/button-with-icon
        {:on-click #(dispatch [::login-with-key])
         :style {:margin-bottom 30}
         :title "import key"
         :image (ui/icons :key-skeleton)
         :description "import private key"})

     ($ ui/button-with-icon
        {:on-click #(dispatch [::login])
         :style {:margin-bottom 30}
         :disabled? true
         :title "from desktop"
         :image (ui/icons :laptop-mobile)
         :description "using the Effect web app"})

     ($ ui/button-with-icon
        {:on-click #(dispatch [::login])
         :title "use Anchor"
         :disabled? true
         :image (ui/icons :brain)
         :description "import your EOS account"})))

(defui login-with-key-screen []
  (let [[txt set-txt!] (uix.core/use-state "")

        [pub-status pub] (use-sub [:get ::public-key])
        [accs-status accs] (use-sub [:get ::authorized-accounts])
        loading? (= accs-status :loading)
        error? (= :error pub-status)]

    ($ base-view
       ($ ui/h1 {:style {:align-self "flex-start"
                         :margin-top -40
                         :width "80%"}}
          "Paste your EOS private key")
       ($ ui/text-input {:inactive? loading?
                         :password? true
                         :on-change-text set-txt!})
       (when (= :error pub-status)
         ($ ui/text-bold {:style {:color "red"}} pub))
       ($ ui/button-dark {:on-click #(when (not loading?) (dispatch [::import-key txt]))
                          :style {:width "100%"
                                  :height 48
                                  :align-items "center"
                                  :margin-top 20}}
          (if loading?
            ($ ui/loader)
            ($ ui/text-bold {:style {:color "white"
                                     :font-size 18}}
               "Login"))))))

(defui select-account-screen []
  (let [[pub-status pub] (use-sub [:get ::public-key])
        [accs-status accs] (use-sub [:get ::authorized-accounts])]
    ($ base-view
       ($ ui/text {:style {:margin-vertical 20}}
          (str "Found " (count accs) " login options for your key. Select the account and permission you would like to use. Note: we do not allow log in as owner."))
       (for [a accs]
         ($ View {:key (gensym)}
            ($ ui/button-with-icon
               {:title (:account_name a)
                :description (str "@" (:permission_name a))
                :style {:width "100%"
                        :margin-bottom 20}
                :image (ui/icons :portrait)}))))))
