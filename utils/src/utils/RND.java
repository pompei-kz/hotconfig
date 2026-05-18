package utils;

import lombok.NonNull;

public class RND {

  public static @NonNull String str(int length) {
    String        chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb    = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int index = (int) (Math.random() * chars.length());
      sb.append(chars.charAt(index));
    }
    return sb.toString();
  }
}
