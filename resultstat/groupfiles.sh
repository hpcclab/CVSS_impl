cd numbers_test
for datei in *_wcodec*.txt; 
do mkdir -p -- "${datei%%_wcodec*}" && 
    mv -- "$datei" "${datei%%_wcodec*}"
 done
