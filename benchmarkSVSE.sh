for f in BenchmarkInput/*
do
if [ -d $f ]; then
    echo "ignore " $f
else
    java -jar CVSS.jar run $f nuSimConfig FIFO.properties
    java -jar CVSS.jar run $f nuSimConfigFair.properties
    java -jar CVSS.jar run $f nuSimConfigDL.properties
fi
done
