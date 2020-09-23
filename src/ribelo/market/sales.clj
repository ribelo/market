(ns ribelo.market.sales
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [meander.epsilon :as m]
   [hanse.danzig :as dz :refer [=>>]]
   [hanse.danzig.io :as dz.io]
   [hanse.rostock.math :as math]))

(defn read-file [file-path]
  (when (.exists (io/as-file file-path))
    (=>> (dz.io/read-csv
           file-path
           {:sep      ";"
            :encoding "cp1250"
            :header   {0  [:dc.sales/market-id str/lower-case]
                       1  [:dc.sales/date :date]
                       2  [:dc.sales.product/ean]
                       5  [:dc.sales.product/name str/lower-case]
                       9  [:dc.sales.product/qty :double]
                       10 [:dc.sales.product/purchase-net-value :double]
                       11 [:dc.sales.product/sell-net-value :double]
                       12 [:dc.sales.product/sell-gross-value :double]
                       13 [:dc.sales.product/net-profit :double]
                       14 [:dc.sales.product/margin #(-> % (e/parse-float) (* 0.01) (e/round2))]
                       15 [:dc.sales/receipts :long]
                       22 [:dc.sales.product/category str/lower-case]}})
         (dz/set :dc.sales.product/sell-net-price
                 (fn [{:keys [dc.sales.product/sell-net-value
                             dc.sales.product/qty]}]
                   (when (pos? qty) (math/round2 (/ sell-net-value qty))))))))

(defn read-files [{:keys [market-id begin-date end-date data-path]}]
  (let [begin-date (cond-> begin-date (not (instance? java.time.LocalDate begin-date)) (jt/local-date))
        end-date   (cond-> end-date (not (instance? java.time.LocalDate end-date)) (jt/local-date))
        dates      (take-while #(jt/before? % (jt/plus end-date (jt/days 1)))
                               (jt/iterate jt/plus begin-date (jt/days 1)))
        files      (=>> (file-seq (io/as-file data-path))
                        (remove #(.isDirectory %))
                        (map #(.getName %))
                        #{})]
    (=>> dates
         (map (fn [date]
                (let [date-str  (jt/format "yyyy_MM_dd" date)
                      file-name (str (str/upper-case market-id)
                                     "_StoreSale_"
                                     date-str)
                      find-file (fn [s] (=>> files (filter #(re-find (re-pattern s) %)) .))
                      file-path (some->> (find-file file-name) (e/path data-path))]
                  file-path)))
         (remove nil?)
         (mapcat read-file))))
