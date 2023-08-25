package us.irdev.bedrock.bag.scanner;

import java.util.ArrayList;
import java.util.List;

public class State<StateIdType, EmitTokenType> {
  record InputAction<StateIdType, EmitTokenType> (char input, Action<StateIdType, EmitTokenType> action) {}

  private final List<InputAction<StateIdType, EmitTokenType>> inputActions;
  private Action<StateIdType, EmitTokenType> defaultAction;
  private StateIdType stateId;

  // XXX if only Java generics could actually be applied generically to base types too
  private int binarySearch (char key) {
    // starting conditions mapped to either end of the internal store
    var low = 0;
    var high = inputActions.size() - 1;

    // loop as long as the bounds have not crossed
    while (low <= high) {
      // compute the midpoint, and compare the search term against the key stored there, this
      // uses the unsigned right shift in lieu of division by 2
      var mid = (low + high) >>> 1;

      // the entire reason this has its own custom binary search function is that Java
      // generics don't captureInput the need to search inside a complex object like this without a big
      // boxing/unboxing penalty
      var cmp = inputActions.get(mid).input - key;

      // check the result of the comparison
      if (cmp < 0) {
        // the current midpoint is below the target value, set 'low' to one past it so the
        // next loop will look only at the part of the array above the midpoint
        low = mid + 1;
      } else if (cmp > 0) {
        // the current midpoint is above the target value, set 'high' to one below it so the
        // next loop will look only at the part of the array below the midpoint
        high = mid - 1;
      } else {
        // "Found it!" she says in a sing-song voice
        return mid;
      }
    }
    // key not found, return an encoded version of where the key SHOULD be
    return -(low + 1);
  }

  public State (StateIdType stateId) {
    inputActions = new ArrayList<>();
    this.stateId = stateId;
  }

  public Action<StateIdType, EmitTokenType> getAction (char input) {
    int index;
    if ((index = binarySearch(input)) >= 0){
      return inputActions.get(index).action;
    }
    return defaultAction;
  }

  public State<StateIdType, EmitTokenType> onInput(char input, Action<StateIdType, EmitTokenType> action) throws DuplicateInputException {
    int index;
    if ((index = binarySearch(input)) < 0) {
      inputActions.add (-(index + 1), new InputAction<>(input, action));
    } else {
      throw new DuplicateInputException (input);
    }
    return this;
  }

  public State<StateIdType, EmitTokenType> onInput(char input, StateIdType nextStateId, boolean capture, EmitTokenType emitToken) throws DuplicateInputException {
    return onInput(input, new Action<> (nextStateId, capture, emitToken));
  }

  public State<StateIdType, EmitTokenType> onInput(String inputs, Action<StateIdType, EmitTokenType> action) throws DuplicateInputException {
    for (int i = 0; i < inputs.length(); ++i) {
      onInput(inputs.charAt(i), action);
    }
    return this;
  }

  public State<StateIdType, EmitTokenType> onInput(String inputs, StateIdType nextStateId, boolean capture, EmitTokenType emitToken) throws DuplicateInputException {
    return onInput (inputs, new Action<> (nextStateId, capture, emitToken));
  }

  public State<StateIdType, EmitTokenType> onEnd(EmitTokenType emitToken) {
    var inputAction = new InputAction<> ('\0', new Action<> (stateId, Scanner.CAPTURE, emitToken));
    int index;
    if ((index = binarySearch('\0')) < 0) {
      inputActions.add (-(index + 1), inputAction);
    } else {
      inputActions.set(index, inputAction);
    }
    return this;
  }

  public State<StateIdType, EmitTokenType> onAnyInput(StateIdType nextStateId, boolean capture, EmitTokenType emitToken) {
    defaultAction = new Action<> (nextStateId, capture, emitToken);
    return this;
  }
}
