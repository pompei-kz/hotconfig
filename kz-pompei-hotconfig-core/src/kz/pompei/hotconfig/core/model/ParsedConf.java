package kz.pompei.hotconfig.core.model;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

public class ParsedConf {
  public final Map<String, Object> paramName_to_value = new HashMap<>();
  public final Map<String, String> paramName_to_error = new HashMap<>();

  public void applyErrorsTo(@NonNull Conf conf) {

    for (ConfParam param : conf.params) {
      String paramName = param.name;
      String error     = paramName_to_error.get(paramName);

      param.error = error;
    }

  }
}
