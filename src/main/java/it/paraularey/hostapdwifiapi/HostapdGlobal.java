package it.paraularey.hostapdwifiapi;

import it.paraularey.hostapdwifiapi.eventsystem.EventSystem;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostapdGlobal {

  private Logger logger = LoggerFactory.getLogger(HostapdGlobal.class);
  private HostapdCtrl ctrl;
  private HostapdCtrl mon;
  private Thread monListener;

  public HostapdGlobal(String path) throws Exception {
    File socketDestFile = new File(path);
    if (!socketDestFile.exists()) {
      throw new Exception("Socket path must exists");
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

  public String request(String cmd) throws Exception {
    return request(cmd, 10);
  }

  public String request(String cmd, int timeout) throws Exception {
    return this.ctrl.request(cmd, timeout);
  }

  public EventSystem getEventSystem() {
    return mon.getEventSystem();
  }
}
