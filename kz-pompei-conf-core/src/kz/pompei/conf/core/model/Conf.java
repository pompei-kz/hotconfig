package kz.pompei.conf.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to store in RAM configuration file data
 */
public class Conf {

  /**
   * List of comment lines for the configuration file
   */
  public List<String> confComments = new ArrayList<>();


  /**
   * List of parameters in the configuration file
   */
  public List<ConfParam> params = new ArrayList<>();
}
