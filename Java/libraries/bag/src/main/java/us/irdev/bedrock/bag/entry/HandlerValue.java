package us.irdev.bedrock.bag.entry;

public class HandlerValue extends Handler {
    public static final HandlerValue HANDLER_VALUE = new HandlerValue ();

    private HandlerValue () {}

    @Override
    public Object getEntry (String input) {
        return input.trim ();
    }
}
