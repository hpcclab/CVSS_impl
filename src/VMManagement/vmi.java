package VMProvisionerpkg;

import TranscodingVM.TranscodingVM;

public class vmi{
    String type;
    String identification;
    TranscodingVM TVM;
    public vmi(String type, String identification, TranscodingVM TVM) {
        this.type = type;
        this.identification = identification;
        this.TVM = TVM;
    }
    public vmi(String type, String identification) {
        this.type = type;
        this.identification = identification;
    }
}