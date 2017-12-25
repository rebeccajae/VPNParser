package com.jpruim.vpnreport;

public class Session {

    public Event start, end;

    public Session(Event begin, Event end){
        this.start = begin;
        this.end = end;
    }
    public long getMillisBetween(){
        return end.dt.getMillis() - start.dt.getMillis();
    }
}
