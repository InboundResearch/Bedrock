package us.irdev.bedrock.bag.scanner;

public class XmlScanner extends Scanner {
  private final String DEFAULT_STATE = DEFAULT_START_STATE_NAME;
  private final String STRING_STATE = "string";
  private final String BEGIN_OPEN_TAG_STATE = "begin-open-tag";
  private final String OPEN_TAG_STATE = "open-tag";
  private final String END_OPEN_TAG_STATE = "end-open-tag";
  private final String CLOSE_TAG_STATE = "close-tag";
  private final String SPECIAL_TAG_STATE = "special-tag";
  private final String EMPTY_TAG_STATE = "empty-tag";
  private final String ATTRIBUTE_NAME_STATE = "attribute-name";
  private final String ATTRIBUTE_EQ_STATE = "attribute-eq";
  private final String ATTRIBUTE_SQUOTE_STATE_1 = "attribute-single-quote-1";
  private final String ATTRIBUTE_SQUOTE_STATE_2 = "attribute-single-quote-2";
  private final String ATTRIBUTE_DQUOTE_STATE_1 = "attribute-double-quote-1";
  private final String ATTRIBUTE_DQUOTE_STATE_2 = "attribute-double-quote-2";

  private final String NO_EMIT = null;
  private final String EMIT_WHITESPACE = "whitespace";
  private final String EMIT_ERROR = "error";
  private final String EMIT_STRING = "string";
  private final String EMIT_OPEN_TAG = "open-tag";
  private final String EMIT_END_OPEN_TAG = "end-open-tag";
  private final String EMIT_CLOSE_TAG = "close-tag";
  private final String EMIT_SPECIAL_TAG = "special-tag";
  private final String EMIT_EMPTY_TAG = "empty-tag";
  private final String EMIT_ATTRIBUTE_NAME = "attribute-name";
  private final String EMIT_ATTRIBUTE_EQ = "attribute-eq";
  private final String EMIT_ATTRIBUTE_VALUE = "attribute-value";

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
      /*
      OnAnyInput          (DEFAULT_STATE,                     STRING_STATE,               StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (DEFAULT_STATE,             "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       NO_EMIT                 );
      OnInput             (DEFAULT_STATE,             "<",    BEGIN_OPEN_TAG_STATE,       StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnWhitespaceInput   (DEFAULT_STATE,                     DEFAULT_STATE,              StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_WHITESPACE         );
      */
      addState(DEFAULT_STATE, STRING_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, NO_EMIT)
              .addAction ("<", BEGIN_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (WHITESPACE, BEGIN_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_WHITESPACE);

      /*
      OnAnyInput          (STRING_STATE,                      STRING_STATE,               StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (STRING_STATE,              "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (STRING_STATE,              "<",    DEFAULT_STATE,              StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_STRING             );
      */
      addState(STRING_STATE, STRING_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction ("<", DEFAULT_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_STRING);


      /*
      OnAnyInput          (BEGIN_OPEN_TAG_STATE,              OPEN_TAG_STATE,             StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (BEGIN_OPEN_TAG_STATE,      "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (BEGIN_OPEN_TAG_STATE,      "/",    CLOSE_TAG_STATE,            StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (BEGIN_OPEN_TAG_STATE,      "!",    SPECIAL_TAG_STATE,          StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (BEGIN_OPEN_TAG_STATE,      "?",    SPECIAL_TAG_STATE,          StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      */
      addState(BEGIN_OPEN_TAG_STATE, OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction ("/", CLOSE_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("!?", SPECIAL_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      /*
      OnAnyInput          (OPEN_TAG_STATE,                    OPEN_TAG_STATE,             StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (OPEN_TAG_STATE,            "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (OPEN_TAG_STATE,            "/",    END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_OPEN_TAG           );
      OnInput             (OPEN_TAG_STATE,            ">",    END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_OPEN_TAG           );
      OnWhitespaceInput   (OPEN_TAG_STATE,                    END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_OPEN_TAG           );
      */
      addState(OPEN_TAG_STATE, OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (WHITESPACE + "/>", END_OPEN_TAG_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_OPEN_TAG);


      /*
      OnAnyInput          (END_OPEN_TAG_STATE,                ATTRIBUTE_NAME_STATE,       StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (END_OPEN_TAG_STATE,        "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (END_OPEN_TAG_STATE,        "/",    EMPTY_TAG_STATE,            StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (END_OPEN_TAG_STATE,        ">",    DEFAULT_STATE,              StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_END_OPEN_TAG       );
      OnWhitespaceInput   (END_OPEN_TAG_STATE,                END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_WHITESPACE         );
      */
      addState(END_OPEN_TAG_STATE, ATTRIBUTE_NAME_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction ("/", EMPTY_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_END_OPEN_TAG)
              .addAction (WHITESPACE, END_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_WHITESPACE);

      /*
      OnAnyInput          (CLOSE_TAG_STATE,                   CLOSE_TAG_STATE,            StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (CLOSE_TAG_STATE,           "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (CLOSE_TAG_STATE,           ">",    DEFAULT_STATE,              StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_CLOSE_TAG          );
      */
      addState(CLOSE_TAG_STATE, CLOSE_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_CLOSE_TAG);

      /*
      OnAnyInput          (SPECIAL_TAG_STATE,                 SPECIAL_TAG_STATE,          StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (SPECIAL_TAG_STATE,         "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (SPECIAL_TAG_STATE,         ">",    DEFAULT_STATE,              StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_SPECIAL_TAG        );
      */
      addState(SPECIAL_TAG_STATE, SPECIAL_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_SPECIAL_TAG);

      /*
      OnAnyInput          (EMPTY_TAG_STATE,                   END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (EMPTY_TAG_STATE,           "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (EMPTY_TAG_STATE,           ">",    DEFAULT_STATE,              StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_EMPTY_TAG          );
      */
      addState(EMPTY_TAG_STATE, END_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (">", DEFAULT_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_EMPTY_TAG);

      /*
      OnAnyInput          (ATTRIBUTE_NAME_STATE,              ATTRIBUTE_NAME_STATE,       StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (ATTRIBUTE_NAME_STATE,      "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (ATTRIBUTE_NAME_STATE,      ">",    END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ATTRIBUTE_NAME     );
      OnInput             (ATTRIBUTE_NAME_STATE,      "=",    ATTRIBUTE_EQ_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ATTRIBUTE_NAME     );
      OnWhitespaceInput   (ATTRIBUTE_NAME_STATE,              ATTRIBUTE_EQ_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ATTRIBUTE_NAME     );
      */
      addState(ATTRIBUTE_NAME_STATE, ATTRIBUTE_NAME_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (">", END_OPEN_TAG_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_NAME)
              .addAction (WHITESPACE + "=", ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_NAME);

      /*
      OnAnyInput          (ATTRIBUTE_EQ_STATE,                ATTRIBUTE_EQ_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_ERROR              );
      OnInput             (ATTRIBUTE_EQ_STATE,        "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (ATTRIBUTE_EQ_STATE,        ">",    END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ERROR              );
      OnInput             (ATTRIBUTE_EQ_STATE,        "=",    ATTRIBUTE_EQ_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (ATTRIBUTE_EQ_STATE,        "\'",   ATTRIBUTE_SQUOTE_STATE_1,   StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ATTRIBUTE_EQ       );
      OnInput             (ATTRIBUTE_EQ_STATE,        "\"",   ATTRIBUTE_DQUOTE_STATE_1,   StorageType.STORE_INPUT_AFTER_EMIT,      EMIT_ATTRIBUTE_EQ       );
      OnWhitespaceInput   (ATTRIBUTE_EQ_STATE,                ATTRIBUTE_EQ_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      */
      addState(ATTRIBUTE_EQ_STATE, ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ERROR)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction (">", END_OPEN_TAG_STATE, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ERROR)
              .addAction ("=", ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("'", ATTRIBUTE_SQUOTE_STATE_1, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_EQ)
              .addAction ("\"", ATTRIBUTE_DQUOTE_STATE_1, StorageType.STORE_INPUT_AFTER_EMIT, EMIT_ATTRIBUTE_EQ)
              .addAction (WHITESPACE, ATTRIBUTE_EQ_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      /*
      OnAnyInput          (ATTRIBUTE_SQUOTE_STATE_1,          ATTRIBUTE_SQUOTE_STATE_2,   StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      */
      addState(ATTRIBUTE_SQUOTE_STATE_1, ATTRIBUTE_SQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      /*
      OnAnyInput          (ATTRIBUTE_SQUOTE_STATE_2,          ATTRIBUTE_SQUOTE_STATE_2,   StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (ATTRIBUTE_SQUOTE_STATE_2,  "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (ATTRIBUTE_SQUOTE_STATE_2,  "\'",   END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_ATTRIBUTE_VALUE    );
      */
      addState(ATTRIBUTE_SQUOTE_STATE_2, ATTRIBUTE_SQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction ("'", END_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ATTRIBUTE_VALUE);

      /*
      OnAnyInput          (ATTRIBUTE_DQUOTE_STATE_1,          ATTRIBUTE_DQUOTE_STATE_2,   StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      */
      addState(ATTRIBUTE_DQUOTE_STATE_1, ATTRIBUTE_DQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT);

      /*
      OnAnyInput          (ATTRIBUTE_DQUOTE_STATE_2,          ATTRIBUTE_DQUOTE_STATE_2,   StorageType.STORE_INPUT_BEFORE_EMIT,     NO_EMIT                 );
      OnInput             (ATTRIBUTE_DQUOTE_STATE_2,  "\0",   DEFAULT_STATE,              StorageType.DONT_STORE_INPUT,       EMIT_ERROR              );
      OnInput             (ATTRIBUTE_DQUOTE_STATE_2,  "\"",   END_OPEN_TAG_STATE,         StorageType.STORE_INPUT_BEFORE_EMIT,     EMIT_ATTRIBUTE_VALUE    );
      */
      addState(ATTRIBUTE_DQUOTE_STATE_2, ATTRIBUTE_DQUOTE_STATE_2, StorageType.STORE_INPUT_BEFORE_EMIT, NO_EMIT)
              .addAction ("\0", DEFAULT_STATE, StorageType.DONT_STORE_INPUT, EMIT_ERROR)
              .addAction ("'", END_OPEN_TAG_STATE, StorageType.STORE_INPUT_BEFORE_EMIT, EMIT_ATTRIBUTE_VALUE);

    } catch (Exception exc) {
      // need to handle duplicate input and duplicate state
    }
  }

  @Override
  public void emit (String actionEmit, String token) {

  }
}
