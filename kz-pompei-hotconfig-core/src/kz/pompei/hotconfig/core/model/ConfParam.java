package kz.pompei.hotconfig.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

  /**
   * Error message if parameter parsing failed.
   */
  public @Nullable String error;

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

  @Override public boolean equals(Object o) {
    if (!(o instanceof ConfParam confParam)) return false;
    //@formatter:off
    return Objects.equals(comments , confParam.comments )
        && Objects.equals(name     , confParam.name     )
        && Objects.equals(valueStr , confParam.valueStr )
        && Objects.equals(error    , confParam.error    );
    //@formatter:on
  }

  @Override public int hashCode() {
    return Objects.hash(comments, name, valueStr, error);
  }
}
