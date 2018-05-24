package Singletons;

import VMManagement.VMProvisioner;

/**
 * Created by pi on 11/1/17.
 */
public class VMPSingleton {
    private static VMPSingleton instance = null;
    public static VMProvisioner VMP = null;

    private VMPSingleton(int num){VMP = new VMProvisioner(num);}

    public static VMPSingleton getInstance(int num){
        if (instance == null){
            instance = new VMPSingleton(num);
        }

        return instance;
    }
}
