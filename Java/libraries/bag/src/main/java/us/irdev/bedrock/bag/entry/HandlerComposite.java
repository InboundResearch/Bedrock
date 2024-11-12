package us.irdev.bedrock.bag.entry;

public abstract class HandlerComposite extends Handler {
    protected Handler handler;

    protected HandlerComposite (Handler handler) {
        this.handler = handler;
    }
}
