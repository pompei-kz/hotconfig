package kz.pompei.conf.core.model;

import java.util.ArrayList;
import java.util.List;

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
}
