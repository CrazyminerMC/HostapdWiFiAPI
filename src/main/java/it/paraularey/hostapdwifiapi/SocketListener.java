package it.paraularey.hostapdwifiapi;

import it.paraularey.hostapdwifiapi.eventsystem.Event;
import it.paraularey.hostapdwifiapi.eventsystem.events.ApDisabledEvent;
import it.paraularey.hostapdwifiapi.eventsystem.events.ApEnabledEvent;
import it.paraularey.hostapdwifiapi.eventsystem.events.ApStationConnectedEvent;
import it.paraularey.hostapdwifiapi.eventsystem.events.ApStationDisconnectedEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketListener implements Runnable {

  private Logger logger = LoggerFactory.getLogger(SocketListener.class);
  private HostapdCtrl hostapdCtrl;

  SocketListener(HostapdCtrl hostapdCtrl) {
    this.hostapdCtrl = hostapdCtrl;
  }

  @Override
  public void run() {
    while (hostapdCtrl.isStarted() && hostapdCtrl.isAttached()) {
      try {
        if (hostapdCtrl.pending(0.5D)) {

          String result = hostapdCtrl.recv().replace("\n", "");
          logger.debug(result);

          Pattern pattern;
          Matcher matcher;
          if (isEventWithMAC(result)) {
            pattern = Pattern.compile("(<[0-9]>)(.+) ([0-9A-Fa-f:]{17})");
            matcher = pattern.matcher(result);

            if (matcher.find()) {
              String debugLevel = matcher.group(1).replace("<", "").replace(">", "");
              String eventMessage = matcher.group(2);
              String macAddress = matcher.group(3);

              callEvent(debugLevel, eventMessage, macAddress);
            }
          } else {
            pattern = Pattern.compile("(<[0-9]>)(.+) ");
            matcher = pattern.matcher(result);

            if (matcher.find()) {
              String debugLevel = matcher.group(1).replace("<", "").replace(">", "");
              String eventMessage = matcher.group(2);

              callEvent(debugLevel, eventMessage);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void callEvent(String debugLevel, String eventMessage, String... parameters) {
    logger.debug(debugLevel + "  " + eventMessage + "  " + Arrays.toString(parameters));
    Event event = null;
    switch (eventMessage) {
      case "AP-STA-CONNECTED":
        event = new ApStationConnectedEvent(Integer.parseInt(debugLevel), parameters[0]);
        break;
      case "AP-STA-DISCONNECTED":
        event = new ApStationDisconnectedEvent(Integer.parseInt(debugLevel), parameters[0]);
        break;
      case "AP-DISABLED":
        event = new ApDisabledEvent();
        break;
      case "AP-ENABLED":
        event = new ApEnabledEvent();
        break;
    }

    if (event != null) {
      hostapdCtrl.getEventSystem().callEvent(event);
    }
  }

  private boolean isAnEvent(String message) {
    return message.matches("(<[0-9]>) ");
  }

  private boolean isEventWithMAC(String message) {
    return message.matches("(<[0-9]>)(.+) ([0-9A-Fa-f:]{17})");
  }
}
