package us.irdev.bedrock.bag.scanner;

import java.util.ArrayList;
import java.util.List;

public class State {
  record InputAction (char input, Action action) {}

  private final String name;
  private final List<InputAction> inputActions;
  private Action defaultAction;

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
      // generics don't capture the need to search inside a complex object like this without a big
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

  public State (String name) {
    this.name = name;
    inputActions = new ArrayList<>();
  }

  public String getName () {
    return name;
  }

  public Action getAction (char input) {
    int index;
    if ((index = binarySearch(input)) >= 0){
      return inputActions.get(index).action;
    }
    return defaultAction;
  }

  public State onInput(char input, Action action) throws DuplicateInputException {
    int index;
    if ((index = binarySearch(input)) < 0) {
      inputActions.add (-(index + 1), new InputAction(input, action));
    } else {
      throw new DuplicateInputException (input);
    }
    return this;
  }

  public State onInput(char input, String nextState, boolean capture, String emit) throws DuplicateInputException {
    return onInput(input, new Action (nextState, capture, emit));
  }

  public State onInput(String inputs, Action action) throws DuplicateInputException {
    for (int i = 0; i < inputs.length(); ++i) {
      onInput(inputs.charAt(i), action);
    }
    return this;
  }

  public State onInput(String inputs, String nextState, boolean capture, String emit) throws DuplicateInputException {
    return onInput (inputs, new Action (nextState, capture, emit));
  }

  public State onAnyInput(String nextState, boolean capture, String emit) {
    defaultAction = new Action (nextState, capture, emit);
    return this;
  }
}
