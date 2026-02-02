(ns zlib-tiny.core
  (:import (java.util.zip InflaterInputStream
                          Inflater
                          GZIPInputStream
                          GZIPOutputStream
                          DeflaterInputStream
                          Deflater
                          ZipException
                          CRC32
                          Adler32)
           (zlib_tiny CRC32C CRC64)
           (org.apache.commons.io IOUtils)
           (java.security MessageDigest)
           (java.io ByteArrayInputStream
                    ByteArrayOutputStream
                    BufferedInputStream
                    InputStream)))

(def ^:private ^:const STREAM_MARK_LIMIT 512)
(def ^:private ^:const DEFAULT_BUFFER_SIZE 8192)

(def ^:private ^ThreadLocal buffer-pool
  (proxy [ThreadLocal] []
    (initialValue []
      (byte-array DEFAULT_BUFFER_SIZE))))

(defn- get-buffer 
  "Gets a reusable buffer from the thread-local pool"
  []
  (.get buffer-pool))

(defn str->bytes
  "Returns the encoding's bytes corresponding to the given string. If no
  encoding is specified, UTF-8 is used."
  [^String s & [^String encoding]]
  (.getBytes s ^String (or encoding "UTF-8")))

(defn bytes->str
  "Returns the String corresponding to the given encoding's decoding of the
  given bytes. If no encoding is specified, UTF-8 is used."
  [^bytes b & [^String encoding]]
  (String. b ^String (or encoding "UTF-8")))

(defn gunzip
  "Returns a gunzip'd version of the given byte array."
  [b]
  (when b
    (cond
      (instance? InputStream b)
      (GZIPInputStream. b)
      :else
      (IOUtils/toByteArray (GZIPInputStream. (ByteArrayInputStream. b))))))

(defn gzip
  "Returns a gzip'd version of the given byte array."
  [b]
  (when b
    (let [baos (ByteArrayOutputStream.)
          gos (GZIPOutputStream. baos ^int DEFAULT_BUFFER_SIZE)
          buffer (get-buffer)
          bis (ByteArrayInputStream. b)]
      (loop []
        (let [n (.read bis buffer 0 DEFAULT_BUFFER_SIZE)]
          (when (pos? n)
            (.write gos buffer 0 n)
            (recur))))
      (.close gos)
      (.toByteArray baos))))

(defn force-byte-array
  "force b as byte array if it is an InputStream, also close the stream"
  ^bytes [b]
  (if (instance? InputStream b)
    (try (IOUtils/toByteArray ^InputStream b)
         (finally (.close ^InputStream b)))
    b))

(defn inflate
  "Returns a zlib inflate'd version of the given byte array or InputStream."
  [b]
  (when b
    ;; This weirdness is because we don't know about what kind of deflation
    ;; sender using, so we try one way, then if that doesn't work, reset and
    ;; try the other way
    (let [stream (BufferedInputStream. (if (instance? InputStream b)
                                         b
                                         (ByteArrayInputStream. b)))
          _ (.mark stream STREAM_MARK_LIMIT)
          probe-inflater (Inflater.)
          readable? (try
                      (.read (InflaterInputStream. stream probe-inflater))
                      true
                      (catch ZipException _ false)
                      (finally (.end probe-inflater)))]
      (.reset stream)
      (if readable?
        (InflaterInputStream. stream)
        (InflaterInputStream. stream (Inflater. true))))))

(defn deflate
  "Returns deflate'd version of the given byte array."
  ([b]
   (when b
     (IOUtils/toByteArray (DeflaterInputStream. (ByteArrayInputStream. b)))))
  ([b level]
   (let [deflater (Deflater. level)
         ba (IOUtils/toByteArray (DeflaterInputStream. (ByteArrayInputStream. b)
                                                       deflater))]
     (.end deflater)
     ba)))

(comment "ZLib Example"
         (bytes->str (force-byte-array (inflate (deflate (str->bytes "test it!"))))))
(comment "GZip Example"
         (bytes->str (gunzip (gzip (str->bytes "test it!")))))

(defmacro wrap-crc [impl b]
  `(let [o# (new ~impl)]
     (.update o# ~b)
     (.getValue o#)))

(defn crc32
  ^Long [^bytes b]
  (wrap-crc CRC32 b))

(defn crc32c
  ^Long [^bytes b]
  (wrap-crc CRC32C b))

(defn crc32c-accelerated?
  "Returns true if CRC32C is using JDK hardware acceleration (Java 9+).
   Useful for diagnostics and performance testing."
  []
  (CRC32C/isHardwareAccelerated))

(defn adler32
  ^Long [^bytes b]
  (wrap-crc Adler32 b))

(defn crc64
  ^Long [^bytes b]
  (wrap-crc CRC64 b))

(defmacro wrap-digest [algn b]
  `(let [o# ^MessageDigest (MessageDigest/getInstance ~algn)]
     (.update o# ~b)
     (.digest o#)))

(defn md5
  ^bytes [^bytes b]
  (wrap-digest "MD5" b))


(defn sha-1
  ^bytes [^bytes b]
  (wrap-digest "SHA-1" b))

(defn sha-256
  ^bytes [^bytes b]
  (wrap-digest "SHA-256" b))

(defn sha-384
  ^bytes [^bytes b]
  (wrap-digest "SHA-384" b))

(defn sha-512
  ^bytes [^bytes b]
  (wrap-digest "SHA-512" b))

;; Streaming API for large data

(defn deflate-stream
  "Returns a DeflaterInputStream for streaming deflation.
   Useful for large files that shouldn't be loaded entirely into memory."
  ([^InputStream input-stream]
   (DeflaterInputStream. input-stream))
  ([^InputStream input-stream level]
   (DeflaterInputStream. input-stream (Deflater. level))))

(defn inflate-stream
  "Returns an InflaterInputStream for streaming inflation.
   Useful for large files that shouldn't be loaded entirely into memory."
  [^InputStream input-stream]
  (let [stream (BufferedInputStream. input-stream)
        _ (.mark stream STREAM_MARK_LIMIT)
        probe-inflater (Inflater.)
        readable? (try
                    (.read (InflaterInputStream. stream probe-inflater))
                    true
                    (catch ZipException _ false)
                    (finally (.end probe-inflater)))]
    (.reset stream)
    (if readable?
      (InflaterInputStream. stream)
      (InflaterInputStream. stream (Inflater. true)))))

(defn gzip-stream
  "Returns a GZIPOutputStream for streaming gzip compression.
   Useful for large files that shouldn't be loaded entirely into memory."
  ^GZIPOutputStream
  ([^java.io.OutputStream output-stream]
   (GZIPOutputStream. output-stream ^int DEFAULT_BUFFER_SIZE))
  ([^java.io.OutputStream output-stream ^Integer buffer-size]
   (GZIPOutputStream. output-stream ^int buffer-size)))

(defn gunzip-stream
  "Returns a GZIPInputStream for streaming gzip decompression.
   Useful for large files that shouldn't be loaded entirely into memory."
  ([^InputStream input-stream]
   (GZIPInputStream. input-stream ^int DEFAULT_BUFFER_SIZE))
  ([^InputStream input-stream buffer-size]
   (GZIPInputStream. input-stream ^int buffer-size)))

(defn copy-compress
  "Copies data from input-stream to output-stream with compression.
   Returns the number of bytes written."
  ^long [^InputStream input-stream ^java.io.OutputStream output-stream compress-fn]
  (let [^java.io.OutputStream compressed-stream (compress-fn output-stream)
        ^bytes buffer (get-buffer)]
    (try
      (loop [total (long 0)]
        (let [n (.read input-stream buffer 0 DEFAULT_BUFFER_SIZE)]
          (if (pos? n)
            (do
              (.write compressed-stream buffer 0 n)
              (recur (+ total n)))
            total)))
      (finally
        (.close compressed-stream)))))

(defn copy-decompress
  "Copies data from input-stream to output-stream with decompression.
   Returns the number of bytes written."
  ^long [^InputStream input-stream ^java.io.OutputStream output-stream decompress-fn]
  (let [^InputStream decompressed-stream (decompress-fn input-stream)
        ^bytes buffer (get-buffer)]
    (try
      (loop [total (long 0)]
        (let [n (.read decompressed-stream buffer 0 DEFAULT_BUFFER_SIZE)]
          (if (pos? n)
            (do
              (.write output-stream buffer 0 n)
              (recur (+ total n)))
            total)))
      (finally
        (.close decompressed-stream)))))
