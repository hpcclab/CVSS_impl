To be updated,

can quickly test run all experiment using run.sh,
results will be output to resultstat/full and resultstat/number folder


general usage of the Jar is to call
 java -jar CVSS.jar run BENCHMARKFILE CONFIGFILE
for example,
 java -jar CVSS.jar run test2400r_180000_10000_3000_s7.txt config4.xml
use BenchmarkInput/test2400r_180000_10000_3000_s7.txt as test case
and config/config4.xml as configuration.


profile folder contain Time Estimator's data.

Benchmark input were generated using 
java -jar CVSS.jar makeconfig NUMBER_OF_GOPS
