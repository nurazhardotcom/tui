(ns tui.stream
  "Streaming chunk assembly. Pure + concurrency-safe."
  (:require [clojure.string :as str]))

(defn append-chunk
  "Append one SSE/text chunk to buffer."
  [buf chunk]
  (str buf (or chunk "")))

(defn assemble
  "Fold sequential chunks."
  [chunks]
  (reduce append-chunk "" chunks))

(defn assemble-parallel
  "Assemble ordered chunks concurrently (pmap) — order preserved by index."
  [chunks]
  (->> (map-indexed vector chunks)
       (pmap (fn [[_ c]] (or c "")))
       (apply str)))

(defn done-marker? [line]
  (boolean (and (string? line) (str/includes? line "[DONE]"))))
