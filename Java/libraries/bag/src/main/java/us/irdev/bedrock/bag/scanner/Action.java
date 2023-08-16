package us.irdev.bedrock.bag.scanner;

public class Action {

  private final String nextState;
  private final StorageType storage;
  private final String emit;

  public Action (String nextState, StorageType storage, String emit) {
    this.nextState = nextState;
    this.storage = storage;
    this.emit = emit;
  }

  public Action (String nextState, StorageType storage) {
    this (nextState, storage, null);
  }

  public String getNextState () {
    return nextState;
  }

  public StorageType getStorage () {
    return storage;
  }

  public String getEmit () {
    return emit;
  }
}
