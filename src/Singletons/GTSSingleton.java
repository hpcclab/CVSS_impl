package Singletons;

import Scheduler.GOPTaskScheduler;

/**
 * Created by pi on 11/1/17.
 */
public class GTSSingleton {
    private static GTSSingleton instance = null;
    public static GOPTaskScheduler GTS = null;

    private GTSSingleton(){
        GTS = new GOPTaskScheduler();
    }

    public static GTSSingleton getInstance(){
        if (instance == null){
            instance = new GTSSingleton();
        }
        return instance;
    }
}
