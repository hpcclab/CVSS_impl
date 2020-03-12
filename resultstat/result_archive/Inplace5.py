import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
import math
def getMeanAndCI(ontimes,i):
    n, min_max, mean, var, skew, kurt = stats.describe(ontimes)
    std=math.sqrt(var)
    R = stats.norm.interval(0.95,loc=mean,scale=std/math.sqrt(i)) #definition's way
    #R = stats.norm.interval(0.05,loc=mean,scale=std) #dr.Amini's dropbox file way
    diff=mean-R[0]
    print("mean="+str(mean))
    return mean,diff
    #ci=diff/int(i)*100 #dr.Amini's dropbox file way
    #return ci #dr.Amini's dropbox file way

def insMeanAndCI(mean,CI,raw,count,l):
    cin=getMeanAndCI(raw[l],count)
    mean.append(cin[0])
    CI.append(cin[1])

#function specific to each graph, if it need some normalize    
def normalizeSubstract(oldlist,baseline):
    newlist=[]
    sequence=0
    for i in range(len(baseline)):
        newlist.append([])
        for j in range(len(baseline[i])):
            newlist[i].append(baseline[i][j]-oldlist[i][j])
            #print("newSeq")
    return newlist

##########start data section
#dump raw data here, so we can calculate both average and confidence interval


#Head=[1.0, 1.4, 1.7, 1.9, 2.0, 2.1, 2.3, 2.6, 3.0, 3.5, 4.0]

#Conservative
Cons_raw=[[343,313,274,510,346,42,326,137,266,204,489,131,410,591,109,478,166,354,190,94,597,740,311,242,135,395,78,272,359,388],[1270,668,1291,1332,1336,1200,1300,1247,1065,1001,1333,1262,824,1353,1054,1047,1075,917,1023,1247,1196,1321,1293,1081,1214,1330,1312,1258,1245,1063],[1811,1841,1802,1847,1830,1792,1823,1771,1873,1682,1845,1796,1766,1819,1819,1794,1834,1803,1763,1747,1861,1816,1838,1648,1805,1819,1805,1871,1819,1741],[2243,2330,2285,2288,2225,2189,2272,2247,2263,2152,2336,2176,2284,2297,2297,2235,2295,2262,2203,2174,2291,2238,2239,2227,2218,2250,2281,2309,2202,2232]]

#Aggressive
Agg_raw=[[368,298,307,556,469,40,340,175,241,146,442,119,463,641,138,513,168,330,137,138,584,635,316,255,106,370,83,269,411,367],[1254,772,1259,1325,1346,1248,1237,1199,1042,688,1323,1257,800,1329,1073,1047,1089,918,1032,1187,1351,1321,1324,1103,1173,1331,1314,1270,1277,1034],[1807,1841,1777,1827,1813,1767,1800,1774,1849,1615,1830,1790,1798,1817,1801,1766,1852,1797,1745,1734,1849,1806,1841,1664,1774,1779,1806,1861,1813,1778],[2207,2299,2256,2209,2220,2160,2191,2205,2252,2121,2263,2147,2247,2218,2247,2180,2246,2223,2175,2143,2236,2203,2198,2179,2199,2221,2220,2262,2182,2199]]

#Adaptive
Adapt_raw=[[328,331,297,518,447,74,310,131,237,294,443,74,423,609,110,513,175,326,134,84,647,670,304,294,142,342,87,214,305,393],[1270,779,1313,1326,1342,1238,1297,994,1090,909,1329,1166,845,1357,1014,1037,1104,918,1053,1235,1349,1322,1321,1133,1145,1329,1310,1251,1288,1165],[1811,1866,1802,1852,1841,1781,1812,1794,1849,1678,1869,1802,1796,1826,1827,1796,1846,1784,1755,1745,1844,1803,1860,1662,1810,1792,1803,1856,1805,1765],[2233,2322,2287,2259,2238,2173,2256,2212,2258,2136,2289,2180,2295,2283,2277,2234,2278,2260,2203,2139,2300,2239,2203,2205,2217,2272,2280,2306,2215,2216]]

#Nomerge
Nomerge_raw=[[447,337,322,576,355,113,377,248,286,229,460,89,447,756,129,523,157,390,230,151,793,720,290,345,130,357,75,300,320,430],[1368,1063,1382,1421,1430,1385,1393,1235,1200,1079,1410,1361,836,1435,1120,1115,1272,1047,1029,1326,1441,1432,1406,1222,1335,1442,1378,1407,1360,1116],[1914,1977,1930,1969,1978,1912,1945,1863,1960,1799,1951,1876,1929,1930,1930,1897,1945,1907,1907,1862,1974,1938,1959,1814,1928,1948,1910,1973,1932,1840],[2409,2495,2455,2459,2473,2389,2413,2435,2454,2368,2465,2335,2484,2452,2440,2399,2454,2409,2446,2352,2470,2442,2430,2403,2403,2442,2443,2436,2404,2421]]









#create array for mean, and confidence interval
Cons=[]
Agg=[]
Adapt=[]
#
Cons_ci=[]
Agg_ci=[]
Adapt_ci=[]
#calculate CI and mean
column=len(Cons_raw)

######## normalize the data
Cons_raw=normalizeSubstract(Cons_raw,Nomerge_raw)
Agg_raw=normalizeSubstract(Agg_raw,Nomerge_raw)
Adapt_raw=normalizeSubstract(Adapt_raw,Nomerge_raw)

#find mean and stdErr
for l in range(column):
    #format: mean array, CI array, raw data, how many trials in the raw data
    #don't forget to change number 30 to number of trials you actually test
    insMeanAndCI(Cons,Cons_ci,Cons_raw,30,l)
    insMeanAndCI(Agg,Agg_ci,Agg_raw,30,l)
    insMeanAndCI(Adapt,Adapt_ci,Adapt_raw,30,l)
    #insMeanAndCI(Nomerge,Nomerge_ci,Nomerge_raw,30,l)
###########################################
# initiation
fig, ax = plt.subplots()
axes = plt.gca()
############
#your main input parameters section
n_groups =3 # number of different data to plot, can change here without removing data
xlabel='Oversubscription Level (#Tasks)'
ylabel='DMR saving against non merging'
n_point = column # number of x ticks to use, must match number of xtick and number of data point
xtick=('1k','1.5k','2k','2.5k')
labels=['Conservative',"Adaptive",'Aggressive']
legendcolumn= 2 #number of column in the legend
data=[Cons,Adapt,Agg]
yerrdata=[Cons_ci,Adapt_ci,Agg_ci]
axes.set_ylim([0,600]) #y axis scale
ticklabelsize=18
axislabelfontsize=16

############
#auto calculated values and some rarely change config, can also overwrite
axes.set_xlim([-0.5, len(xtick)-0.5]) #y axis
font = {'family' : 'DejaVu Sans',
        #'weight' : 'bold',
        'size'   : 16 }
bar_width =1.0/(n_groups+2) 
edgecols=['royalblue','forestgreen','red','mediumblue','orange','pink','limegreen','lightblue','darkgreen'] #prepared 9 colors
#hatch_arr=[".","x"]
hatch_arr=["////","ooo",".\\\\\\","----","**","xxx","+++",".///","////////"] #prepared 9 hatch style
opacity = 1 #chart opacity
offsetindex=(n_groups-1)/2.0


############
#plot section
plt.rc('font', **font)
index = np.arange(n_point)
print("data"+str(data))
print("yerrdata"+str(yerrdata))
for i in range(0,n_groups):
    #draw internal hatch, and labels
    plt.bar(index - (offsetindex-i)*bar_width, data[i], bar_width,
                     alpha=opacity,                 
                     hatch=hatch_arr[i],
                    #color=edgecols[i],
                	 color='white',
		     edgecolor=edgecols[i],
             label=labels[i],
		     lw=1.0,
		     zorder = 0)
    #draw black liner and error bar
    plt.bar(index - (offsetindex-i)*bar_width, data[i], bar_width, yerr =
		    yerrdata[i],                              
                    color='none',
		    error_kw=dict(ecolor='black',capsize=3),
                    edgecolor='k',
		    zorder = 1,
		    lw=1.0)

plt.tick_params(axis='both', which='major', labelsize=ticklabelsize)
plt.tick_params(axis='both', which='minor', labelsize=ticklabelsize)
plt.xlabel(xlabel,fontsize=axislabelfontsize)
plt.ylabel(ylabel,fontsize=axislabelfontsize)
#plt.title('Execution time (deadline sorted batch queue)') #generally, we add title in latex
ax.set_xticks(index)
ax.set_xticklabels(xtick)
ax.legend(loc='upper center', prop={'size': 10},bbox_to_anchor=(0.5, 1.00), shadow= True, ncol=legendcolumn)

plt.tight_layout()
plt.show()


