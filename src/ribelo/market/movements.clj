(ns ribelo.market.movements
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [net.cgrand.xforms :as x]
   [net.cgrand.xforms.io :as xio]))

(def translate-contractor
  {"centralny1" "cg"
   "magazyncg"  "cg"
   "magazync"   "cg"
   "magazyn c"  "cg"
   "sklep4"     "f01752"
   "sklep3"     "f01450"
   "sklep1"     "f01451"
   "sklep nr 1" "f01451"
   "ecsa"       "ecsa"})

(defn doc-id->doc-type [s]
  (cond
    (str/starts-with? s "mw")  :dispatch
    (str/starts-with? s "pk")  :corection
    (str/starts-with? s "kmp") :corection
    (str/starts-with? s "po")  :delivery
    (str/starts-with? s "pz")  :delivery
    (str/starts-with? s "wo")  :dispatch
    (str/starts-with? s "mp")  :delivery
    ;; (str/starts-with? s "pp")  :delivery
    (str/starts-with? s "wz")  :dispatch
    ;; (str/starts-with? s "km")
    :else                      :unknown))


(defn read-file [file-path]
  (when (.exists (io/as-file file-path))
    (into []
          (comp (map #(str/split % #";"))
                (map (fn [{market-id          0
                           posting-date       2
                           date               3
                           doc-id             6
                           market-doc-id      7
                           sap                10
                           product-id         11
                           product-name       12
                           ean                15
                           qty                16
                           purchase-net-value 17
                           sell-gross-value   19
                           contractor         22}]
                       (let [purchase-net-value' (Double/parseDouble purchase-net-value)
                             sell-gross-value'   (Double/parseDouble sell-gross-value)
                             qty'                (Double/parseDouble qty)
                             purchase-net-price  (e/round2 (/ purchase-net-value' qty'))
                             sell-gross-price    (e/round2 (/ sell-gross-value' qty'))]
                         {:market-id          (str/lower-case market-id)
                          :posting-date       (jt/local-date posting-date)
                          :date               (when (seq date) (jt/local-date date))
                          :doc-id             (str/lower-case doc-id)
                          :market-doc-id      (str/lower-case market-doc-id)
                          :sap                sap
                          :product-id         product-id
                          :product-name       (str/lower-case product-name)
                          :ean                ean
                          :qty                (Double/parseDouble qty)
                          :purchase-net-value purchase-net-value'
                          :sell-gross-value   sell-gross-value'
                          :purchase-net-price purchase-net-price
                          :sell-gross-price   sell-gross-price
                          :contractor         (translate-contractor (str/lower-case contractor))
                          :doc-type           (doc-id->doc-type (str/lower-case doc-id))}))))
          (xio/lines-in (io/reader file-path :encoding "cp1250")))))

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
                                             "_StockMovement_"
                                             date-str)
                              file-path (e/path data-path file-name)]
                          file-path))
                  (filter #(.exists (io/as-file %)))
                  (mapcat read-file))))))
