package it.paraularey.hostapdwifiapi.eventsystem.events;

import it.paraularey.hostapdwifiapi.eventsystem.Event;

public class ApStationDisconnectedEvent implements Event {

    private String mac;
    private int debugLevel;

    public ApStationDisconnectedEvent(int debugLevel, String mac) {
        this.debugLevel = debugLevel;
        this.mac = mac;
    }


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}