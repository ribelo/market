(ns ribelo.market.receipt-sale
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [ribelo.wombat :as wb :refer [=>> +>>]]
   [ribelo.wombat.io :as wio]
   [ribelo.visby.math :as math]
   [net.cgrand.xforms :as x]))

(defn read-file [file-path]
  (=>> (wio/read-csv
        file-path
        {:sep    ";"
         :header {:market/id                  0
                  :product/ean                1
                  :product/sap                2
                  :product/id                 3
                  :product/qty                4
                  :receipt/purchase-net-value 5
                  :receipt/sell-net-value     6
                  :receipt/sell-gross-value   7
                  :cash/id                    9
                  :receipt/id                 10
                  :receipt/hour               13
                  :cashier/id                 11
                  :category/id                19}
         :parse  {:market/id                  str/lower-case
                  :product/qty                e/as-float
                  :receipt/purchase-net-value e/as-float
                  :receipt/sell-net-value     e/as-float
                  :receipt/sell-gross-value   e/as-float
                  :receipt/hour               e/as-int
                  :cash/id                    e/as-int
                  :cashier/id                 e/as-int}})
       (map (fn [{:keys [product/qty
                         receipt/purchase-net-value
                         receipt/sell-net-value
                         receipt/sell-gross-value
                         receipt/id]
                  :as   m}]
              (let [date (jt/local-date "yyMMdd" (subs id 0 6))]
                (assoc m
                       :receipt/purchase-net-price (math/round 2 (/ purchase-net-value qty))
                       :receipt/sell-net-price     (math/round 2 (/ sell-net-value qty))
                       :receipt/sell-gross-price   (math/round 2 (/ sell-gross-value qty))
                       :receipt/date               date))))))

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
                                             "_ReceiptSale_"
                                             date-str)
                              file-path (e/path data-path file-name)]
                          file-path))
                  (filter #(.exists (io/as-file %)))
                  (mapcat read-file))))))
