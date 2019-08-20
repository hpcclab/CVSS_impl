for f in BenchmarkInput/*
do
if [ -d $f ]; then
    echo "ignore " $f
else
    java -jar CVSS.jar run $f simconfig_nomerge.properties
    java -jar CVSS.jar run $f simconfig_adaptive.properties
    java -jar CVSS.jar run $f simconfig_aggressivemerge.properties
    java -jar CVSS.jar run $f simconfig_consideratemerge.properties
    java -jar CVSS.jar run $f simconfig_positionfind.properties
fi
done
