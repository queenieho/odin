(ns iface.utils.file)

(defn files->form-data
  "Given files being uploaded, convert them to the correct data form."
  [files]
  (let [form-data (js/FormData.)]
    (doseq [file-key (.keys js/Object files)]
      (let [file (aget files file-key)]
        (.append form-data "files[]" file (.-name file))))
    form-data))
