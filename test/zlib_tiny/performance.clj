(ns zlib-tiny.performance
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn ^"[B" generate-test-data
  "Generate test data of specified size"
  [size]
  (byte-array (repeatedly size #(rand-int 256))))

(deftest performance-test
  (testing "CRC32C performance with unrolled loop"
    (let [small-data (generate-test-data 100)
          medium-data (generate-test-data 10000)
          large-data (generate-test-data 1000000)]
      (println "\nCRC32C Performance:")
      (println "Small data (100 bytes):" (time (crc32c small-data)))
      (println "Medium data (10KB):" (time (crc32c medium-data)))
      (println "Large data (1MB):" (time (crc32c large-data)))))

  (testing "CRC64 performance with unrolled loop"
    (let [small-data (generate-test-data 100)
          medium-data (generate-test-data 10000)
          large-data (generate-test-data 1000000)]
      (println "\nCRC64 Performance:")
      (println "Small data (100 bytes):" (time (crc64 small-data)))
      (println "Medium data (10KB):" (time (crc64 medium-data)))
      (println "Large data (1MB):" (time (crc64 large-data)))))

  (testing "Streaming API for large data"
    (let [test-data (generate-test-data 100000)
          bais (ByteArrayInputStream. test-data)
          baos (ByteArrayOutputStream.)]
      (println "\nStreaming API Performance (100KB):")
      (time (copy-compress bais baos gzip-stream))
      (println "Compressed size:" (.size baos))

      (let [compressed-data (.toByteArray baos)
            bais2 (ByteArrayInputStream. compressed-data)
            baos2 (ByteArrayOutputStream.)]
        (time (copy-decompress bais2 baos2 gunzip-stream))
        (println "Decompressed size:" (.size baos2))
        (is (= (alength test-data) (.size baos2)) "Decompressed size should match original")))))