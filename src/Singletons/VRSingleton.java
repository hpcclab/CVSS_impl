package Singletons;

import Repository.VideoRepository;

/**
 * Created by pi on 11/1/17.
 */
public class VRSingleton {
    private static VRSingleton instance = null;
    public static VideoRepository VR = null;

    private VRSingleton(){VR = new VideoRepository();}

    public static VRSingleton getInstance(){
        if (instance == null){
            instance = new VRSingleton();
        }
        return instance;
    }
}
