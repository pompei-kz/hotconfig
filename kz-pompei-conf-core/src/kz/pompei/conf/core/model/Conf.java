package kz.pompei.conf.core.model;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/**
 * In-memory representation of one configuration file.
 */
public class Conf {

  /**
   * Comment lines that describe the whole configuration file.
   * <p>
   * Each item stores the comment text without the leading {@code #} marker.
   */
  public List<String> confComments = new ArrayList<>();

  /**
   * Parameters contained in the configuration file, in file order.
   */
  public List<ConfParam> params = new ArrayList<>();

  public@NonNull Conf copy() {
    // TODO: Implement Conf.copy() to create a deep copy of the configuration
    throw new RuntimeException("2026-05-19 07:20 Not impl yet Conf.copy()");
  }
}
