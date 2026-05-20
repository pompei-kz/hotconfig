package kz.pompei.hotconfig.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * In-memory representation of one configuration parameter.
 */
public class ConfParam {

  /**
   * Comment lines that describe this parameter.
   * <p>
   * Each item stores the comment text without the leading {@code #} marker.
   */
  public List<String> comments = new ArrayList<>();

  /**
   * Parameter name used on the left side of the {@code name=value} line.
   */
  public String name;

  /**
   * Parameter value as a string.
   */
  public String valueStr;

  public ConfParam() {}

  public ConfParam(String name, String valueStr) {
    this.name     = name;
    this.valueStr = valueStr;
  }

  public ConfParam comment(@Nullable String comment) {
    if (comment != null) {
      Collections.addAll(comments, comment.split("\n"));
    }
    return this;
  }
}
