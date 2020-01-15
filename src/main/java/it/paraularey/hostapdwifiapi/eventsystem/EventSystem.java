package it.paraularey.hostapdwifiapi.eventsystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventSystem {

    private List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean callEvent(Event event) {
        for (Listener listener : listeners) {
            return invokeEvent(listener, event);
        }
        return false;
    }

    private boolean invokeEvent(Listener listener, Event event) {
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                if (method.getParameterTypes().length == 0
                        || method.getParameterTypes()[0] != event.getClass()) {
                    continue;
                }
                //System.out.println(method);
                try {
                    method.setAccessible(true);
                    method.invoke(listener, event);
                    return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }
}