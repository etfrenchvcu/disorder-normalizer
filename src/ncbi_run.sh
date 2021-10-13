#!/bin/bash
sh build.sh

for i in `seq 1 4`;
do
	echo $i
	java tool.Main ../ncbi-data/training ../ncbi-data/test ../ncbi-data/TERMINOLOGY.txt $i
done

