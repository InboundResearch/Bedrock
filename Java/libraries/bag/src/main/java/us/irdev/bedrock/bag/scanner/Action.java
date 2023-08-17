package us.irdev.bedrock.bag.scanner;

public class Action {

  private final String nextState;
  private final boolean capture;
  private final String emit;

  public Action (String nextState, boolean capture, String emit) {
    this.nextState = nextState;
    this.capture = capture;
    this.emit = emit;
  }

  public String getNextState () {
    return nextState;
  }

  public boolean getCapture() {
    return capture;
  }

  public String getEmit () {
    return emit;
  }
}
