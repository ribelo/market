(ns ribelo.market.sales
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [net.cgrand.xforms.io :as xio]))

(defn read-file [file-path & {:keys [encoding] :or {encoding "utf-8"}}]
  (when (.exists (io/as-file file-path))
    (into []
          (comp (map #(str/split % #";"))
                (map (fn [{market-id          0
                           date               1
                           ean                2
                           sap                3
                           id                 4
                           name               5
                           qty                9
                           purchase-net-value 19
                           sell-net-value     11
                           sell-gross-value   12
                           net-profit         13
                           margin             14
                           receipts           15
                           category-id        22}]
                       (let [name               (str/lower-case name)
                             qty                (Double/parseDouble qty)
                             purchase-net-value (Double/parseDouble purchase-net-value)
                             sell-net-value     (Double/parseDouble sell-net-value)
                             sell-gross-value   (Double/parseDouble sell-gross-value)
                             net-profit         (Double/parseDouble net-profit)
                             margin             (e/round2 (/ (Double/parseDouble margin) 10.0))
                             receipts           (e/catching (Long/parseLong receipts) _ 0.0)
                             purchase-net-price (e/round2 (/ purchase-net-value qty))
                             sell-net-price     (e/round2 (/ sell-net-value qty))
                             sell-gross-price   (e/round2 (/ sell-gross-value qty))]
                         {:market/id                (str/lower-case market-id)
                          :sales/date               (jt/local-date date)
                          :product/name             name
                          :product/id               id
                          :product/ean              ean
                          :product/sap              sap
                          :product/qty              qty
                          :product/category-id      category-id
                          :sales/purchase-net-value purchase-net-value
                          :sales/purchase-net-price purchase-net-price
                          :sales/sell-net-value     sell-net-value
                          :sales/sell-net-price     sell-net-price
                          :sales/sell-gross-value   sell-gross-value
                          :sales/sell-gross-price   sell-gross-price
                          :sales/net-profit         net-profit
                          :sales/margin             margin
                          :sales/receipts           receipts}))))
          (xio/lines-in (io/reader file-path :encoding encoding)))))

(defn read-files [{:keys [market-id begin-date end-date data-path]}]
  (let [begin-date (cond-> begin-date (not (instance? java.time.LocalDate begin-date)) (jt/local-date))
        end-date   (cond-> end-date (not (instance? java.time.LocalDate end-date)) (jt/local-date))
        dates      (take-while #(jt/before? % (jt/plus end-date (jt/days 1)))
                               (jt/iterate jt/plus begin-date (jt/days 1)))]
    (->> dates
         (into []
               (comp
                (map #(let [date-str (jt/format "yyyy_MM_dd" %)
                            file-name (str (str/upper-case market-id)
                                           "_StoreSale_"
                                           date-str)
                            file-path (e/path data-path file-name)]
                        file-path))
                (filter #(.exists (io/as-file %)))
                (mapcat read-file))))))

(read-files {:market-id  "f01450"
             :begin-date "2019-01-01"
             :end-date   "2019-06-01"
             :data-path "/home/ribelo/s3-dane"})
