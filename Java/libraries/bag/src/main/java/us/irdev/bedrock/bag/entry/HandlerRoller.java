package us.irdev.bedrock.bag.entry;

public class HandlerRoller extends Handler {
    private final Handler[] handlers;
    private int roll;

    public HandlerRoller (Handler... handlers) {
        this.handlers = handlers;
        roll = 0;
    }

    @Override
    public Object getEntry (String input) {
        var result = handlers[roll].getEntry (input);
        roll = (roll + 1) % handlers.length;
        return result;
    }
}
