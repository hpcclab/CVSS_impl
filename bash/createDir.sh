#!/bin/bash

outdir=$1
absPath=$2
videoname=$3

mkdir $absPath/web/$outdir
cp $absPath/repositoryvideos/$videoname/out.m3u8 $absPath/web/$outdir
