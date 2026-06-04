package powder.api.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventSystem {

    private static final Map<Class<? extends Event>, List<Object>> listeners = new HashMap<>();

    public static void register(Object listener) {
        for (Method method : getSubscribedMethods(listener.getClass())) {
            Class<?> eventClass = method.getParameterTypes()[0];
            if (Event.class.isAssignableFrom(eventClass)) {
                List<Object> list = listeners.computeIfAbsent((Class<? extends Event>) eventClass, k -> new ArrayList<>());
                if (!list.contains(listener)) list.add(listener);
            }
        }
    }

    public static void unregister(Object listener) {
        listeners.values().forEach(list -> list.remove(listener));
    }

    public static <T extends Event> T post(T event) {
        List<Object> eventListeners = listeners.get(event.getClass());

        if (eventListeners != null) {
            for (Object listener : new ArrayList<>(eventListeners)) {
                for (Method method : getSubscribedMethods(listener.getClass())) {
                    if (method.getParameterTypes()[0].equals(event.getClass())) {
                        try {
                            method.invoke(listener, event);
                            if (event.isCanceled()) break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return event;
    }

    /**
     * Collects all {@link EventSubscribe} methods of {@code type}, including those
     * inherited from super classes. {@link Class#getDeclaredMethods()} alone only
     * returns methods declared on the exact class, so listeners that subscribe via
     * a base class (e.g. the Torov Visual HUD elements extending {@code HudElement})
     * would otherwise never be registered. Overridden methods are kept once.
     */
    private static List<Method> getSubscribedMethods(Class<?> type) {
        Map<String, Method> collected = new LinkedHashMap<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventSubscribe.class)) continue;
                if (method.getParameterTypes().length != 1) continue;

                String signature = method.getName() + "(" + method.getParameterTypes()[0].getName() + ")";
                if (collected.containsKey(signature)) continue;

                method.setAccessible(true);
                collected.put(signature, method);
            }
        }
        return new ArrayList<>(collected.values());
    }

}
