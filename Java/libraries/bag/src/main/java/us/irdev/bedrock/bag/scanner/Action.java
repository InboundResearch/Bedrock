package us.irdev.bedrock.bag.scanner;

public record Action(String nextState, boolean capture, String emit) { }
