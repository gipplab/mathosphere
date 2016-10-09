#!/bin/bash
USAGE="Usage: convert folderToRecurse fileList maxRes output"
COUNTER=0
relevantNames=$2
num=$3
out=$4
recurse() {
 for i in "$1"/*;do
	if [ -d "$i" ];then
		echo "dir: $i"
		COUNTER=$[$COUNTER +1]
		/data/scripts/convert "$i" "$relevantNames" "$num" "$out$COUNTER" &>"$COUNTER".log &
	fi
 done
}

recurse $1
