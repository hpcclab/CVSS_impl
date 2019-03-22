package mainPackage;

import Cache.Caching;
import IOWindows.OutputWindow;
import IOWindows.WebserviceRequestGate;
import Repository.VideoRepository;
import ResourceManagement.ResourceProvisioner;
import Scheduler.GOPTaskScheduler;
import Simulator.RequestGenerator;
import TimeEstimatorpkg.TimeEstimator;

public class CVSE {
    public VideoRepository VR;
    public Caching CACHING;
    public GOPTaskScheduler GTS; //merger created inside GTS if use mergeableGTS
    public ResourceProvisioner VMP;
    public TimeEstimator TE;
    public RequestGenerator RG;
    public OutputWindow OW;

    public WebserviceRequestGate WG;
}
