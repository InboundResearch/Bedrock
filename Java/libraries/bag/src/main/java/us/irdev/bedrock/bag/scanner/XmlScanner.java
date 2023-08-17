package us.irdev.bedrock.bag.scanner;

public class XmlScanner extends Scanner {
  private final String DEFAULT_STATE = DEFAULT_START_STATE_NAME;
  private final String ERROR_STATE = "error";
  private final String BODY_STATE = "body";
  private final String BEGIN_OPEN_ELEMENT_STATE = "begin-open-element";
  private final String OPEN_ELEMENT_STATE = "open-element";
  private final String END_OPEN_ELEMENT_STATE = "end-open-element";
  private final String CLOSE_ELEMENT_STATE = "close-element";
  private final String END_CLOSE_ELEMENT_STATE = "end-close-element";
  private final String SPECIAL_ELEMENT_STATE = "special-element";
  private final String EMPTY_ELEMENT_STATE = "empty-element";
  private final String ATTRIBUTE_NAME_STATE = "attribute-name";
  private final String ATTRIBUTE_EQ_STATE = "attribute-eq";
  private final String ATTRIBUTE_SQUOTE_STATE_1 = "attribute-single-quote-1";
  private final String ATTRIBUTE_SQUOTE_STATE_2 = "attribute-single-quote-2";
  private final String ATTRIBUTE_SQUOTE_STATE_3 = "attribute-single-quote-3";
  private final String ATTRIBUTE_DQUOTE_STATE_1 = "attribute-double-quote-1";
  private final String ATTRIBUTE_DQUOTE_STATE_2 = "attribute-double-quote-2";
  private final String ATTRIBUTE_DQUOTE_STATE_3 = "attribute-double-quote-3";

  private final String NO_EMIT = null;
  private final String EMIT_WHITESPACE = "whitespace";
  private final String EMIT_ERROR = "error";
  private final String EMIT_BODY = "body";
  private final String EMIT_BEGIN_OPEN_ELEMENT = "begin-open-element";
  private final String EMIT_OPEN_ELEMENT = "open-element";
  private final String EMIT_END_OPEN_ELEMENT = "end-open-element";
  private final String EMIT_BEGIN_CLOSE_ELEMENT = "begin-close-element";
  private final String EMIT_CLOSE_ELEMENT = "close-element";
  private final String EMIT_END_CLOSE_ELEMENT = "end-close-element";
  private final String EMIT_SPECIAL_ELEMENT = "special-element";
  private final String EMIT_EMPTY_ELEMENT = "empty-element";
  private final String EMIT_ATTRIBUTE_NAME = "attribute-name";
  private final String EMIT_ATTRIBUTE_EQ = "attribute-eq";
  private final String EMIT_ATTRIBUTE_VALUE = "attribute-value";
  private final String EMIT_OPEN_QUOTE = "open-quote";
  private final String EMIT_CLOSE_QUOTE = "close-quote";

  private final String WHITESPACE = " \t\n\f\r";

  private StrategyInclusive addState(String stateName, String anyInputNextState, StorageType anyInputStorageType, String anyInputEmit) throws DuplicateInputException, DuplicateStateException {
    var inclusive = new StrategyInclusive ();
            //.addAction ('\0', new Action (DEFAULT_STATE, StorageType.DONT_STORE_INPUT, nullInputEmit));
    addState(new State (stateName)
            .addStrategy (inclusive)
            .addStrategy (new StrategyAny (new Action (anyInputNextState, anyInputStorageType, anyInputEmit)))
    );
    return inclusive;
  }

  public XmlScanner() {
    super();

    try {
      addState(ERROR_STATE, ERROR_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      addState(DEFAULT_STATE, BODY_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("<", BEGIN_OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_BEGIN_OPEN_ELEMENT);

      addState(BODY_STATE, BODY_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("<", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_BODY);

      addState(BEGIN_OPEN_ELEMENT_STATE, OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("/", CLOSE_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_BEGIN_CLOSE_ELEMENT)
              .addAction ("!?", SPECIAL_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      addState(OPEN_ELEMENT_STATE, OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("/>", END_OPEN_ELEMENT_STATE, StorageType.DONT_STORE_INPUT, EMIT_OPEN_ELEMENT)
              .addAction (WHITESPACE, END_OPEN_ELEMENT_STATE, StorageType.DONT_STORE_INPUT, EMIT_OPEN_ELEMENT);

      addState(END_OPEN_ELEMENT_STATE, ATTRIBUTE_NAME_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("/", EMPTY_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_END_OPEN_ELEMENT)
              .addAction (WHITESPACE, END_OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_WHITESPACE);

      addState(CLOSE_ELEMENT_STATE, CLOSE_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (">", END_CLOSE_ELEMENT_STATE, StorageType.DONT_STORE_INPUT, EMIT_CLOSE_ELEMENT);

      addState(END_CLOSE_ELEMENT_STATE, ERROR_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ERROR)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_END_CLOSE_ELEMENT);

      addState(SPECIAL_ELEMENT_STATE, SPECIAL_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_SPECIAL_ELEMENT);

      addState(EMPTY_ELEMENT_STATE, ERROR_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ERROR)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_EMPTY_ELEMENT);

      addState(ATTRIBUTE_NAME_STATE, ATTRIBUTE_NAME_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (">", END_OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_NAME)
              .addAction (WHITESPACE + "=", ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_NAME);

      addState(ATTRIBUTE_EQ_STATE, ERROR_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ERROR)
              .addAction ("=", ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("'", ATTRIBUTE_SQUOTE_STATE_1, StorageType.DONT_STORE_INPUT, EMIT_ATTRIBUTE_EQ)
              .addAction ("\"", ATTRIBUTE_DQUOTE_STATE_1, StorageType.DONT_STORE_INPUT, EMIT_ATTRIBUTE_EQ)
              .addAction (WHITESPACE, ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      addState(ATTRIBUTE_SQUOTE_STATE_1, ATTRIBUTE_SQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_OPEN_QUOTE);

      addState(ATTRIBUTE_SQUOTE_STATE_2, ATTRIBUTE_SQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("'", ATTRIBUTE_SQUOTE_STATE_3, StorageType.DONT_STORE_INPUT, EMIT_ATTRIBUTE_VALUE);

      addState(ATTRIBUTE_SQUOTE_STATE_3, END_OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_CLOSE_QUOTE);

      addState(ATTRIBUTE_DQUOTE_STATE_1, ATTRIBUTE_DQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_OPEN_QUOTE);

      addState(ATTRIBUTE_DQUOTE_STATE_2, ATTRIBUTE_DQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("'\"", ATTRIBUTE_DQUOTE_STATE_3, StorageType.DONT_STORE_INPUT, EMIT_ATTRIBUTE_VALUE);

      addState(ATTRIBUTE_DQUOTE_STATE_3, END_OPEN_ELEMENT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_CLOSE_QUOTE);

    } catch (Exception exc) {
      // need to handle duplicate input and duplicate state
    }
  }

  @Override
  public void emit (String actionEmit, String token, String nextStateName) {

  }
}
