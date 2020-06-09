package Scheduler;

import SessionPkg.Session;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

public class AdmissionControl {
    private double utilityBased_Prioritization(double c,int segment_number){
        //page 5 of the paper
        return Math.pow(1/Math.E,c*segment_number);
    }


    public void AssignSegmentPriority(TranscodingRequest segment)
    {
        segment.Priority=0;
    }
    public void AssignSegmentPriority(TranscodingRequest segment,int priority)
    {
        segment.Priority=priority;
    }
//    public void AssignStreamPriority(Session session){
//        for (StreamGOP x : session.streamGOPs){
//            double newPriority;
//           // System.out.println("adc segment"+x.segment);
//            int segment_number=Integer.parseInt(x.segment);
//
//            //how we actually assign priority?
//            newPriority=utilityBased_Prioritization(CVSE.config.c_const_for_utilitybased,segment_number);
//            //
//            x.setPriority(newPriority);
//        }
//
//    }
    private static void GetVideoSplitterSegmentInfo(TranscodingRequest segment)
    {

    }

    private static void GetVideoMergerSegmentInfo(TranscodingRequest segment)
    {

    }

}