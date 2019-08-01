for f in BenchmarkInput/*
do
if [ -d $f ]; then
    echo "ignore " $f
else
    java -jar CVSS.jar run $f simconfig_nomerge.properties
    java -jar CVSS.jar run $f simconfig_merge.properties
    java -jar CVSS.jar run $f simconfig_consideratemerge.properties
fi
done
