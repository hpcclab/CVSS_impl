import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
import math
import sys
import csv

def getMeanAndCI(ontimes,i):
    n, min_max, mean, var, skew, kurt = stats.describe(ontimes)
    std=math.sqrt(var)
    R = stats.norm.interval(0.95,loc=mean,scale=std/math.sqrt(i)) #definition's way
    #R = stats.norm.interval(0.05,loc=mean,scale=std) #dr.Amini's dropbox file way
    diff=mean-R[0]
    return mean,diff
    #ci=diff/int(i)*100 #dr.Amini's dropbox file way
    #return ci #dr.Amini's dropbox file way

def insMeanAndCI(mean,CI,raw,count):
    cin=getMeanAndCI(raw[l],count)
    mean.append(cin[0])
    CI.append(cin[1])

Ontime=0
Miss=0
#####################
#grab data section
#print "This is The arguments [1]", sys.argv[1]
with open(sys.argv[1]) as csv_file:
#with open("./numbers/"+"merge__Sortalways_mergeDeadline_test2000r_180000_10000_3000_s1.txt") as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        Ontime=int(row[0])
        Miss=int(row[2])
print(str(Ontime)+" "+str(Miss))
#####################
top=max(Ontime,Miss)*1.2
###########################################
# initiation
fig, ax = plt.subplots()
axes = plt.gca()
############
#your main input parameters section
n_groups = 2 # number of different data to plot, can change here without removing data
xlabel=''
ylabel='Number of Tasks'
n_point = 1 # number of x ticks to use, must match number of xtick and number of data point
xtick=('Total')
labels=['Miss','On-time']
legendcolumn= 1 #number of column in the legend
data=[Ontime,Miss]
#yerrdata=[MM_ci,MSD_ci,MMU_ci,MM_P_ci,MSD_P_ci,MMU_P_ci]
axes.set_ylim([0,top]) #y axis scale
ticklabelsize=18
axislabelfontsize=16

############
#auto calculated values and some rarely change config, can also overwrite
axes.set_xlim([-0.5, len(xtick)-0.5]) #y axis
font = {'family' : 'DejaVu Sans',
        #'weight' : 'bold',
        'size'   : 16 }
bar_width =1.0/(n_groups+2) 
edgecols=['red','forestgreen']
#hatch_arr=[".","x"]
hatch_arr=["////","ooo",".\\\\\\","----","**","xxx","+++",".///","////////"] #prepared 9 hatch style
opacity = 1 #chart opacity
offsetindex=(n_groups-1)/2.0


############
#plot section
plt.rc('font', **font)
index = np.arange(n_point)

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
    plt.bar(index - (offsetindex-i)*bar_width, data[i], bar_width, #yerr = yerrdata[i],                              
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
ax.legend(loc='upper center', prop={'size': 12},bbox_to_anchor=(0.5, 1.00), shadow= True, ncol=legendcolumn)

plt.tight_layout()
plt.show()


