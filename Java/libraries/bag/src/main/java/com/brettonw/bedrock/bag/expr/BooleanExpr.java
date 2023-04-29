package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;

abstract public class BooleanExpr extends Expr {
    public boolean evaluateIsTrue (Bag bag) {
        return (Boolean) evaluate (bag);
    }
}
