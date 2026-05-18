package kz.pompei.conf.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to store in RAM a configuration file parameter
 */
public class ConfParam {

  /**
   * List of comment lines for the configuration parameter
   */
  public List<String> comments = new ArrayList<>();

  /**
   * Name of configuration parameter
   */
  public String name;

  /**
   * String value of configuration parameter
   */
  public String valueStr;
}
