for f in BenchmarkInput/*
do
if [ -d $f ]; then
    echo "ignore " $f
else
    java -jar CVSS.jar run $f nuSimConfigFIFO.properties
    java -jar CVSS.jar run $f nuSimConfigFair.properties
    java -jar CVSS.jar run $f nuSimConfigDL.properties
fi
done
