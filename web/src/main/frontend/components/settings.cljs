(ns frontend.components.settings
  (:require [rum.core :as rum]
            [frontend.ui :as ui]
            [frontend.handler :as handler]
            [frontend.handler.notification :as notification]
            [frontend.handler.user :as user-handler]
            [frontend.state :as state]
            [frontend.util :as util]
            [frontend.config :as config]
            [clojure.string :as string]
            [goog.object :as gobj]))

(rum/defcs set-email < (rum/local "" ::email)
  [state]
  (let [email (get state ::email)]
    [:div.p-8.flex.items-center.justify-center
     [:div.w-full.mx-auto
      [:div
       [:div
        [:h1.title.mb-1
         "Your email address:"]
        [:div.mt-2.mb-4.relative.rounded-md.shadow-sm.max-w-xs
         [:input#.form-input.block.w-full.pl-2.sm:text-sm.sm:leading-5
          {:autoFocus true
           :on-change (fn [e]
                        (reset! email (util/evalue e)))}]]]]
      (ui/button
        "Submit"
        :on-click
        (fn []
          (user-handler/set-email! @email)))

      [:hr]

      [:span.pl-1.opacity-70 "Git commit requires the email address."]]]))

(rum/defcs set-cors < (rum/local "" ::cors)
  [state]
  (let [cors (get state ::cors)]
    [:div.p-8.flex.items-center.justify-center
     [:div.w-full.mx-auto
      [:div
       [:div
        [:h1.title.mb-1
         "Your cors address:"]
        [:div.mt-2.mb-4.relative.rounded-md.shadow-sm.max-w-xs
         [:input#.form-input.block.w-full.pl-2.sm:text-sm.sm:leading-5
          {:autoFocus true
           :on-change (fn [e]
                        (reset! cors (util/evalue e)))}]]]]
      (ui/button
        "Submit"
        :on-click
        (fn []
          (user-handler/set-cors! @cors)))

      [:hr]

      [:span.pl-1.opacity-70 "Git commit requires the cors address."]]]))

(rum/defcs settings < rum/reactive
  []
  (let [preferred-format (keyword (state/sub [:me :preferred_format]))
        github-token (state/sub [:me :access-token])
        cors-proxy (state/sub [:me :cors_proxy])]
    [:div#settings
     [:h1.title "Settings"]

     [:div.pl-1
      ;; config.edn
      [:a {:href (str "/file/" (util/encode-str (str config/app-name "/" config/config-file)))}
       "Edit config.edn (for current repo)"]

      [:hr]

      [:div.mt-6.sm:mt-5
       [:div.sm:grid.sm:grid-cols-3.sm:gap-4.sm:items-start.sm:pt-5
        [:label.block.text-sm.font-medium.leading-5.sm:mt-px.sm:pt-2.opacity-70
         {:for "preferred_format"}
         "Preferred file format"]
        [:div.mt-1.sm:mt-0.sm:col-span-2
         [:div.max-w-lg.rounded-md.shadow-sm.sm:max-w-xs
          [:select.mt-1.form-select.block.w-full.pl-3.pr-10.py-2.text-base.leading-6.border-gray-300.focus:outline-none.focus:shadow-outline-blue.focus:border-blue-300.sm:text-sm.sm:leading-5
           {:on-change (fn [e]
                         (let [format (-> (util/evalue e)
                                          (string/lower-case)
                                          keyword)]
                           (user-handler/set-preferred-format! format)))}
           (for [format [:org :markdown]]
             [:option (cond->
                          {:key (name format)}
                        (= format preferred-format)
                        (assoc :selected "selected"))
              (string/capitalize (name format))])]]]]
       [:div.mt-6.sm:mt-5.sm:grid.sm:grid-cols-3.sm:gap-4.sm:items-start.sm:pt-5
        [:label.block.text-sm.font-medium.leading-5.sm:mt-px.sm:pt-2.opacity-70
         {:for "pat"}
         "Github personal access token"]
        [:div.mt-1.sm:mt-0.sm:col-span-2
         [:div.max-w-lg.rounded-md.shadow-sm.sm:max-w-xs
          [:input#pat.form-input.block.w-full.transition.duration-150.ease-in-out.sm:text-sm.sm:leading-5
           {:default-value github-token
            :type "password"
            :autocomplete "new-password"
            :on-blur (fn [event]
                       (when-let [token (util/evalue event)]
                         (when-not (string/blank? token)
                           (user-handler/set-github-token! token false)
                           (notification/show! "Github personal access token updated successfully!" :success))))
            :on-key-press (fn [event]
                            (let [k (gobj/get event "key")]
                              (if (= "Enter" k)
                                (when-let [token (util/evalue event)]
                                  (when-not (string/blank? token)
                                    (user-handler/set-github-token! token false)
                                    (notification/show! "Github personal access token updated successfully!" :success))))))}]]]]

       [:hr ]

       (ui/admonition
        :important
        [:p "Don't use other people's proxy servers. It's very dangerous, which could make your token and notes stolen. Logseq will not be responsible for this loss if you use other people's proxy servers. You can deploy it yourself, check "
         [:a {:href "https://github.com/isomorphic-git/cors-proxy"
              :target "_blank"}
          "https://github.com/isomorphic-git/cors-proxy"]])

       [:div.mt-6.sm:mt-5.sm:grid.sm:grid-cols-3.sm:gap-4.sm:items-start.sm:pt-5
        [:label.block.text-sm.font-medium.leading-5.sm:mt-px.sm:pt-2.opacity-70
         {:for "cors"}
         "Custom CORS proxy server"]
        [:div.mt-1.sm:mt-0.sm:col-span-2
         [:div.max-w-lg.rounded-md.shadow-sm.sm:max-w-xs
          [:input#pat.form-input.block.w-full.transition.duration-150.ease-in-out.sm:text-sm.sm:leading-5
           {:default-value cors-proxy
            :on-blur (fn [event]
                       (when-let [server (util/evalue event)]
                         (user-handler/set-cors! server)
                         (notification/show! "Custom CORS proxy updated successfully!" :success)))
            :on-key-press (fn [event]
                            (let [k (gobj/get event "key")]
                              (if (= "Enter" k)
                                (when-let [server (util/evalue event)]
                                  (user-handler/set-cors! server)
                                  (notification/show! "Custom CORS proxy updated successfully!" :success)))))}]]]]]]]))