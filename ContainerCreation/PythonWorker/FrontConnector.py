#!/usr/bin/env python
import sys
import os
import pika
import TaskRequest_pb2
import time;
import pycvss.ffmpeg.bindings as args
import pycvss.ffmpeg.benchmark as benchmark

#RepDir="/home/c00251686/workspace/git-repos/CVSS_impl/PythonWorker/sampleRepo/"
#ExpDir="/home/c00251686/workspace/git-repos/CVSS_impl/PythonWorker/sampleOutput/"
###############################################################################
INITQUEUENAME="init_queue"
FEEDBACKQUEUENAME="" #feedback_queue, will init after get the message from INITQUEUENAME 
QUEUENAME="" #mq0 etc. after init
REFERENCETIME=""
RMQHOST="10.131.80.30"
MYID=0
RepDir="/share_dir/SVSE/sampleRepo/"
ExpDir="/share_dir/SVSE/sampleOutput/"
transcodingcount=0

#todo, this must be expandable...
cmdTranslatorTable={
'RESOLUTION':'-vf',
'FRAMERATE':'-r',
'BITRATE':'-b:v',
'CODEC':'-c:v',
'CUSTOM':'',
'custom':''
}
paramTranslatorTable={
'RESOLUTION':['scale=1920:1080','scale=640:360'],
'FRAMERATE':['60','24'],
'BITRATE':['3.4M','2.4M'],
'CODEC':['libx264','libvpx-vp9']
}

def cmdTranslate(operation):
    if cmdTranslatorTable.get(str(operation))!=None:
        return cmdTranslatorTable[operation]
    else:
        print("Unknown translateion look up "+operation)
        return "UNKNOWNOP"

def paramTranslate(operation,param):
    print(str(operation.upper())+" "+str(param))
    if operation.lower()=='custom':
        return param
    elif cmdTranslate(operation.upper())=="UNKNOWNOP":
        return "UNKNOWNPARAM"
    elif paramTranslatorTable.get(str(operation.upper()))[int(param)]!=None:
        return paramTranslatorTable.get(str(operation.upper()))[int(param)]
    else:
        print("Unknown translateion look up "+operation+" "+param)
        return "UNKNOWNPARAM"


def transcode(aRequest):
    global RepDir,ExpDir,transcodingcount
    #cmd,options,datasource
    if(aRequest.DataSource.find("_")>-1):
        videochoice=aRequest.DataSource.split("_")[0]
        segmentnum=aRequest.DataSource.split("_")[1]
        segmentName="video"+str(int(segmentnum))+".ts"
        DataTag=aRequest.DataTag
    else:
        print("Special message received ")
        return;
    inputfile=RepDir+videochoice+"/"+segmentName
    #outputfile=ExpDir+videochoice+"/"+segmentName
    foldername=videochoice+"_"+DataTag
    if not os.path.exists(os.path.join(ExpDir,foldername,)):
        try:
            access_rights = 0o777
            os.makedirs(os.path.join(ExpDir,foldername),access_rights)
            os.popen('cp '+os.path.join(RepDir,videochoice,'video.m3u8') +' '+os.path.join(ExpDir,foldername,'video.m3u8')) #copy manifest file
        except:
            print("Error found:", sys.exc_info()[0])
            pass
    print(inputfile+" Deadline="+str(aRequest.GlobalDeadline)+" ")
    tests = []
    #print("test="+str(tests))
    for eachop in aRequest.OPlist:
        #print("cmd="+str(eachop.Cmd))
        for eachparam in eachop.Parameter:
            #print("parameterchoice="+str(eachparam.subparameter))
            #print("cmd="+ "ffmpeg -y -i "+inputfile+" "+cmdTranslate(eachop.Cmd)+" "+paramTranslate(eachop.Cmd,int(eachparam.subparameter))+" "+outputfile)
            tests.append(benchmark.BTest(
            segmentName+" "+eachop.Cmd+paramTranslate(eachop.Cmd,eachparam.subparameter),
            lambda: args.generic_video_convert_args(
                os.path.join(RepDir,videochoice,segmentName),
                os.path.join(ExpDir,foldername,segmentName),
                cmd_=cmdTranslate(eachop.Cmd),
                option_=paramTranslate(eachop.Cmd,eachparam.subparameter)
                ),
            input_file_=os.path.join(RepDir,videochoice,segmentName),
            output_file_=os.path.join(ExpDir,videochoice,segmentName),
            ))

    ###now start to execute
    results = list()
    #print("results="+str(results))
###disable to test
    for test in tests:
        results.append(test.process())
    print("end"+str(transcodingcount))
    transcodingcount=transcodingcount+1
    return results

def report(results,aRequest):
    #reportback some data
    report= TaskRequest_pb2.TaskReport()
    report.completedTaskID=aRequest.TaskID
    report.workerNodeID=MYID
    report.executionTime=results[0][1]*1000 ###execution time
    #report.executionTime=20 ###test
    report.timeStamp=time.time()*1000-REFERENCETIME
    print("ref time="+str(REFERENCETIME)+" timeStamp="+str(report.timeStamp))
    report.theRequest.CopyFrom(aRequest)
    channel.basic_publish(    
        exchange='',
        routing_key=FEEDBACKQUEUENAME,
        body=report.SerializeToString(),
        properties=pika.BasicProperties(
            delivery_mode=2,  # make message persistent
    ))


def callback(ch, method, properties, body):
    parsedbody = TaskRequest_pb2.ServiceRequest()
    parsedbody.ParseFromString(body)
    print(parsedbody)
    result=transcode(parsedbody)
    report(result,parsedbody)
    ch.basic_ack(delivery_tag = method.delivery_tag)

#main()
if __name__ == "__main__":
    if len(sys.argv)>2:
        print("Overwrite host addr with "+sys.argv[2])
        RMQHOST=sys.argv[2]
    if len(sys.argv)>1:
        print("I am worker number "+sys.argv[1])
        MYID=int(sys.argv[1])
    thecredential=pika.PlainCredentials('pworker','aweakpswd')
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RMQHOST,credentials=thecredential))
    channel = connection.channel()
    channel.queue_declare(queue=INITQUEUENAME)
    #get ONE message
    method_frame, header_frame, body = channel.basic_get(queue=INITQUEUENAME)
    if method_frame:
        #print(method_frame, header_frame, body)
        parsedbody=body.decode("utf-8").split(" ")
        print("MQ=",parsedbody[0],"feedbackqueue=",parsedbody[1])
        QUEUENAME=parsedbody[0]
        FEEDBACKQUEUENAME=parsedbody[1]
        REFERENCETIME=float(parsedbody[2])
        
        print("ref time="+str(REFERENCETIME))
        channel.basic_ack(method_frame.delivery_tag)
    else:
        print('No message returned')

    #2nd channel is not required at the moment
    #feedbackchannel = connection.channel() 
    #feedbackchannel.queue_declare(queue=FEEDBACKQUEUENAME) #singular work queue....
    #channel2 = connection.channel()
    #channel2.queue_declare(queue='return_queue')

    channel.queue_declare(queue=QUEUENAME) 
    channel.basic_consume(
        queue=QUEUENAME, on_message_callback=callback) # auto_ack=True removed

    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()
