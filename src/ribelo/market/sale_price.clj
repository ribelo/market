(ns ribelo.market.sale-price
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [java-time :as jt]
   [net.cgrand.xforms.io :as xio]
   [ribelo.wombat :as wb]
   [ribelo.wombat.io :as wio]
   [taoensso.encore :as e]))


(wio/read-csv "/home/ribelo/s4-dane/F01752_SalePrice_2019_01_08"
              {:sep ";"})

(defn read-file [file-path date]
  (when (.exists (io/as-file file-path))
    (into []
          (comp (map #(str/split % #";"))
                (map (fn [{market-id 0
                           id        4
                           qty       9
                           promotion 14}]
                       (let [qty (Double/parseDouble qty)]
                         {:market/id            (str/lower-case market-id)
                          :sale-price/date      (cond-> date (not (instance? java.time.LocalDate date)) (jt/local-date))
                          :product/id           id
                          :sale-price/qty       qty
                          :sale-price/promotion promotion}))))
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
                                             "_SalePrice_"
                                             date-str)
                              file-path (e/path data-path file-name)]
                          [file-path %]))
                  (filter (fn [[file-path _]](.exists (io/as-file file-path))))
                  (mapcat (fn [[file-path date]] (read-file file-path date))))))))
