package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

public class Not extends BooleanExpr {
    public static final String NOT = "not";

    private final BooleanExpr left;

    public Not (BagObject expr) {
        left = (BooleanExpr) Exprs.get (expr.getObject (LEFT));
    }

    @Override
    Object evaluate (Bag bag) {
        return ! left.evaluateIsTrue (bag);
    }

    public static BagObject bag (BagObject left) {
        return bag (NOT, left);
    }
}
