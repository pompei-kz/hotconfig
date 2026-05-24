package kz.pompei.hotconfig.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
      resultParam.error = param.error;
      result.params.add(resultParam);
    }
    return result;
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Conf conf)) return false;
    return Objects.equals(confComments, conf.confComments) && Objects.equals(params, conf.params);
  }

  @Override public int hashCode() {
    return Objects.hash(confComments, params);
  }
}
