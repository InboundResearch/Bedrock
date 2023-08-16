package us.irdev.bedrock.bag.scanner;

import java.util.ArrayList;
import java.util.List;

public class State {
  private final String name;
  private final List<Strategy> strategies;

  public State (String name) {
    this.name = name;
    strategies = new ArrayList<Strategy>();
  }

  public String getName () {
    return name;
  }

  public State addStrategy(Strategy strategy) {
    strategies.add(strategy);
    return this;
  }

  public Action getScannerAction (char input) {
    for (Strategy strategy : strategies) {
      var action = strategy.getScannerAction(input);
      if (action != null) {
        return action;
      }
    }
    return null;
  }
}
