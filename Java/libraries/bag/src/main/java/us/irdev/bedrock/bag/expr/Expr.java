package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

abstract public class Expr {
    public static final String OPERATOR = "operator";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    abstract Object evaluate (Bag bag);

    static BagObject bag (String operator, BagObject left, BagObject right) {
        return new BagObject ()
                .put (OPERATOR, operator)
                .put (LEFT, left)
                .put (RIGHT, right);
    }

    static BagObject bag (String operator, BagObject left) {
        return new BagObject ()
                .put (OPERATOR, operator)
                .put (LEFT, left);
    }

    static BagObject bag (String operator) {
        return new BagObject ()
                .put (OPERATOR, operator);
    }
}
