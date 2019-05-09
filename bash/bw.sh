#!/bin/bash
path=$1
codec=$2
outdir=$3
filename=$4
ffmpeg -hide_banner -y -i $path -vf hue=s=0 -c:a copy $outdir/$filename 
chmod 775 $outdir/$filename

