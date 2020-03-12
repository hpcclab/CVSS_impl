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
Cons_raw=[[357,268,311,468,313,47,361,190,230,186,486,81,412,590,111,519,175,395,135,103,585,747,353,231,180,336,86,251,143,452],[1280,680,1272,1314,1341,1257,1232,925,1072,910,1332,1271,848,1340,1080,1060,1174,959,1007,1201,1358,1339,1199,1102,1131,1292,1334,1228,1259,1063],[1819,1862,1804,1892,1883,1838,1860,1763,1872,1655,1872,1811,1847,1854,1851,1826,1881,1822,1758,1749,1885,1836,1859,1717,1824,1845,1844,1895,1815,1798],[2278,2366,2318,2327,2303,2237,2286,2282,2295,2197,2374,2216,2345,2336,2330,2285,2349,2314,2255,2172,2327,2287,2305,2248,2304,2282,2362,2346,2234,2279]]

#Aggressive
Agg_raw=[[351,313,286,574,248,58,365,176,242,202,461,77,413,616,95,518,160,345,168,78,632,711,281,314,152,294,88,271,385,404],[1282,672,1293,1272,1341,1227,1272,1048,1044,906,1319,1184,902,1328,1062,1058,1128,940,1053,1220,1343,1322,1306,1074,1128,1325,1314,1247,1299,1053],[1802,1812,1800,1820,1819,1772,1806,1770,1848,1635,1828,1788,1813,1792,1791,1776,1821,1794,1728,1728,1820,1805,1846,1659,1765,1806,1796,1855,1798,1771],[2214,2312,2001,2217,2216,2127,1885,2192,2243,2117,2198,2117,2256,2224,2227,2200,2241,2222,2186,2161,2241,2204,2208,2195,2187,2214,2224,2246,2173,2205]]

#Adaptive
Adapt_raw=[[330,290,196,490,307,47,349,106,206,189,457,119,460,590,130,441,168,337,145,110,610,700,293,270,144,313,79,268,302,403],[1297,683,1279,1253,1345,1258,1302,1063,1101,948,1337,1262,860,1338,1050,1062,1085,969,967,1197,1358,1339,1242,1126,1070,1282,1310,1227,1266,991],[1831,1870,1804,1899,1873,1764,1874,1773,1885,1684,1874,1799,1852,1856,1840,1810,1866,1820,1764,1805,1906,1820,1853,1716,1817,1839,1858,1897,1811,1781],[2292,2353,2331,2323,2324,2239,2312,1884,2322,2180,2387,2017,2343,2360,2343,2300,2375,2312,2255,2206,2369,2284,2302,2260,2297,2311,2384,2343,2266,2293]]

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


