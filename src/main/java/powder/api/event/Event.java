package powder.api.event;

public class Event {

    private boolean isCanceled;

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

}
