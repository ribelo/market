(ns ribelo.market.categories
  (:refer-clojure :exclude [ancestors parents])
  (:require [taoensso.encore :as e]
            [clojure.string :as str]))

(def by-id
  {"01"     {:name       "alkohole"
             :max-margin 0.21},
   "0101"   {:name "wino"
             :max-margin 0.30},
   "010101" {:name "cydry"},
   "010103" {:name "nalewki"},
   "010104" {:name "vermouthy"},
   "010105" {:name "wina musujące"},
   "010106" {:name "wina owocowe"},
   "010107" {:name "wina spokojne"},
   "010108" {:name "wino migracja"},
   "0103"   {:name "piwo"
             :max-margin 0.20},
   "010301" {:name "piwo w butelce"},
   "010302" {:name "piwo w puszce"},
   "0104"   {:name "alkohole mocne"
             :max-margin 0.21},
   "010401" {:name "brandy, gin, rum"},
   "010402" {:name "drinki"},
   "010403" {:name "likiery"},
   "010404" {:name "whisky"},
   "010405" {:name "wódka"},
   "010406" {:name "alkohole migracja"},
   "02"     {:name "chemia"
             :max-margin 0.25},
   "0202"   {:name "kosmetyki"
             :max-margin 0.31},
   "020201" {:name "artykuły sezonowe, kosmetyki"},
   "020202" {:name "dezodoranty i perfumy"},
   "020203" {:name "golenie"},
   "020204" {:name "makijaż"},
   "020205" {:name "mydła"},
   "020206" {:name "pasty i higiena jamy ustnej"},
   "020207" {:name "pielęgnacja ciała i twarzy"},
   "020208" {:name "płyny/żele do kąpieli"},
   "020209" {:name "preparaty do farbowania włosów"},
   "020210" {:name "preparaty do higieny intymnej"},
   "020211" {:name "preparaty do układania włosów"},
   "020212" {:name "szampony i odżywki"},
   "020213" {:name "kosmetyki migracja"},
   "0203"   {:name "odświeżacze"
             :max-margin 0.27},
   "020301" {:name "odświeżacze"},
   "0204"   {:name "sezonowe owady"},
   "020401" {:name "sezonowe owady"},
   "0205"   {:name "środki do czyszczenia"
             :max-margin 0.27},
   "020501" {:name "środki do czyszczenia"},
   "0206"   {:name "detergenty do prania"
             :max-margin 0.24},
   "020601" {:name "detergenty do prania"},
   "0207"   {:name "detergenty do zmywania"},
   "020701" {:name "detergenty do zmywania"},
   "0208"   {:name "higiena osobista"
             :max-margin 0.30},
   "020801" {:name "chusteczki i ręczniki"},
   "020802" {:name "higiena niemowląt"
             :max-margin 0.21},
   "020803" {:name "papier toaletowy"},
   "020804" {:name "pieluchy"
             :max-margin 0.21},
   "020805" {:name "podpaski"
             :max-margin 0.31},
   "020806" {:name "tampony"
             :max-margin 0.31},
   "03"     {:name "strefa przykasowa"
             :max-margin 0.065},
   "0301"   {:name "papierosy"
             :max-margin 0.065},
   "030101" {:name "akcesoria do papierosów"
             :max-margin 0.30},
   "030103" {:name "papierosy"},
   "030104" {:name "tytoń"},
   "0302"   {:name "prasa"
             :max-margin 0.22},
   "030201" {:name "prasa"},
   "0304"   {:name "farmacja"
             :max-margin 0.30},
   "030401" {:name "farmacja"},
   "0305"   {:name "telefonia i usługi"},
   "030501" {:name "telefonia"},
   "04"     {:name "mięso, wędliny i inne lada"
             :max-margin 0.20},
   "0401"   {:name "mięso białe waga"
             :max-margin 0.15},
   "040101" {:name "gęś waga"},
   "040102" {:name "indyk waga"},
   "040103" {:name "kaczka waga"},
   "040105" {:name "kurczak waga"},
   "0402"   {:name "ryby waga"
             :max-margin 0.25},
   "040201" {:name "marynaty rybne waga"},
   "040202" {:name "owoce morza waga"},
   "040203" {:name "ryby świeże waga"},
   "040204" {:name "ryby wędzone waga"},
   "0403"   {:name "sery waga"
             :max-margin 0.20},
   "040301" {:name "sery pleśniowe waga"},
   "040302" {:name "sery premium waga"},
   "040303" {:name "sery żółte waga"},
   "040304" {:name "twarogi waga"},
   "040305" {:name "sery waga przecena"},
   "0404"   {:name "wędliny waga"
             :max-margin 0.30},
   "040401" {:name "kabanosy waga"},
   "040403" {:name "kiełbasy cienkie waga"},
   "040404" {:name "kiełbasy grube waga"},
   "040405" {:name "mielonki, bloki waga"},
   "040406" {:name "parówki, parówkowe i serdelki waga"},
   "040407" {:name "salami waga"},
   "040408" {:name "wędzonki waga"},
   "040409" {:name "wyroby podrobowe i garmażeryjne waga"},
   "040410" {:name "wędliny waga przecena"},
   "0406"   {:name "garmażerka waga"
             :max-margin 0.30},
   "040602" {:name "dania mięsne waga"},
   "040603" {:name "dania rybne waga"},
   "040604" {:name "dania warzywne waga"},
   "0407"   {:name "mięso czerwone waga"},
   "040701" {:name "cielęcina waga"},
   "040702" {:name "inne mięso czerwone waga"},
   "040703" {:name "wieprzowina waga"},
   "040704" {:name "wołowina waga"},
   "05"     {:name "mrożonki"
             :max-margin 0.30},
   "0502"   {:name "mrożonki"},
   "050201" {:name "dania mrożone"},
   "050203" {:name "lody"},
   "050204" {:name "mięso mrożone"},
   "050205" {:name "owoce i warzywa mrożone"},
   "050206" {:name "ryby i owoce morza mrożone"},
   "050207" {:name "ryby i owoce morza mrożone - przecena"},
   "050208" {:name "mrożonki migracja"},
   "06"     {:name "nabiał i chłodzone samoobsługa"
             :max-margin 0.21},
   "0601"   {:name "jaja"
             :max-margin 0.21},
   "060101" {:name "jaja"},
   "0602"   {:name "sery samoobsługa"
             :max-margin 0.21},
   "060201" {:name "sery pleśniowe pakowane"},
   "060202" {:name "sery topione pakowane"},
   "060203" {:name "sery żółte pakowane"},
   "060204" {:name "twarogi pakowane"},
   "0603"   {:name "tłuszcze"
             :max-margin 0.20},
   "060301" {:name "margaryna kulinarna"},
   "060302" {:name "margaryna stołowa"},
   "060303" {:name "masło"},
   "060304" {:name "mixy"},
   "060305" {:name "olej"},
   "060306" {:name "oliwa z oliwek"},
   "060307" {:name "smalec"},
   "0606"   {:name "garmażerka samoobsługa"
             :max-margin 0.30},
   "060601" {:name "dania mączne pakowane"},
   "060602" {:name "dania mięsne pakowane"},
   "060603" {:name "dania rybne pakowane"},
   "060604" {:name "marynaty i sałatki rybne pakowane"},
   "060605" {:name "wędliny pakowane"},
   "0608"   {:name "produkty mleczne"
             :max-margin 0.21},
   "060801" {:name "desery, serki, kefiry"},
   "060802" {:name "jogurty "},
   "060803" {:name "mleko świeże"},
   "060804" {:name "mleko uht"},
   "060805" {:name "śmietany"},
   "07"     {:name "napoje, soki"
             :max-margin 0.27},
   "0701"   {:name "woda"
             :max-margin 0.30},
   "070101" {:name "woda"},
   "0703"   {:name "napoje, soki"},
   "070301" {:name "napoje gazowane inne"},
   "070302" {:name "napoje typu cola"},
   "070303" {:name "napoje typu ice tea"},
   "070304" {:name "napoje typu izotoniki, energetyki"},
   "070305" {:name "soki, nektary, inne napoje niegazowane"},
   "070306" {:name "syropy"
             :max-margin 0.30},
   "08"     {:name "kosztowe"
             :max-margin 0.0},
   "0802"   {:name "artykuły kosztowe"},
   "080201" {:name "artykuły kosztowe"},
   "0803"   {:name "opakowania"},
   "080301" {:name "opakowania"},
   "09"     {:name "owoce i warzywa"
             :max-margin 0.25},
   "0902"   {:name "kiszonki i koncentraty"},
   "0903"   {:name "kwiaty i rośliny"},
   "090201" {:name "kiszonki i koncentraty"},
   "090301" {:name "kwiaty i rośliny"},
   "0904"   {:name "owoce i warzywa pakowane"},
   "090401" {:name "owoce pakowane"},
   "090402" {:name "warzywa pakowane"},
   "090403" {:name "grzyby suszone pakowane"},
   "0905"   {:name "owoce luz"},
   "090501" {:name "bakalie"
             :max-margin 0.30},
   "090502" {:name "cytrusy"},
   "090503" {:name "jabłka i gruszki"},
   "090504" {:name "melony"},
   "090505" {:name "owoce egzotyczne"},
   "090506" {:name "owoce miękkie"},
   "090507" {:name "owoce pestkowe"},
   "090508" {:name "winogrona"},
   "0906"   {:name "sałaty i zioła"},
   "090601" {:name "sałaty pakowane"},
   "090602" {:name "zioła doniczka"},
   "0907"   {:name "warzywa luz"},
   "090701" {:name "grzyby"},
   "090702" {:name "warzywa kapustne"},
   "090703" {:name "warzywa okopowe"},
   "090704" {:name "warzywa sałatkowe"},
   "090705" {:name "warzywa sezonowe"},
   "090706" {:name "zieleniny"},
   "10"     {:name "pieczywo"
             :max-margin 0.24},
   "1002"   {:name "pieczywo"},
   "100201" {:name "pieczywo  - pieczywo tostowe"},
   "100202" {:name "pieczywo - bułka tarta"},
   "100203" {:name "pieczywo - torilla"},
   "100204" {:name "pieczywo mrożone - bułki, bagietki"},
   "100205" {:name "pieczywo mrożone - ciasta"},
   "100206" {:name "pieczywo mrożone - ciasta"},
   "100207" {:name "pieczywo mrożone - przekąski"},
   "100208" {:name "pieczywo regionalne - bułki i bagietki"},
   "100209" {:name "pieczywo regionalne - chleb biały"},
   "100210" {:name "pieczywo regionalne - chleb ciemny"},
   "100211" {:name "pieczywo regionalne - ciasta luz"},
   "100212" {:name "pieczywo regionalne - przekąski słodkie i słone"},
   "11"     {:name "przemysłowe"
             :max-margin 0.40},
   "1102"   {:name "artykuły przemysłowe"},
   "110201" {:name "akcesoria gospodarstwa domowego"},
   "110202" {:name "artykuły przemysłowe inne"},
   "110203" {:name "sezonowe grillowe"},
   "110204" {:name "sezonowe świąteczne"},
   "110205" {:name "szkoła"},
   "110206" {:name "znicze"},
   "12"     {:name "spożywka pakowana"
             :max-margin 0.25},
   "1201"   {:name "przekąski"
             :max-margin 0.30},
   "120101" {:name "przekąski"},
   "1203"   {:name "śniadania, zdrowa żywność"
             :max-margin 0.25},
   "120301" {:name "dżemy"},
   "120302" {:name "kakao i napoje czekoladowe"},
   "120303" {:name "płatki śniadaniowe"},
   "120304" {:name "zdrowa żywność"},
   "1206"   {:name "herbata"
             :max-margin 0.26},
   "120601" {:name "herbaty czarne"},
   "120602" {:name "herbaty pozostałe"},
   "1207"   {:name "kulinaria słona"
             :max-margin 0.28},
   "120701" {:name "buliony"
             :max-margin 0.30},
   "120702" {:name "dania gotowe"
             :max-margin 0.30},
   "120703" {:name "konserwy mięsne"},
   "120704" {:name "konserwy rybne"},
   "120705" {:name "ocet"},
   "120706" {:name "pasztety"},
   "120707" {:name "produkty z pomidorów"},
   "120708" {:name "przyprawy"},
   "120709" {:name "sosy mokre"},
   "120710" {:name "sosy w proszku"},
   "120711" {:name "warzywa konserwowe"},
   "120712" {:name "zupy"
             :max-margin 0.30},
   "1208"   {:name "kawa"
             :max-margin 0.23},
   "120801" {:name "kawy mielone"},
   "120802" {:name "kawy rozpuszczalne"},
   "120803" {:name "kawy ziarniste"},
   "1209"   {:name "kulinaria słodka"
             :max-margin 0.30},
   "120901" {:name "bakalie pakowane"},
   "120902" {:name "desery w proszku"},
   "120903" {:name "dodatki do ciast"},
   "120904" {:name "owoce w puszkach"},
   "1210"   {:name "produkty spożywcze podstawowe"
             :max-margin 0.15},
   "121001" {:name "cukier"},
   "121002" {:name "kasza"},
   "121003" {:name "makaron"},
   "121004" {:name "mąka"},
   "121005" {:name "ryż"},
   "121006" {:name "zabielacze do kawy"},
   "1211"   {:name "słodycze"
             :max-margin 0.25},
   "121101" {:name "batony"},
   "121102" {:name "bombonierki"},
   "121103" {:name "chałwa i sezamki"},
   "121104" {:name "ciastka"
             :max-margin 0.27},
   "121105" {:name "cukierki pakowane"
             :max-margin 0.28},
   "121106" {:name "czekolady"
             :max-margin 0.25},
   "121107" {:name "gumy do żucia"},
   "121108" {:name "lizaki"},
   "121109" {:name "słodycze luz"
             :max-margin 0.27},
   "121111" {:name "słodycze świąteczne"},
   "1212"   {:name "dania dla niemowląt"},
   "121201" {:name "dania dla niemowląt"},
   "13"     {:name "dla zwierząt"
             :max-margin 0.28},
   "1301"   {:name "karma i akcesoria dla zwierząt"},
   "130102" {:name "karma dla kotów"},
   "130103" {:name "karma dla psów"},
   "130105" {:name "piasek, żwirek"},
   "14"     {:name "ogólna migracja"},
   "1401"   {:name "ogólna migracja"},
   "140101" {:name "ogólna migracja"}})

(defn get-level [level]
  (into {}
        (comp
         (keep (fn [[k {:keys [name]}]]
                 (when (= (* 2 (inc level)) (count k))
                   {k name}))))
        by-id))

(def parents
  (get-level 0))


(defn id->level [id]
  (/ (- (count id) 2) 2))


(defn dec-level [id]
  (e/get-substring id 0 (- (count id) 2)))


(defn margin [id]
  (loop [id id]
    (when (seq id)
      (if-let [margin (get-in by-id [id :max-margin])]
       margin
       (recur (dec-level id))))))

(defn ancestors [idx]
  (into {}
        (keep (fn [[k {:keys [name]}]]
                  (when (and (str/starts-with? k idx)
                             (= (count k) (+ (count idx) 2)))
                    {k name})))
        by-id))

(defn tree
  ([idx]
   (when idx
     {:idx      idx
      :name     (get-in by-id [idx :name])
      :children (mapv (fn [[k _]] (tree k)) (ancestors idx))}))
  ([]
   (mapv (fn [[k _]] (tree k)) parents)))

(def last-name
  (e/memoize
   (fn [category-id]
     (get-in by-id [category-id :name]))))

(def full-name
  (e/memoize
   (fn [category-id]
     (when (>= (count category-id) 2)
       (str/join " - " (filter identity (flatten (cons (full-name (apply str (drop-last 2 category-id))) [(get-in by-id [category-id :name])]))))))))
