(ns ribelo.market.stock
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [java-time :as jt]
   [net.cgrand.xforms.io :as xio]
   [taoensso.encore :as e]))

(defn read-file [file-path]
  (when (.exists (io/as-file file-path))
    (into []
          (comp (map #(str/split % #";"))
                (map (fn [{market-id          0
                           id                 1
                           ean                2
                           sap                3
                           date               4
                           qty                5
                           purchase-net-value 6
                           sell-gross-value   8
                           vat                9
                           category-id        11}]
                       (let [purchase-net-value (Double/parseDouble purchase-net-value)
                             sell-gross-value   (Double/parseDouble sell-gross-value)
                             qty                (Double/parseDouble qty)
                             purchase-net-price (e/round2 (/ purchase-net-value qty))
                             sell-gross-price   (e/round2 (/ sell-gross-value qty))
                             vat                (e/round2 (/ (Double/parseDouble vat) 100.0))
                             sell-net-price     (e/round2 (/ sell-gross-price (+ 1.0 vat)))
                             sell-net-value     (e/round2 (* sell-gross-price qty))]
                         {:market/id                (str/lower-case market-id)
                          :stock/date               (jt/local-date date)
                          :product/id               id
                          :product/ean              ean
                          :product/sap              sap
                          :stock/qty                qty
                          :product/vat              vat
                          :product/category-id      category-id
                          :stock/purchase-net-value purchase-net-value
                          :stock/purchase-net-price purchase-net-price
                          :stock/sell-gross-value   sell-gross-value
                          :stock/sell-gross-price   sell-gross-price
                          :stock/sell-net-price     sell-net-price
                          :stock/sell-net-value     sell-net-value}))))
          (xio/lines-in (io/reader file-path :encoding "cp1250")))))

(defn read-files [{:keys [market-id begin-date end-date data-path]}]
  (let [begin-date (cond-> begin-date (not (instance? java.time.LocalDate begin-date)) (jt/local-date))
        end-date   (cond-> end-date (not (instance? java.time.LocalDate end-date)) (jt/local-date))
        dates (take-while #(jt/before? % (jt/plus end-date (jt/days 1)))
                          (jt/iterate jt/plus begin-date (jt/days 1)))]
    (->> dates
         (into []
                 (comp
                  (map #(let [date-str (jt/format "yyyy_MM_dd" %)
                              file-name (str (str/upper-case market-id)
                                             "_ProductStock_"
                                             date-str)
                              file-path (e/path data-path file-name)]
                          file-path))
                  (filter #(.exists (io/as-file %)))
                  (mapcat read-file))))))
