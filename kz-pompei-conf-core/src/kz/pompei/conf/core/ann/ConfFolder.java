package kz.pompei.conf.core.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the folder where configuration files are located.
 * <p>
 * This annotation is specified for the configuration interface.
 * <p>
 * If this annotation is not specified, configuration files will be searched for in the base directory.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfFolder {
  String value() default "";
}
