ar=[1920,1080,768,1990,4192,262144,800,12345,678,521,50,167,1,251,68,6,333,1048575,81,7]
context=[2000,2200,2400,2600,2800,3000,3200]
for i in ar:
    for j in context:
            print "java -jar CVSS.jar run test"+str(j)+"r_180000_10000_3000_s"+str(i)+".txt config9.xml"
