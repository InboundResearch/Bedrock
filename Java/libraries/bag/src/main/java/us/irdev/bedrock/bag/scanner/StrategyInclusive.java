package us.irdev.bedrock.bag.scanner;

import java.util.ArrayList;

public class StrategyInclusive implements Strategy {
  static class InputAction {
    char input;
    Action action;

    InputAction (char input, Action action) {
      this.input = input;
      this.action = action;
    }
  }

  private final ArrayList<InputAction> charActions;

  private int binarySearch (char key) {
    // starting conditions mapped to either end of the internal store
    var low = 0;
    var high = charActions.size() - 1;

    // loop as long as the bounds have not crossed
    while (low <= high) {
      // compute the midpoint, and compare the search term against the key stored there, this
      // uses the unsigned right shift in lieu of division by 2
      var mid = (low + high) >>> 1;

      // the entire reason this has its own custom binary search function is that Java
      // generics don't capture the need to search inside a complex object like this without a big
      // boxing/unboxing penalty
      var cmp = charActions.get(mid).input - key;

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


  StrategyInclusive () {
    charActions = new ArrayList<InputAction> ();
  }

  public Action getScannerAction (char input) {
    int index;
    if ((index = binarySearch(input)) >= 0){
      return charActions.get(index).action;
    }
    return null;
  }

  public StrategyInclusive addAction (char input, Action action) throws DuplicateInputException {
    int index;
    if ((index = binarySearch(input)) < 0) {
      charActions.add (-(index + 1), new InputAction(input, action));
    } else {
      throw new DuplicateInputException (input);
    }
    return this;
  }

  public StrategyInclusive addAction (String inputs, String nextState, StorageType storage, String emit) throws DuplicateInputException {
    var action = new Action (nextState, storage, emit);
    for (int i = 0; i < inputs.length(); ++i) {
      addAction(inputs.charAt(i), action);
    }
    return this;
  }
}
