package us.irdev.bedrock.bag.scanner;

import java.util.HashMap;
import java.util.Map;

public class Scanner {
  protected Map<String, State> states;
  private final String startStateName;
  protected String currentStateName;
  private String currentToken;
  protected String input;
  protected int offset;

  protected static final String DEFAULT_START_STATE_NAME = "start";
  protected final static String ERROR_STATE = "error";
  protected final static String EMIT_ERROR = "error";

  public Scanner (String startStateName) {
    states = new HashMap<>();
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

  public Token scanChar(char input) {
    // set up to receive the result
    Token result = null;

    // get the current state
    var currentState = states.get(currentStateName);

    // get the action for the input from the current state
    var action = currentState.getAction(input);
    if (action != null) {
      // capture the next state
      var nextStateName = action.nextState();

      // if we should capture the input...
      if (action.capture()) {
        currentToken += input;
        ++offset;
      }

      // if we should emit...
      var actionEmit = action.emit();
      if (actionEmit != null) {
        result = new Token (currentStateName, actionEmit, currentToken, nextStateName);
        currentToken = "";
      }

      // advance to the next state
      currentStateName = nextStateName;
    }

    // return the token result of the scan (if any)
    return result;
  }

  public void start (String input) {
    this.input = input;
    currentToken = "";
    currentStateName = startStateName;
    offset = 0;
  }

  public Token scanToken() {
    while (offset < input.length ()) {
      var token = scanChar(input.charAt(offset));
      if (token != null) {
        return token;
      }
    }
    // XXX consider returning the final status as a "cleanup" token
    return null;
  }

  public void scanString(String input, Receiver receiver) {
    start (input);
    Token token;
    while ((token = scanToken()) != null) {
      receiver.handleToken(token);
    }
  }
}
