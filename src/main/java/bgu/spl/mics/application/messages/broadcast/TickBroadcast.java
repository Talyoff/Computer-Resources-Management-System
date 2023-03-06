package bgu.spl.mics.application.messages.broadcast;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    private int timePassed = 0;
    private int tickTime;

    public TickBroadcast(int timePassed, int tickTime){
        this.timePassed = timePassed;
        this.tickTime = tickTime;
    }

    public int getTicksPassed(){
        return timePassed;
    }

    public int getTickTime(){
        return tickTime;
    }
}
