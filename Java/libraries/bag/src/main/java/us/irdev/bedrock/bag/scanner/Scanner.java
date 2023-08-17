package us.irdev.bedrock.bag.scanner;

import java.util.HashMap;
import java.util.Map;

public abstract class Scanner {
  protected Map<String, State> states;
  private final String startStateName;
  protected String currentStateName;
  private String currentToken;
  protected int offset;

  protected static final String DEFAULT_START_STATE_NAME = "start";
  protected final static String ERROR_STATE = "error";
  protected final static String EMIT_ERROR = "error";

  public Scanner (String startStateName) {
    states = new HashMap<String, State>();
    this.startStateName = startStateName;
  }

  public Scanner () {
    this (DEFAULT_START_STATE_NAME);
  }

  public State addState(String stateName) throws DuplicateStateException {
    if (states.containsKey(stateName)) {
      throw new DuplicateStateException(stateName);
    }
    var state = new State(stateName);
    states.put (stateName, state);
    return state;
  }

  public void scanChar(char input) {
    // get the current state
    var currentState = states.get(currentStateName);

    // get the action for the input from the current state
    var action = currentState.getAction(input);
    if (action != null) {
      // if we should capture the input...
      if (action.getCapture()) {
        currentToken += input;
        ++offset;
      }

      // if we should emit...
      var actionEmit = action.getEmit();
      var nextStateName = action.getNextState();
      if (actionEmit != null) {
        emit (actionEmit, currentToken, nextStateName);
        currentToken = "";
      }

      // advance to the next state
      currentStateName = nextStateName;
    }
  }

  public Scanner scanString(String input) {
    currentToken = "";
    currentStateName = startStateName;
    offset = 0;
    while (offset < input.length ()) {
      scanChar(input.charAt(offset));
    }
    return this;
  }

  public abstract void emit (String actionEmit, String token, String nextStateName);
}
