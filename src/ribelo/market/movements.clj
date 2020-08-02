(ns ribelo.market.movements
  (:require
   [clojure.java.io :as io]
   [java-time :as jt]
   [clojure.string :as str]
   [taoensso.encore :as e]
   [meander.epsilon :as m]
   [java-time :as jt]
   [hanse.danzig :as dz :refer [=>>]]
   [hanse.danzig.io :as dz.io]
   [hanse.rostock.math :as math]))

(defn translate-contractor [s]
  (m/match s
    "centralny1" "cg"
    "magazyncg"  "cg"
    "magazync"   "cg"
    "magazyn c"  "cg"
    "sklep4"     "f01752"
    "sklep3"     "f01450"
    "sklep1"     "f01451"
    "sklep nr 1" "f01451"
    _            (throw (ex-info "bad contractor" {:s s}))))

(defn doc-id->doc-type [s]
  (m/match s
    (m/re #"^mw.*")  :movements/out
    (m/re #"^pk.*")  :corection/undefined
    (m/re #"^kmp.*") :corection/undefined
    (m/re #"^po.*")  :package/in
    (m/re #"^pz.*")  :purchase/slip
    (m/re #"^wo.*")  :package/out
    (m/re #"^mp.*")  :movements/in
    (m/re #"^pp.*")  :movements/discount
    (m/re #"^wz.*")  :sales/slip
    (m/re #"^pl.*")  :movements/liquidation
    (m/re #"^pi.*")  :movements/inventory
    nil              :movement/undefined
    ;; (m/re "&km")
    _                (throw (ex-info "bad doc id" {:s s}))))

(defn read-file [file-path]
  (when (.exists (io/as-file file-path))
    (=>> (dz.io/read-csv
           file-path
           {:sep      ";"
            :encoding "cp1250"
            :header   {0  [:dc.movement/market-id str/lower-case]
                       2  [:dc.movement/posting-date :date]
                       3  [:dc.movement/date #(e/catching (jt/local-date %))]
                       6  [:dc.movement/market-doc-id str/lower-case]
                       7  [:dc.movement/document-id str/lower-case]
                       10 [:dc.movement.product/sap str/lower-case]
                       11 [:dc.movement.product/id str/lower-case]
                       12 [:dc.movement.product/name str/lower-case]
                       15 :dc.movement.product/ean
                       16 [:dc.movement.product/qty :double]
                       17 [:dc.movement.product/purchase-net-value :double]
                       19 [:dc.movement.product/sell-gross-value :double]
                       22 [:dc.movement/contractor str/lower-case]}})
         (filter identity)
         (map (fn [m] (e/if-lets [qty   (-> m :dc.movement.product/qty e/as-?pos-float)
                                  value (-> m :dc.movement.product/purchase-net-value e/as-?pos-float)
                                  price (e/round2 (/ value qty))]
                        (assoc m :dc.movement.product/purchase-net-price price)
                        m)))
         (map (fn [m] (e/if-lets [qty   (-> m :dc.movement.product/qty e/as-?pos-float)
                                  value (-> m :dc.movement.product/sell-gross-value e/as-?pos-float)
                                  price (e/round2 (/ value qty))]
                        (assoc m :dc.movement.product/sell-gross-price price)
                        m)))
         (dz/set :dc.movement.document/type [doc-id->doc-type :dc.movement/market-doc-id]))))

(defn read-files [{:keys [market-id begin-date end-date data-path]}]
  (let [begin-date (cond-> begin-date (not (instance? java.time.LocalDate begin-date)) (jt/local-date))
        end-date   (cond-> end-date (not (instance? java.time.LocalDate end-date)) (jt/local-date))
        dates      (take-while #(jt/before? % (jt/plus end-date (jt/days 1)))
                               (jt/iterate jt/plus begin-date (jt/days 1)))]
    (reduce
      (fn [acc dt]
        (let [date-str  (jt/format "yyyy_MM_dd" dt)
              file-name (str (str/upper-case (name market-id))
                             "_StockMovement_"
                             date-str)
              file-path (e/path data-path file-name)
              data      (read-file file-path)]
          (if data (into acc data) acc)))
      []
      dates)))
