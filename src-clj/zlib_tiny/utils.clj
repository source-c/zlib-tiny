(ns zlib-tiny.utils)

(defn hexlify ^String [^bytes b]
  (->> (map #(format "%02x" %) b)
       (apply str)))


(defn unhexlify ^bytes [^String s]
  (->> (partition 2 s)
       (map #(Integer/parseInt (apply str %) 16))
       byte-array))
