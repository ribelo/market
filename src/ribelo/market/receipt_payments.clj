(ns ribelo.market.receipt-payments
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [ribelo.wombat.io :as wio]
   [net.cgrand.xforms :as x]))

(defn read-file [file-path]
  (wio/read-csv
   file-path
   {:sep ";"
    :header {:market/id 0
             :receip/id 2
             :receipt/type 3
             :receipt/sell-gross-value 4}
    :parse {:market/id str/lower-case
            :receipt/type #(case % "G" :cash "K" :card)
            :receipt/sell-gross-value e/as-?float}}))

(defn read-files [{:keys [market-id begin-date end-date data-path]}]
  (let [begin-date (cond-> begin-date (not (instance? java.time.LocalDate begin-date)) (jt/local-date))
        end-date   (cond-> end-date (not (instance? java.time.LocalDate end-date)) (jt/local-date))
        dates (take-while #(jt/before? % (jt/plus end-date (jt/days 1)))
                          (jt/iterate jt/plus begin-date (jt/days 1)))]
    (->> dates
         (x/into []
                 (comp
                  (map #(let [date-str (jt/format "yyyy_MM_dd" %)
                              file-name (str (str/upper-case market-id)
                                             "_ReceiptPayment_"
                                             date-str)
                              file-path (e/path data-path file-name)]
                          file-path))
                  (filter #(.exists (io/as-file %)))
                  (mapcat read-file))))))
