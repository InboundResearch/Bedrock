package us.irdev.bedrock.bag.scanner;

public record Action<StateIdType, EmitTokenType>(StateIdType nextStateId, boolean captureInput, EmitTokenType emitToken) { }
