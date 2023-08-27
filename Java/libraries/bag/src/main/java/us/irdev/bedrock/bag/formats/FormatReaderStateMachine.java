package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;

public class FormatReaderStateMachine extends FormatReader {

  private class Token {
    String buffer;
    String type;
  }

  private class State {
    String nextState;

  }

  private BagObject states;
  private String currentState;

  private FormatReaderStateMachine() {}

  public FormatReaderStateMachine(String input, BagObject states, String startState) {
    super (input);
    this.states = states;
    currentState = startState;
  }

  public FormatReaderStateMachine(String input, BagObject states) {
    this (input, states, "start");
  }

  private Token read () {
    return null;
  }
}
