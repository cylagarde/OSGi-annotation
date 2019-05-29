package cl.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OSGiNamed
{
  /** service name 'component.name' */
  String value() default "";

  /** service property */
  String[] property() default {};

  /** component filter in LDAP format */
  String filter() default "";

  /** take highest 'service.ranking' property if multiple otherwise throw exception */
  boolean takeHighestRankingIfMultiple() default true;

  /** service annotations */
  Class<? extends Annotation>[] annotations() default {};

  /** service types */
  Class<?>[] types() default {};
}