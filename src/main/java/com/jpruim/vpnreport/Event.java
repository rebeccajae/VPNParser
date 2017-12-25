package com.jpruim.vpnreport;

import org.joda.time.DateTime;

public class Event {
    public enum eventType {
        EVENT_LOGON,
        EVENT_LOGOFF
    }
    public String user;
    public DateTime dt;
    public eventType type;
    public Event(String username, DateTime eventTime, eventType type){
        this.user = username;
        this.dt = eventTime;
        this.type = type;
    }

}
