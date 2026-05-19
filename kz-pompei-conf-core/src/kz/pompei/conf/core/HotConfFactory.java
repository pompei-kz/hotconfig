package kz.pompei.conf.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import kz.pompei.conf.core.ann.ConfDefaultValue;
import kz.pompei.conf.core.ann.ConfDoc;
import kz.pompei.conf.core.ann.ConfFolder;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import kz.pompei.conf.core.model.HotConfFactoryParams;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

public class HotConfFactory {
  private final @NonNull ConfTunnel           tunnel;
  private final @NonNull HotConfFactoryParams params;
  private final @NonNull DynamicParams        dynamicParams;
  private final @NonNull AtomicLong           lastReadMs  = new AtomicLong(0);
  private final @NonNull AtomicBoolean        refreshable = new AtomicBoolean(false);

  public HotConfFactory(@NonNull ConfTunnel tunnel, @NonNull HotConfFactoryParams params, @NonNull DynamicParams dynamicParams) {
    this.tunnel        = tunnel;
    this.params        = params;
    this.dynamicParams = dynamicParams;
  }

  public HotConfFactory(@NonNull ConfTunnel tunnel) {
    this(tunnel, HotConfFactoryParams.builder().build(), DynamicParams.REAL);
  }

  @RequiredArgsConstructor
  private static class Dot {
    final Conf                conf;
    final long                modificationMarker;
    final Map<String, Object> valueMap;
    final AtomicBoolean       touched = new AtomicBoolean(true);
  }

  private final ConcurrentHashMap<String, Dot> path_to_conf = new ConcurrentHashMap<>();

  public @NonNull <I> I createConf(@NonNull Class<I> confClass) {

    ConfFolder annConfFolder = confClass.getAnnotation(ConfFolder.class);
    String     folder        = annConfFolder == null ? null : annConfFolder.value();

    for (Method method : confClass.getMethods()) {
      if (method.getParameterCount() > 0) {
        throw new RuntimeException("vrA1Lf8sHR :: Method `" + confClass.getSimpleName() + "." + method.getName() + "`" +
                                     " with parameters is not supported");
      }
    }

    //noinspection unchecked
    return (I) Proxy.newProxyInstance(confClass.getClassLoader(), new Class[]{confClass}, new HotConfProxyHandler(confClass, folder));
  }

  private <I> @NonNull String formConfName(@NonNull Class<I> confClass) {
    return confClass.getSimpleName() + params.extension;
  }

  private class HotConfProxyHandler implements InvocationHandler {

    private final @NonNull  Class<?> confClass;
    private final @Nullable String   folder;

    public <I> HotConfProxyHandler(@NonNull Class<I> confClass, @Nullable String folder) {
      this.confClass = confClass;
      this.folder    = folder;
    }

    @Override public @Nullable Object invoke(Object proxy, @NonNull Method method, Object[] args) {
      return switch (method.getName()) {
        case "toString" -> getClass().getSimpleName() + " for " + confClass.getSimpleName();
        case "equals" -> proxy == args[0];
        case "hashCode" -> System.identityHashCode(proxy);
        case "getClass" -> confClass;
        default -> {
          if (method.getParameterCount() > 0) {
            throw new RuntimeException("W47IxHKolP :: Method `" + confClass.getSimpleName() + "." + method.getName() + "`" +
                                         " with parameters is not supported");
          }
          {
            Dot    dot   = getDot(folder, confClass);
            Object value = dot.valueMap.get(method.getName());

            yield value;
          }
        }
      };
    }
  }

  private @NonNull Dot getDot(@Nullable String folder, @NonNull Class<?> confClass) {
    String confName  = formConfName(confClass);
    String localPath = folder == null ? confName : folder + "/" + confName;

    {
      Dot dot = path_to_conf.get(localPath);
      if (dot != null) {

        if (refreshable.get()) {
          dot.touched.set(true);
          return dot;
        }

        long lastReadMs = this.lastReadMs.longValue();
        long now        = dynamicParams.now();

        if (now < lastReadMs + this.params.revisionCheckTimeoutMs) {
          return dot;
        }

        {
          Long modificationMarker = tunnel.modificationMarker(localPath);
          if (modificationMarker != null) {
            this.lastReadMs.set(dynamicParams.now());
            if (dot.modificationMarker == modificationMarker) {
              return dot;
            }
          }
        }
      }
    }

    {
      Long modificationMarker = tunnel.modificationMarker(localPath);
      if (modificationMarker != null) {

        Conf conf = tunnel.read(localPath);

        if (conf == null) {
          modificationMarker = tunnel.modificationMarker(localPath);
          if (modificationMarker != null) {
            conf = tunnel.read(localPath);
            if (conf == null) {
              throw new RuntimeException("j22hK1Tm7q :: FATAL ERROR: tunnel.read(" + localPath + ") returns null" +
                                           " just after tunnel.modificationMarker(" + localPath + ") returns non-null." +
                                           " tunnel.class = " + tunnel.getClass());
            }
          }
        }


        if (modificationMarker != null) {
          // assert conf != null

          if (!path_to_conf.containsKey(localPath)) {
            conf = compareOrSupplementAndWriteConf(conf, confClass, localPath);
          }

          Dot dot = new Dot(conf, modificationMarker, convertConfToValueMap(conf, confClass));
          path_to_conf.put(localPath, dot);
          lastReadMs.set(dynamicParams.now());
          return dot;
        }
      }
    }

    {
      Conf conf = createDefaultConf(confClass);

      tunnel.write(localPath, conf);

      Long modificationMarker = tunnel.modificationMarker(localPath);
      if (modificationMarker == null) {
        throw new RuntimeException("j22hK1Tm7q :: FATAL ERROR: tunnel.modificationMarker(" + localPath + ") returns null" +
                                     " just after tunnel.write(...). tunnel.class = " + tunnel.getClass());
      }

      lastReadMs.set(dynamicParams.now());

      Dot dot = new Dot(conf, modificationMarker, convertConfToValueMap(conf, confClass));
      path_to_conf.put(localPath, dot);
      return dot;
    }
  }

  private @NonNull List<Method> extractConfigMethods(@NonNull Class<?> confClass) {
    List<Method> ret = new ArrayList<>();
    for (Method method : confClass.getMethods()) {
      if (method.getParameterCount() > 0) continue;
      String methodName = method.getName();
      if ("toString".equals(methodName)) continue;
      ret.add(method);
    }
    return ret;
  }

  private @NonNull Conf compareOrSupplementAndWriteConf(@NonNull Conf conf, @NonNull Class<?> confClass, @NonNull String localPath) {

    Set<String> existsParams = new HashSet<>();
    for (ConfParam param : conf.params) {
      existsParams.add(param.name);
    }

    List<Method> absentMethods = new ArrayList<>();

    for (Method method : extractConfigMethods(confClass)) {
      if (existsParams.contains(method.getName())) continue;
      absentMethods.add(method);
    }

    if (absentMethods.isEmpty()) return conf;

    Conf ret = conf.copy();

    for (Method method : absentMethods) {
      ret.params.add(convertToConfParam(method));
    }

    tunnel.write(localPath, ret);

    return ret;
  }

  private @NonNull Map<String, Object> convertConfToValueMap(@NonNull Conf conf, @NonNull Class<?> confClass) {

    Map<String, Object> ret = new HashMap<>();

    for (ConfParam param : conf.params) {
      try {
        Method method = confClass.getMethod(param.name);

        ret.put(param.name, ParseUtil.parseStrToGenericType(param.valueStr, dynamicParams, method.getGenericReturnType()));

      } catch (NoSuchMethodException e) {
        continue;
      }
    }

    return ret;
  }

  private @NonNull Conf createDefaultConf(@NonNull Class<?> confClass) {

    Conf ret = new Conf();

    {
      ConfDoc ann = confClass.getAnnotation(ConfDoc.class);
      String  doc = ann == null ? null : ann.value();
      if (doc != null) {
        ret.confComments.addAll(Arrays.asList(doc.split("\n")));
      }
    }

    for (Method method : extractConfigMethods(confClass)) {
      ret.params.add(convertToConfParam(method));
    }

    return ret;
  }

  private @NonNull ConfParam convertToConfParam(@NonNull Method method) {
    ConfParam param = new ConfParam();
    param.name = method.getName();

    {
      ConfDoc annConfDoc = method.getAnnotation(ConfDoc.class);
      String  doc        = annConfDoc == null ? null : annConfDoc.value();
      if (doc != null) {
        param.comments.addAll(Arrays.asList(doc.split("\n")));
      }
    }

    ConfDefaultValue annDefaultValue = method.getAnnotation(ConfDefaultValue.class);
    param.valueStr = annDefaultValue == null ? null : annDefaultValue.value();

    return param;
  }
}
