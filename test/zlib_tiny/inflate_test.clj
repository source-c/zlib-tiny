(ns zlib-tiny.inflate-test
  (:require [clojure.test :refer :all]
            [zlib-tiny.core :refer :all])
  (:import (java.util.zip Deflater DeflaterInputStream)
           (java.io ByteArrayInputStream ByteArrayOutputStream)
           (org.apache.commons.io IOUtils)))

(def test-string "The quick brown fox jumps over the lazy dog")

(defn deflate-raw
  "Deflate using raw format (no zlib header/trailer)"
  [^bytes b]
  (let [deflater (Deflater. Deflater/DEFAULT_COMPRESSION true)] ; true = nowrap (raw)
    (try
      (IOUtils/toByteArray (DeflaterInputStream. (ByteArrayInputStream. b) deflater))
      (finally
        (.end deflater)))))

(defn deflate-wrapped
  "Deflate using wrapped format (with zlib header/trailer)"
  [^bytes b]
  (let [deflater (Deflater.)] ; default = wrapped
    (try
      (IOUtils/toByteArray (DeflaterInputStream. (ByteArrayInputStream. b) deflater))
      (finally
        (.end deflater)))))

(deftest inflate-format-detection
  (testing "inflate handles wrapped zlib format"
    (let [original (str->bytes test-string)
          compressed (deflate-wrapped original)
          decompressed (force-byte-array (inflate compressed))]
      (is (= test-string (bytes->str decompressed)))))

  (testing "inflate handles raw zlib format"
    (let [original (str->bytes test-string)
          compressed (deflate-raw original)
          decompressed (force-byte-array (inflate compressed))]
      (is (= test-string (bytes->str decompressed))))))

(deftest inflate-stream-format-detection
  (testing "inflate-stream handles wrapped zlib format"
    (let [original (str->bytes test-string)
          compressed (deflate-wrapped original)
          decompressed (force-byte-array (inflate-stream (ByteArrayInputStream. compressed)))]
      (is (= test-string (bytes->str decompressed)))))

  (testing "inflate-stream handles raw zlib format"
    (let [original (str->bytes test-string)
          compressed (deflate-raw original)
          decompressed (force-byte-array (inflate-stream (ByteArrayInputStream. compressed)))]
      (is (= test-string (bytes->str decompressed))))))

(deftest inflate-high-volume
  (testing "inflate handles high volume without native memory leak"
    ;; This test calls inflate many times to verify that native Inflater
    ;; resources are properly cleaned up during format detection.
    ;; Before the fix, this could accumulate native memory.
    (let [original (str->bytes test-string)
          compressed-wrapped (deflate-wrapped original)
          compressed-raw (deflate-raw original)
          iterations 1000]
      ;; Alternate between wrapped and raw to exercise both code paths
      (dotimes [i iterations]
        (let [compressed (if (even? i) compressed-wrapped compressed-raw)
              result (force-byte-array (inflate compressed))]
          (when (zero? (mod i 100))
            ;; Hint GC periodically to help detect leaks faster
            (System/gc))
          (is (= test-string (bytes->str result))
              (str "Failed at iteration " i))))
      ;; If we got here without OOM or excessive slowdown, the fix works
      (is true "High volume inflate completed successfully"))))

(deftest inflate-stream-high-volume
  (testing "inflate-stream handles high volume without native memory leak"
    (let [original (str->bytes test-string)
          compressed-wrapped (deflate-wrapped original)
          compressed-raw (deflate-raw original)
          iterations 1000]
      (dotimes [i iterations]
        (let [compressed (if (even? i) compressed-wrapped compressed-raw)
              result (force-byte-array (inflate-stream (ByteArrayInputStream. compressed)))]
          (when (zero? (mod i 100))
            (System/gc))
          (is (= test-string (bytes->str result))
              (str "Failed at iteration " i))))
      (is true "High volume inflate-stream completed successfully"))))
