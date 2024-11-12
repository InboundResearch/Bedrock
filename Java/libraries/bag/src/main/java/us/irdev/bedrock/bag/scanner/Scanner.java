package us.irdev.bedrock.bag.scanner;

import java.util.HashMap;
import java.util.Map;

public class Scanner<StateIdType, EmitTokenType> {
  protected Map<StateIdType, State<StateIdType, EmitTokenType>> states;
  private final StateIdType startStateId;
  private final EmitTokenType endOfInputToken;
  protected StateIdType currentStateId;
  private String currentToken;
  protected char[] input;
  protected int offset;

  protected static final boolean CAPTURE = true;
  protected static final boolean DONT_CAPTURE = false;
  protected static final char END_OF_INPUT = '\0';


  public Scanner (StateIdType startStateId, EmitTokenType endOfInputToken) {
    states = new HashMap<>();
    this.startStateId = startStateId;
    this.endOfInputToken = endOfInputToken;
  }

  public State<StateIdType, EmitTokenType> addState(StateIdType stateId) throws DuplicateStateException {
    if (states.containsKey(stateId)) {
      throw new DuplicateStateException(stateId.toString());
    }
    var state = new State<StateIdType, EmitTokenType>(stateId);
    states.put (stateId, state);
    return state.onEnd (endOfInputToken);
  }

  public Token<EmitTokenType> scanChar(char input) {
    // set up to receive the result
    Token<EmitTokenType> result = null;

    // get the current state
    var currentState = states.get(currentStateId);

    // get the action for the input from the current state
    var action = currentState.getAction(input);
    if (action != null) {
      // advance to the next state
      currentStateId = action.nextStateId();

      // if we should capture the input...
      if (action.captureInput()) {
        currentToken += input;
        ++offset;
      }

      // if we should emit a token...
      var emitToken = action.emitToken();
      if (emitToken != null) {
        result = new Token<> (emitToken, currentToken);

        // reset the current token
        currentToken = "";
      }
    }

    // return the token result of the scan (if any)
    return result;
  }

  public void start (char[] input) {
    this.input = input;
    currentToken = "";
    currentStateId = startStateId;
    offset = 0;
  }

  public void start (String input) {
    start (input.toCharArray());
  }

  public Token<EmitTokenType> scanToken() {
    while (offset < input.length) {
      var token = scanChar(input[offset]);
      if (token != null) {
        return token;
      }
    }

    // send the end of input into the scanner
    return (currentToken.length() > 0) ? scanChar(END_OF_INPUT) : null;
  }

  public void scanString(String input, Receiver<EmitTokenType> receiver) {
    start (input);
    Token<EmitTokenType> token;
    while ((token = scanToken()) != null) {
      receiver.handleToken(token);
    }
  }
}
