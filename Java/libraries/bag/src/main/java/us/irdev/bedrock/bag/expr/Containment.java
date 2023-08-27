package us.irdev.bedrock.bag.expr;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagObject;

public class Containment extends BooleanExpr {
  public static final String CONTAINMENT = "#";

  private final Expr left;
  private final Expr right;

  public Containment (BagObject expr) {
    left = Exprs.get (expr.getObject (LEFT));
    right = Exprs.get (expr.getObject (RIGHT));
  }

  @Override
  public Object evaluate (Bag bag) {
    var leftResult = (String) (left.evaluate (bag));
    var rightResult = (String) (right.evaluate (bag));
    return (leftResult != null) && (rightResult != null) && leftResult.contains (rightResult);
  }

  public static BagObject bag (BagObject left, BagObject right) {
    return bag (CONTAINMENT, left, right);
  }
}
