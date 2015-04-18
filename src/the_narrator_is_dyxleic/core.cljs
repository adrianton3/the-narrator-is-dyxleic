(ns the-narrator-is-dyxleic.core
  (:require
    [clojure.browser.repl :as repl]
    [clojure.string :as str]
    [the-narrator-is-dyxleic.localstorage :as local-storage]
    [the-narrator-is-dyxleic.levels :as levels]
    ))

(enable-console-print!)

; =============================================================================

(defn get-text [id]
  (.-textContent (.getElementById js/document id)))

(defn set-text [id text]
  (set! (.-textContent (.getElementById js/document id)) text))

(def get-current (partial get-text "current"))

(def set-current (partial set-text "current"))

(defn set-line [before after]
  (set-text "text-before" before)
  (set-text "text-after" after))

(defn set-target [text]
  (set! (.-targetText js/window) text))

(defn get-target []
  (.-targetText js/window))

(def operations {
  "reverse" str/reverse
  "rotate" (fn [string]
             (str
               (last string)
               (subs string 0 (dec (count string)))))
  "push-a" (fn [string] (str string "a"))
  "push-b" (fn [string] (str string "b"))})

(defn check-win []
  (if (= (get-current) (get-target))
    (do (increment-level)
      (next-level))))

(defn clicked-on [text]
  (let [new-string ((get operations text) (get-current))]
    (set-current new-string)
    (check-win)))

(defn create-item [text]
  (let [item (.createElement js/document "li")]
    (set! (.-textContent item) text)
    (.addEventListener item "click" (fn [] (clicked-on text)))
    item))

(defn update-list [elements]
  (let [list (.getElementById js/document "operations")]
    (set! (.-innerHTML list) "")
    (doseq [element elements]
      (let [item (create-item element)]
        (.appendChild list item)))))

(defn increment-level []
  (local-storage/set-item! "level"
    (inc (js/parseInt (local-storage/get-item "level")))))

(def get-level (partial local-storage/get-item "level"))

(defn next-level []
  (let [{:keys [operations text-before text-after start target]}
        (levels/levels (get-level))]
    (update-list operations)
    (set-line text-before text-after)
    (set-current start)
    (set-target target)))

; =============================================================================

; initial run
(if-not (local-storage/get-item "level")
  (local-storage/set-item! "level" 0))

(next-level)