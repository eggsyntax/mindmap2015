(ns mindmap.io)

; Namespace for various io methods (initially output to file, possibly others later).

; We ought to be able to handle all or nearly all of this with slurp and spit,
; only varying what kind of reader/writer we hand it.
; See https://clojuredocs.org/clojure.java.io/reader
;     https://clojure.github.io/clojure/clojure.java.io-api.html

; Known I/O targets (just because it makes me feel happy about clj :) ):
; array-of-bytes, array-of-characters, java.io.BufferedInputStream,
; java.io.BufferedOutputStream, java.io.BufferedReader, java.io.BufferedWriter,
; java.io.File, java.io.InputStream, java.io.OutputStream, java.io.Reader,
; java.io.Writer, java.lang.String, java.net.Socket, java.net.URI, java.net.URL,
; Object

(defprotocol IOProtocol
  "Protocol for providing an I/O target suitable for writing/reading.")


