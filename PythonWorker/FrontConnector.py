#!/usr/bin/env python
import pika
import TaskRequest_pb2

QUEUENAME="mq0"
FEEDBACKQUEUENAME="feedback_queue"
MYID=0

connection = pika.BlockingConnection(
    pika.ConnectionParameters(host='localhost'))
channel = connection.channel()
channel.queue_declare(queue=QUEUENAME) #singular work queue....
#feedbackchannel = connection.channel()
#feedbackchannel.queue_declare(queue=FEEDBACKQUEUENAME) #singular work queue....
#channel2 = connection.channel()
#channel2.queue_declare(queue='return_queue')

#todo, this must be expandable...
cmdTranslatorTable={
'RESOLUTION':'-vf scale=',
'FRAMERATE':'-r',
'BITRATE':'-b:v',
'CODEC':'-c:v'
}
paramTranslatorTable={
'RESOLUTION':['1920:1080','640:360'],
'FRAMERATE':['60','24'],
'BITRATE':['3.4M','2.4M'],
'CODEC':['libx264','libvpx-vp9']
}

def cmdTranslate(operation):
    if cmdTranslatorTable[str(operation)]!='null':
        return cmdTranslatorTable[operation]
    else:
        print("Unknown translateion look up "+operation)
        return "UNKNOWNOP"

def paramTranslate(operation,param):
    if paramTranslatorTable[operation][param]!='null':
        return paramTranslatorTable[operation][param]
    else:
        print("Unknown translateion look up "+operation+" "+param)
        return "UNKNOWNPARAM"

def transcode(aRequest):
    #cmd,options,datasource
    if(aRequest.DataSource.find("_")>-1):
        videochoice=aRequest.DataSource.split("_")[0]
        segmentnum=aRequest.DataSource.split("_")[1]
    else:
        print("Special message received ")
        return;
    inputfile="./Repo/"+videochoice+"/"+segmentnum+".ts"
    outputfile="./out/"+videochoice+"/"+segmentnum+".ts"
    print(inputfile+" Deadline="+str(aRequest.GlobalDeadline)+" ")
    for eachop in aRequest.OPlist:
        print("cmd="+str(eachop.Cmd))
        for eachparam in eachop.Parameter:
            print("parameterchoice="+str(eachparam.subparameter))
            print("cmd="+ "ffmpeg -y -i "+inputfile+" "+cmdTranslate(eachop.Cmd)+" "+paramTranslate(eachop.Cmd,int(eachparam.subparameter))+" "+outputfile)
    #reportback some data
    report= TaskRequest_pb2.TaskReport()
    report.completedTaskID=aRequest.TaskID
    report.workerNodeID=MYID
    report.executionTime=20
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
    transcode(parsedbody)
    ch.basic_ack(delivery_tag = method.delivery_tag)


channel.basic_consume(
    queue=QUEUENAME, on_message_callback=callback) # auto_ack=True removed

print(' [*] Waiting for messages. To exit press CTRL+C')
channel.start_consuming()
