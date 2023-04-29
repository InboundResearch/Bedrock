package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

public class Value extends Expr {
    public static final String VALUE = "value";

    private final String value;

    public Value (BagObject expr) {
        value = expr.getString (VALUE);
    }

    @Override
    public Object evaluate (Bag bag) {
        return value;
    }

    public static BagObject bag (Object value) {
        return bag (VALUE).put (VALUE, value);
    }
}
