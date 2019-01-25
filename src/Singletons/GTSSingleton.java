package Singletons;

import Scheduler.GOPTaskScheduler_mergable;

/**
 * Created by pi on 11/1/17.
 */
public class GTSSingleton {
    private static GTSSingleton instance = null;
    public static GOPTaskScheduler_mergable GTS = null;

    private GTSSingleton(){
        GTS = new GOPTaskScheduler_mergable();
    }

    public static GTSSingleton getInstance(){
        if (instance == null){
            instance = new GTSSingleton();
        }
        return instance;
    }
}
