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

  public Scanner (String startStateName) {
    this.startStateName = startStateName;
    resetScanner();
  }

  public Scanner () {
    this (DEFAULT_START_STATE_NAME);
  }

  public Scanner addState(State state) throws DuplicateStateException {
    var stateName = state.getName();
    if (states.containsKey(stateName)) {
      throw new DuplicateStateException(stateName);
    }
    states.put (state.getName(), state);
    return this;
  }

  public Scanner setCurrentState (String stateName) {
    currentStateName = stateName;
    return this;
  }

  public Scanner resetScanner () {
    states = new HashMap<String, State>();
    return reset();
  }

  public Scanner reset () {
    currentToken = "";
    offset = 0;
    return setCurrentState(startStateName);
  }

  public String getStartStateName () {
    return startStateName;
  }

  public State getState(String stateName) {
    return states.get(stateName);
  }

  public State getStartState () {
    return getState(startStateName);
  }

  public void handleInput(char input) {
    // get the current state
    var currentState = states.get(currentStateName);

    // get the action for the input from the current state
    var action = currentState.getScannerAction(input);
    if (action != null) {
      // if we should store the input to the token before emitting...
      var storage = action.getStorage();
      if (storage == StorageType.STORE_INPUT_BEFORE_EMIT) {
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

      // if we should store the input to the token after emitting...
      if (storage == StorageType.STORE_INPUT_AFTER_EMIT) {
        currentToken += input;
        ++offset;
      }

      // advance to the next state
      currentStateName = nextStateName;
    }
  }

  public Scanner scan(String input) {
    reset();
    while (offset < input.length ()) {
      handleInput(input.charAt(offset));
    }
    return this;
  }

  public abstract void emit (String actionEmit, String token, String nextStateName);
}
