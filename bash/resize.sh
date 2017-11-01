#!/bin/bash
path=$1
width=$2
height=$3
outdir=$4
filename=$5

mkdir $outdir
ffmpeg -i $path -s $width:$height -c:a copy $outdir/$filename 
