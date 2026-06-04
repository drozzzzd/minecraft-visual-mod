package powder.api.timer;

public interface ITimer {
    default void run() {}
    default void run(Runnable runnable) {}

    void stop();
}
