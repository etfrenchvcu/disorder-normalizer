#!/bin/bash
for i in `seq 1 5`;
do
	echo $i
	java tool.Main ../n2c2-data/train ../n2c2-data/test ../ncbi-data/TERMINOLOGY.txt $i
done

