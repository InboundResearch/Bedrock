package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

public class And extends BooleanExpr {
    public static final String AND = "and";

    private final BooleanExpr left;
    private final BooleanExpr right;

    public And (BagObject expr) {
        left = (BooleanExpr) Exprs.get (expr.getObject (LEFT));
        right = (BooleanExpr) Exprs.get (expr.getObject (RIGHT));
    }

    @Override
    Object evaluate (Bag bag) {
        return left.evaluateIsTrue (bag) && right.evaluateIsTrue (bag);
    }

    public static BagObject bag (BagObject left, BagObject right) {
        return bag (AND, left, right);
    }
}
