package us.irdev.bedrock.bag.formats;

import java.util.Arrays;

public class Utility {
  public static char[] sortString (String string) {
    var chars = string.toCharArray ();
    Arrays.sort (chars);
    return chars;
  }
}
