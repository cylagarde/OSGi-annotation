package cl.annotation;

import java.util.function.Predicate;

import org.osgi.framework.ServiceReference;

/**
 * The class <b>DefaultServiceReferencePredicate</b> allows to.<br>
 */
public class DefaultServiceReferencePredicate implements Predicate<ServiceReference<?>>
{
  @Override
  public boolean test(ServiceReference<?> t)
  {
    return true;
  }
}
