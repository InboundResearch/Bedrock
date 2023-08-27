package us.irdev.bedrock.bag.scanner;

public class DuplicateInputException extends Exception {
  DuplicateInputException (char input, boolean inclusive) {
    super ("Duplicate input: '" + input + "' (" + (inclusive? "inclusive" : "exclusive") + ")");
  }
  DuplicateInputException (String inputs, boolean inclusive) {
    super ("Duplicate input: [" + inputs + "] (" + (inclusive? "inclusive" : "exclusive") + ")");
  }
  DuplicateInputException (char input) {
    this(input, true);
  }
  DuplicateInputException (String inputs) {
    this(inputs, false);
  }
}
