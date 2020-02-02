package it.paraularey.hostapdwifiapi;

import it.paraularey.hostapdwifiapi.eventsystem.EventSystem;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import jnr.enxio.channels.NativeSelectorProvider;
import jnr.unixsocket.UnixDatagramChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostapdCtrl {

  private Logger logger = LoggerFactory.getLogger(HostapdCtrl.class);
  private boolean attached;
  private boolean started;
  private UnixSocketAddress dest;
  private UnixSocketAddress local;
  private UnixDatagramChannel socket;
  private EventSystem eventSystem;
  private AbstractSelector selector;

  public HostapdCtrl(String path) throws HostapdException, IOException {
    File socketDestFile = new File(path);
    if (!socketDestFile.exists()) {
      throw new HostapdException("Socket path must exists");
    }

    attached = false;
    started = false;

    selector = NativeSelectorProvider.getInstance().openSelector();
    socket = UnixDatagramChannel.open();

    dest = new UnixSocketAddress(socketDestFile);
    local = new UnixSocketAddress(createTempSockFile());

    socket.configureBlocking(false);
    socket.setOption(UnixSocketOptions.SO_RCVTIMEO, 10);
    socket.register(selector, SelectionKey.OP_READ);

    socket.bind(local);
    socket.connect(dest);
    started = true;

    eventSystem = new EventSystem();
  }

  public void close() {
    if (this.attached) {
        try {
            detach();
        } catch (Exception e) {
            e.printStackTrace();
            this.attached = false;
        }
    }
    if (this.started) {
      try {
        this.socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      this.started = false;
    }
    try {
      selector.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void attach() throws HostapdException {
    if (this.attached) return;
    String result = request("ATTACH");
    if ("OK\n".equals(result)) {
      this.attached = true;
      logger.info(local.humanReadablePath() + "  ATTACHED successfully");
    } else {
      throw new HostapdException("ATTACH failed");
    }
  }

  public void detach() throws HostapdException, IOException {
    if (!this.attached) return;
    while (pending())
      recv();

    String result = request("DETACH");
    if ("OK\n".equals(result)) {
      this.attached = false;
      logger.info(local.humanReadablePath() + "  DETACHED successfully");
    } else {
      throw new HostapdException("DETACH failed");
    }
  }

  public void terminate() {
    if (this.attached) {
      try {
        detach();
        request("TERMINATE");
      } catch (Exception e) {
        attached = false;
      }
    }
    close();
  }

  public boolean pending() throws IOException {
    return pending(0);
  }

  public boolean pending(double timeout) throws IOException {
    int result =
        timeout == 0
            ? selector.selectNow()
            : timeout > 0 ? selector.select((long) (timeout * 1000D)) : selector.select();
    if (result > 0) {
      Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      while (iterator.hasNext()) {
        SelectionKey selectedKey = iterator.next();
        if (selectedKey.isReadable()) {
          iterator.remove();
          return true;
        }
      }
    }
    return false;
  }

  public String recv() throws IOException {
    ByteBuffer rxBuf = ByteBuffer.allocate(2048);
    this.socket.receive(rxBuf);
    rxBuf.flip();
    return StandardCharsets.UTF_8.decode(rxBuf).toString();
  }

  public String request(String cmd) throws HostapdException {
    return request(cmd, 10);
  }

  public String request(String cmd, int timeout) throws HostapdException {
    logger.info(local.humanReadablePath() + ": command executed" + cmd);
    ByteBuffer txBuf = StandardCharsets.UTF_8.encode(cmd);

    if (socket != null && socket.isOpen()) {
      try {
        this.socket.send(txBuf, this.dest);
        if (pending(timeout)) {
          return recv();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    throw new HostapdException("Timeout on waiting response");
  }

  private File createTempSockFile() {
    File tempSocketFile = null;
    try {
      tempSocketFile = File.createTempFile("wpa_ctrl_", "");
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(
          "Non sono riuscito a creare il file temporaneo per il socket "
              + this.dest.humanReadablePath());
    }
    tempSocketFile.delete();
    tempSocketFile.deleteOnExit();
    return tempSocketFile;
  }

  public UnixDatagramChannel getSocket() {
    return socket;
  }

  public EventSystem getEventSystem() {
    return eventSystem;
  }
  public boolean isAttached() {
    return attached;
  }

  public boolean isStarted() {
    return started;
  }

  public AbstractSelector getSelector() {
    return selector;
  }
}
