package us.irdev.bedrock.bag.scanner;

import java.util.Arrays;

import static us.irdev.bedrock.bag.formats.Utility.sortString;

public class StrategyExclusive implements Strategy {
  private final char[] inputs;
  private final Action action;

  StrategyExclusive(String inputs, Action action) {
    this.inputs = sortString(inputs);
    this.action = action;
  }

  public Action getScannerAction (char input) {
    return (Arrays.binarySearch(inputs, input) >= 0) ? action : null;
  }
}
