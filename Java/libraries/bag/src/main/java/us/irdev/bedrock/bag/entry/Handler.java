package us.irdev.bedrock.bag.entry;

public abstract class Handler {
    public abstract Object getEntry (String input);
    public Object getEntry (char[] input) {
        return getEntry(new String (input));
    }
}
