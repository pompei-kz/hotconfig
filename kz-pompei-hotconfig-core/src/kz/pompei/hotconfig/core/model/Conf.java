package kz.pompei.hotconfig.core.model;

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

  public @NonNull Conf copy() {
    Conf result = new Conf();
    result.confComments = new ArrayList<>(confComments);
    for (ConfParam param : params) {
      ConfParam resultParam = new ConfParam();
      resultParam.comments = new ArrayList<>(param.comments);
      resultParam.name     = param.name;
      resultParam.valueStr = param.valueStr;
      result.params.add(resultParam);
    }
    return result;
  }
}
