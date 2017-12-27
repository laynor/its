#!/bin/bash -e

keysymdef_url=http://cgit.freedesktop.org/xorg/proto/xproto/plain/keysymdef.h
keysymdef=$(mktemp)

curl -L $keysymdef_url -o $keysymdef
# gcc -fpreprocessed -dD -E $keysymdef

(
echo "
;; 
;; This file is automatically translated from X.Org's xproto/keysymdef.h
;; Please, do not update this file with your hands, run $(basename "$0").
;; 

(ns its.keysyms)

(def keysims {
"

grep "^#define" $keysymdef | sed -r '
  s/#ifdef\s+/\/\/ Group /
  s/#endif.*//
  s/#define\s+([^ ]+)(\s+)([^ ]+)\s*\/\*\s*([^\*]+[^ ])\s*\*\//  :\1\2{ :code \3 :description "\4" },/
  s/(\b)U\+([0-9A-F]+)(\b)/\1(\\u\2)\3/i
  s/#define\s+([^ ]+)(\s+)([^ ]+)/  :\1\2{ :code \3, :description nil },/
  #s/#define\s+([^ ]+)(\s+[^ ]+)/  :\1\2,/
'

echo -n '
  :NoSymbol 0
})'

) > "$(dirname "$0")/src/its/keysyms.cljs"

rm $keysymdef
