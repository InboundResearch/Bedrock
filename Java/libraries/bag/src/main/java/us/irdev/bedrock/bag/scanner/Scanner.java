package us.irdev.bedrock.bag.scanner;

import java.util.HashMap;
import java.util.Map;

public abstract class Scanner {
  protected Map<String, State> states;
  private final String startStateName;
  protected String currentStateName;
  private String currentToken;

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
      }

      // if we should emit...
      var actionEmit = action.getEmit();
      if (actionEmit != null) {
        emit (actionEmit, currentToken);
        currentToken = "";
      }

      // if we should store the input to the token after emitting...
      if (storage == StorageType.STORE_INPUT_AFTER_EMIT) {
        currentToken += input;
      }

      // advance to the next state
      currentStateName = action.getNextState ();
    }
  }

  public Scanner scan(String input) {
    reset();
    for (int i = 0; i < input.length(); ++i) {
      handleInput(input.charAt(i));
    }
    return this;
  }

  public abstract void emit (String actionEmit, String token);
}
