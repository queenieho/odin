(ns apply.db)


(defn bootstrap [account]
  {:lang    :en
   :account account
   :route   {:page      :home
             :path      [:home]
             :params    {}
             :requester account}})
