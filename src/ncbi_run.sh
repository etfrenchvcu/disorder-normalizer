#!/bin/bash
for i in `seq 1 10`;
do
	echo $i
	java tool.Main ../ncbi-data/training ../ncbi-data/test ../ncbi-data/TERMINOLOGY.txt $i
done

