(ns admin.notes.db)


(def path ::notes)


(def default-value
  {path {:creating false
         :form     {:notify true}}})
