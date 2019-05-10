#!/bin/bash
path=$1
outdir=$2
filename=$3
ffmpeg -hide_banner -y -i $path -vcodec libx264 -acodec copy -vf hue=s=0 $outdir/$filename 
chmod 775 $outdir/$filename

