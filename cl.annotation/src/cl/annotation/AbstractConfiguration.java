package cl.annotation;

import java.lang.annotation.Annotation;

/**
 * The class <b>AbstractConfiguration</b> allows to.<br>
 */
public abstract class AbstractConfiguration implements OSGiNamed
{
  @Override
  public final Class<AbstractConfiguration> configuration()
  {
    return null;
  }

  @Override
  public final Class<? extends Annotation> annotationType()
  {
    return OSGiNamed.class;
  }

  @Override
  public String[] name()
  {
    return null;
  }

  @Override
  public String[] property()
  {
    return null;
  }

  @Override
  public String filter()
  {
    return null;
  }

  @Override
  public boolean takeHighestRankingIfMultiple()
  {
    return false;
  }

  @Override
  public Class<? extends Annotation>[] annotation()
  {
    return null;
  }

  @Override
  public Class<? extends Annotation>[] notHaveAnnotation()
  {
    return null;
  }

  @Override
  public Class<?>[] type()
  {
    return null;
  }

  @Override
  public Class<?>[] notHaveType()
  {
    return null;
  }

  @Override
  public String[] bundleName()
  {
    return null;
  }

  @Override
  public String[] bundleVersionRange()
  {
    return null;
  }
}
