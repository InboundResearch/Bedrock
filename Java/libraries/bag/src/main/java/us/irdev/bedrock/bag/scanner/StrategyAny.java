package us.irdev.bedrock.bag.scanner;

public class StrategyAny implements Strategy {
  private final Action action;

  StrategyAny(Action action) {
    this.action = action;
  }

  public Action getScannerAction (char input) {
    return action;
  }
}
