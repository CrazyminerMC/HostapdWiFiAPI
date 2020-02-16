package it.paraularey.hostapdwifiapi;

import it.paraularey.hostapdwifiapi.eventsystem.EventSystem;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostapdGlobal {

  private Logger logger = LoggerFactory.getLogger(HostapdGlobal.class);
  private HostapdCtrl ctrl;
  private HostapdCtrl mon;
  private Thread monListener;

  public HostapdGlobal(String path) throws HostapdException, IOException {
    File socketDestFile = new File(path);
    if (!socketDestFile.exists()) {
      throw new HostapdException("Socket path must exists");
    }

    ctrl = new HostapdCtrl(path);
    mon = new HostapdCtrl(path);
    mon.attach();

    monListener = new Thread(new SocketListener(mon));
    monListener.start();
  }

  public void close() {
    this.mon.close();
    this.ctrl.close();
  }

  public String request(String cmd) throws HostapdException {
    return request(cmd, 10);
  }

  public String request(String cmd, int timeout) throws HostapdException {
    return this.ctrl.request(cmd, timeout);
  }

  public EventSystem getEventSystem() {
    return mon.getEventSystem();
  }

  public boolean isCtrlStarted() {
    return this.ctrl != null && this.ctrl.isStarted();
  }
}
