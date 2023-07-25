package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

public class Equality extends BooleanExpr {
    public static final String EQUALITY = "=";

    private final Expr left;
    private final Expr right;

    public Equality (BagObject expr) {
        left = Exprs.get (expr.getObject (LEFT));
        right = Exprs.get (expr.getObject (RIGHT));
    }

    @Override
    public Object evaluate (Bag bag) {
        var leftResult = left.evaluate (bag);
        var rightResult = right.evaluate (bag);
        return (leftResult != null) ? leftResult.equals (rightResult) : (rightResult == null);
    }

    public static BagObject bag (BagObject left, BagObject right) {
        return bag (EQUALITY, left, right);
    }
}
