package us.irdev.bedrock.bag.scanner;

public class DuplicateStateException extends Exception {
  DuplicateStateException (String stateName) {
    super ("Duplicate scanner state: '" + stateName + "')");
  }
}
