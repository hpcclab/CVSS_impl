cd task
for datei in *_start0*.txt; 
do mkdir -p -- "${datei%%_start0*}" && 
    mv -- "$datei" "${datei%%_start0*}"
 done
