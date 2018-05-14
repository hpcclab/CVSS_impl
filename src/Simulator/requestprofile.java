package Simulator;

class requestprofile implements Comparable<requestprofile> {
    public int videoChoice;
    public String command;
    public String setting;
    public long appearTime;
    public long deadline;
    //recipe is at Video level, each presentation time of each GOPS are in repository video
    public requestprofile(int videoChoice, String command, String setting, long appearTime, long deadline) {
        this.videoChoice = videoChoice;
        this.command = command;
        this.setting = setting;
        this.appearTime = appearTime;
        this.deadline = deadline;
    }

    @Override
    public int compareTo(requestprofile requestprofile) {
        if (requestprofile != null) {
            if (this.appearTime > requestprofile.appearTime) {
                return 1;
            } else if (this.appearTime < requestprofile.appearTime) {
                return -1;
            }
            return 0;
        }
        return -1;
    }
}