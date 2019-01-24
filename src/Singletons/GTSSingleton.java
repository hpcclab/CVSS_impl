package Singletons;

import Scheduler.GOPTaskScheduler_Mergable;

/**
 * Created by pi on 11/1/17.
 */
public class GTSSingleton {
    private static GTSSingleton instance = null;
    public static GOPTaskScheduler_Mergable GTS = null;

    private GTSSingleton(){
        GTS = new GOPTaskScheduler_Mergable();
    }

    public static GTSSingleton getInstance(){
        if (instance == null){
            instance = new GTSSingleton();
        }
        return instance;
    }
}
