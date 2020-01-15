package it.paraularey.hostapdwifiapi.eventsystem.events;

import it.paraularey.hostapdwifiapi.eventsystem.Event;

public class ApStationConnectedEvent implements Event {

    private int debugLevel;
    private String mac;

    public ApStationConnectedEvent(int debugLevel, String mac) {
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