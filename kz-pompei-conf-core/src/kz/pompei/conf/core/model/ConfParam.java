package kz.pompei.conf.core.model;

import java.util.ArrayList;
import java.util.List;

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
}
