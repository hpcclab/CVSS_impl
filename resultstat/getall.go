package main

import (
    "os"
	"fmt"
	//"time"
    "log"
    "io/ioutil"
    "strings"
    "strconv"
    "math"
    "sort"
)
//concat 50 files into 1 files, and also to a summery.txt
func concat(inp string){
    count := make(map[string]int)  
    meanExeT := make(map[string]int)
    var keys []string
    eachExeT := make(map[string][]float64)  
    meanMiss := make(map[string]int)  
    meanMisspercent := make(map[string]float64)  
    eachMiss := make(map[string][]float64) 
    eachMisspercent := make(map[string][]float64) 
    SDexet := make(map[string]float64)
    SDmiss := make(map[string]float64)
    SDmisspercent := make(map[string]float64)  
    meanDone := make(map[string]int) 
    file, err := os.Open(inp)
    splitS := strings.Split(inp,"/")
    fName :=splitS[1]
	if err != nil {
		log.Fatalf("failed opening directory: %s", err)
	}
	defer file.Close()

    fileO, err2 := os.Create("sum/"+fName+".txt")
	if err2 != nil {
		log.Fatalf("failed opening directory: %s", err2)
	}
    defer fileO.Close()
	list,_ := file.Readdirnames(0) // 0 to read all files and folders
	for _, name := range list {
        b, err := ioutil.ReadFile(inp+"/"+name)
        if err != nil {
            fmt.Println(err)
        }
        str := string(b)
		fmt.Fprint(fileO,name +" , "+ str)
        //store data to be summerize
        category :=strings.Split(name,"_s")[0]
        var strsplit[] string =strings.Split(str," , ")
        //fmt.Println(strsplit[4])
        //ExeT
        val,_ :=strconv.Atoi( strings.Split(strsplit[4],"\n")[0] )
        //fmt.Println(val)
        meanExeT[category]+=val    
        eachExeT[category]= append(eachExeT[category], float64(val)) 
        //mean miss
        val2,_ :=strconv.Atoi(strsplit[3])
        val4,_ :=strconv.Atoi(strsplit[1])
        meanMiss[category]+=val2    
        eachMiss[category]= append(eachMiss[category], float64(val2)) 
        meanMisspercent[category]+=float64(val2)/float64(val4)
        eachMisspercent[category]= append(eachMisspercent[category], meanMisspercent[category]) 
        meanDone[category]+=val4 
        count[category]+=1
	}
    // summerize
    file3, err3 := os.Create("sum/summerize_"+fName+".txt")
	if err3 != nil {
		log.Fatalf("failed opening directory: %s", err3)
	}
    defer file3.Close()

    for k,_ :=range meanExeT{
        keys=append(keys,k)

    }
    sort.Strings(keys)

    for _,k := range keys {
    fmt.Println(k)
        meanExeT[k]  = meanExeT[k]/count[k]
        meanMiss[k]  = meanMiss[k]/count[k]
        meanMisspercent[k]  = meanMisspercent[k]*100.0/float64(count[k])        
        //find SD
        for _,j := range eachExeT[k]{
            SDexet[k] +=math.Pow(j-float64(meanExeT[k]),2)/float64(count[k])
        }
        for _,j := range eachMiss[k]{
            SDmiss[k] +=math.Pow(j-float64(meanMiss[k]),2)/float64(count[k])         
            //fmt.Println(j)
        }
        for _,j := range eachMiss[k]{
            SDmisspercent[k]+=math.Pow(j-float64(meanMisspercent[k]),2)/float64(count[k])           
            //fmt.Println(j)
        }
        SDexet[k]= math.Sqrt(SDexet[k])
        SDmiss[k]=math.Sqrt(SDmiss[k])
        SDmisspercent[k]=math.Sqrt(SDmisspercent[k])
        //fmt.Println(SDmiss)
        fmt.Fprintln(file3,k+" , "+strconv.Itoa(meanExeT[k] )+" , "+strconv.FormatFloat(SDexet[k], 'f', 6, 64)+" , "+strconv.Itoa(meanMiss[k])+" , "+strconv.FormatFloat(SDmiss[k], 'f', 6, 64)+" , "+strconv.Itoa(meanDone[k]/count[k]))
    }
    //exeT
    fmt.Fprint(file3,"ExeT=(")
    for _,k := range keys {
        fmt.Fprint(file3,meanExeT[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
    //SD_exeT
    fmt.Fprint(file3,"SD_ExeT=(")
    for _,k := range keys {
        fmt.Fprint(file3,SDexet[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
    //miss
    fmt.Fprint(file3,"miss=(")
    for _,k := range keys {
        fmt.Fprint(file3,meanMiss[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
    //SD_miss
    fmt.Fprint(file3,"SD_miss=(")
    for _,k := range keys {
        fmt.Fprint(file3,SDmiss[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
    //misspercent
    fmt.Fprint(file3,"mean_misspercent=(")
    for _,k := range keys {
        fmt.Fprint(file3,meanMisspercent[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
    //SD_misspercent
    fmt.Fprint(file3,"SD_misspercent=(")
    for _,k := range keys {
        fmt.Fprint(file3,SDmisspercent[k])
        fmt.Fprint(file3,",")
    }
    fmt.Fprintln(file3,")")
}
// error rate = SD/ sqrt(n) -> n=10 here

func main() {

    folders := [9]string{"finished/merge_Deadline/numbers/","finished/merge_Urgency/numbers/","finished/merge_Unsort/numbers/","finished/nomerge_Deadline/numbers/","finished/nomerge_Urgency/numbers/","finished/nomerge_Unsort/numbers/","finished/alwaysmerge_Deadline/numbers/","finished/alwaysmerge_Urgency/numbers/","finished/alwaysmerge_Unsort/numbers/"}
    for i:=0; i<9; i+=1 {	
        concat(folders[i])
    }

    //concat("finished/alwaysmerge_Unsort/numbers/");
	//fmt.Println("The time is", time.Now())
}

