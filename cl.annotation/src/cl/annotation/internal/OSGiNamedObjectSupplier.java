package cl.annotation.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.VersionRange;
import org.osgi.service.component.annotations.Component;

import cl.annotation.OSGiNamed;

/**
 * The class <b>OSGiNamedObjectSupplier</b> allows to select object injected.<br>
 */
@Component(service = ExtendedObjectSupplier.class,
  property = "dependency.injection.annotation=cl.annotation.OSGiNamed")
public final class OSGiNamedObjectSupplier extends ExtendedObjectSupplier
{
  private static final Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
  private static final BundleContext bundleContext = bundle.getBundleContext();

  private final Map<Class<?>, Set<IRequestor>> listeners = new ConcurrentHashMap<>();
  private final Map<IRequestor, ServiceListener> serviceListeners = new ConcurrentHashMap<>();

  @Override
  public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group)
  {
    OSGiNamed osgiNamed = descriptor.getQualifier(OSGiNamed.class);
    String[] names = osgiNamed.name();
    String[] property = osgiNamed.property();
    String filter = osgiNamed.filter();
    boolean takeHighestRankingIfMultiple = osgiNamed.takeHighestRankingIfMultiple();
    Class<? extends Annotation>[] annotations = osgiNamed.annotation();
    Class<? extends Annotation>[] notHaveAnnotations = osgiNamed.notHaveAnnotation();
    Class<?>[] types = osgiNamed.type();
    Class<?>[] notHaveTypes = osgiNamed.notHaveType();
    String[] bundleNames = osgiNamed.bundleName();
    String[] bundleVersionRanges = osgiNamed.bundleVersionRange();

    Type desiredType = descriptor.getDesiredType();
    Class<?> desiredClass = getDesiredClass(desiredType);
    if (desiredClass == null)
      return IInjector.NOT_A_VALUE;

    // take account for collection
    boolean isCollection = false;
    if (Collection.class.equals(desiredClass))
    {
      isCollection = true;
      Type genericType = getGenericTypeForCollection(desiredType);
      if (genericType == null)
        return IInjector.NOT_A_VALUE;
      desiredType = genericType;
    }

    String typeName = desiredType.getTypeName();
    String generatedFilter = generateFilter(names, property, filter);

    ServiceListener serviceListener = requestor == null? null : serviceListeners.get(requestor);
    if (track && requestor != null)
    {
      addListener(desiredClass, requestor);

      if (serviceListener != null)
        bundleContext.removeServiceListener(serviceListener);

      try
      {
        String trackingFilter = "(objectClass=" + typeName + ")";
        if (generatedFilter != null)
          trackingFilter = "(&" + trackingFilter + generatedFilter + ")";

        serviceListener = event -> notifyRequestor(requestor);
        serviceListeners.put(requestor, serviceListener);

        // add tracking
        bundleContext.addServiceListener(serviceListener, trackingFilter);
      }
      catch(Exception e)
      {
        return new InjectionException(e);
      }
    }
    else if (serviceListener != null && requestor != null)
    {
      removeListener(desiredClass, requestor);

      bundleContext.removeServiceListener(serviceListener);
      serviceListeners.remove(requestor);
    }

    // get all service references
    ServiceReference<?>[] refs = null;
    try
    {
      refs = bundleContext.getAllServiceReferences(typeName, generatedFilter);
    }
    catch(Exception e)
    {
      throw new InjectionException(e);
    }

    //
    Status status = new Status();
    status.refs = refs;

    status.filterBundles(bundleNames, bundleVersionRanges);
    status.filterAnnotations(annotations);
    status.filterNotHaveAnnotations(notHaveAnnotations);
    status.filterTypes(types);
    status.filterNotHaveTypes(notHaveTypes);

    //
    if (isCollection)
      return status.getServices();

    //
    int serviceCount = status.serviceCount();
    if (serviceCount == 0)
    {
      if (descriptor.hasQualifier(Optional.class))
        return null;
      return IInjector.NOT_A_VALUE;
    }

    //
    if (takeHighestRankingIfMultiple || serviceCount == 1)
    {
      Object service = status.getFirstService();
      return service;
    }

    throw new InjectionException("Unable to process \"" + requestor + "\": " + serviceCount + " values were found for the argument \"" + descriptor + "\"");
  }

  private static String generateFilter(String[] names, String[] property, String filter)
  {
    boolean multipleFilter = false;

    String generatedFilter = null;
    if (names.length != 0)
    {
      generatedFilter = "";
      for(String name : names)
        generatedFilter += "(component.name=" + name + ")";
      if (names.length > 1)
        generatedFilter = "(|" + generatedFilter + ")";
    }

    if (property.length != 0)
    {
      if (generatedFilter != null || property.length > 1)
        multipleFilter = true;
      if (generatedFilter == null)
        generatedFilter = "";
      for(String p : property)
        generatedFilter += "(" + p + ")";
    }

    if (!"".equals(filter))
    {
      if (generatedFilter != null)
        multipleFilter = true;
      else
        generatedFilter = "";
      generatedFilter += filter;
    }

    if (multipleFilter)
      generatedFilter = "(&" + generatedFilter + ")";
    return generatedFilter;
  }

  private final class Status
  {
    ServiceReference<?>[] refs;
    List<Object> services = null;

    void filterAnnotations(Class<? extends Annotation>[] annotations)
    {
      if (annotations.length != 0)
      {
        fillAllServices();

        for(int s = services.size() - 1; s >= 0; s--)
        {
          Object service = services.get(s);
          Class<?> serviceClass = service.getClass();

          // check all annotations
          for(int a = 0; a < annotations.length; a++)
          {
            // if annotation not found then remove service
            if (serviceClass.getAnnotation(annotations[a]) == null)
            {
              services.remove(s);
              break;
            }
          }
        }
      }
    }

    void filterNotHaveAnnotations(Class<? extends Annotation>[] notHaveAnnotations)
    {
      if (notHaveAnnotations.length != 0)
      {
        fillAllServices();

        for(int s = services.size() - 1; s >= 0; s--)
        {
          Object service = services.get(s);
          Class<?> serviceClass = service.getClass();

          // check all annotations
          for(int a = 0; a < notHaveAnnotations.length; a++)
          {
            // if annotation found then remove service
            if (serviceClass.getAnnotation(notHaveAnnotations[a]) != null)
            {
              services.remove(s);
              break;
            }
          }
        }
      }
    }

    void filterTypes(Class<?>[] types)
    {
      if (types.length != 0)
      {
        fillAllServices();

        for(int s = services.size() - 1; s >= 0; s--)
        {
          Object service = services.get(s);

          // check all types
          for(int t = 0; t < types.length; t++)
          {
            // if service is not instance of type then remove service
            if (!types[t].isInstance(service))
            {
              services.remove(s);
              break;
            }
          }
        }
      }
    }

    void filterNotHaveTypes(Class<?>[] notHaveTypes)
    {
      if (notHaveTypes.length != 0)
      {
        fillAllServices();

        for(int s = services.size() - 1; s >= 0; s--)
        {
          Object service = services.get(s);

          // check all types
          for(int t = 0; t < notHaveTypes.length; t++)
          {
            // if service is instance of type then remove service
            if (notHaveTypes[t].isInstance(service))
            {
              services.remove(s);
              break;
            }
          }
        }
      }
    }

    void filterBundles(String[] bundleNames, String[] bundleVersionRanges)
    {
      if (bundleNames.length != 0 || bundleVersionRanges.length != 0)
      {
        // check same arrays size
        if (bundleVersionRanges.length != 0)
        {
          if (bundleVersionRanges.length == 1)
          {
            if (bundleNames.length > 1)
              throw new InjectionException("bundleNames have " + bundleNames.length + " entries but bundleVersionRanges have " + bundleVersionRanges.length + " entries");
          }
          else
          {
            if (bundleNames.length != bundleVersionRanges.length)
              throw new InjectionException("bundleNames have " + bundleNames.length + " entries but bundleVersionRanges have " + bundleVersionRanges.length + " entries");
          }
        }

        if (refs == null)
          return;

        Pattern[] patterns = new Pattern[bundleNames.length];
        services = new ArrayList<>(refs.length);
        for(ServiceReference<?> ref : refs)
        {
          Bundle bundle = ref.getBundle();
          if (bundle == null)
            continue;
          String symbolicName = bundle.getSymbolicName();

          // check bundle name
          int foundIndex = findBundleName(symbolicName, bundleNames);
          if (foundIndex == -1)
            foundIndex = findBundleNameWithPatterns(symbolicName, bundleNames, patterns);

          if (foundIndex >= 0)
          {
            if (bundleVersionRanges.length != 0)
            {
              try
              {
                // check bundle version
                VersionRange versionRange = new VersionRange(bundleVersionRanges[foundIndex]);
                if (!versionRange.includes(bundle.getVersion()))
                  foundIndex = -1;
              }
              catch(IllegalArgumentException iae)
              {
                throw new InjectionException(iae);
              }
            }
          }
          else if (bundleNames.length == 0)
          {
            try
            {
              // check bundle version
              VersionRange versionRange = new VersionRange(bundleVersionRanges[0]);
              if (versionRange.includes(bundle.getVersion()))
                foundIndex = 0;
            }
            catch(IllegalArgumentException iae)
            {
              throw new InjectionException(iae);
            }
          }

          if (foundIndex >= 0)
          {
            Object service = bundleContext.getService(ref);
            services.add(service);
          }
        }
      }
    }

    private int findBundleNameWithPatterns(String symbolicName, String[] bundleNames, Pattern[] patterns)
    {
      int foundIndex = -1;
      for(int i = 0; i < bundleNames.length; i++)
      {
        // check with regex
        if (patterns[i] == null)
        {
          String regexpPattern = Pattern.quote(bundleNames[i]);
          regexpPattern = regexpPattern.replaceAll("\\*", "\\\\E.*\\\\Q");
          regexpPattern = regexpPattern.replaceAll("\\?", "\\\\E.\\\\Q");
          regexpPattern = regexpPattern.replaceAll("\\\\Q\\\\E", "");
          patterns[i] = Pattern.compile(regexpPattern);
        }

        if (patterns[i].matcher(symbolicName).matches())
        {
          foundIndex = i;
          break;
        }
      }
      return foundIndex;
    }

    private int findBundleName(String symbolicName, String[] bundleNames)
    {
      int foundIndex = -1;
      for(int i = 0; i < bundleNames.length; i++)
      {
        if (symbolicName.equals(bundleNames[i]))
        {
          foundIndex = i;
          break;
        }
      }
      return foundIndex;
    }

    void fillAllServices()
    {
      if (services != null)
        return;
      if (refs == null)
      {
        services = Collections.emptyList();
        return;
      }

      services = new ArrayList<>(refs.length);
      for(int i = 0; i < refs.length; i++)
      {
        Object service = bundleContext.getService(refs[i]);
        services.add(service);
      }
    }

    Object getFirstService()
    {
      if (services != null)
        return services.iterator().next();

      return bundleContext.getService(refs[0]);
    }

    List<Object> getServices()
    {
      fillAllServices();

      return Arrays.asList(services.toArray());
    }

    int serviceCount()
    {
      return services != null? services.size() : refs != null? refs.length : 0;
    }
  }

  private void addListener(Class<?> descriptorsClass, IRequestor requestor)
  {
    Set<IRequestor> registered = this.listeners.computeIfAbsent(descriptorsClass, dc -> ConcurrentHashMap.newKeySet());
    registered.add(requestor);
  }

  private void removeListener(Class<?> descriptorsClass, IRequestor requestor)
  {
    Set<IRequestor> registered = this.listeners.get(descriptorsClass);
    if (registered != null)
    {
      registered.remove(requestor);
      if (registered.isEmpty())
        listeners.remove(descriptorsClass);
    }
  }

  private static Class<?> getDesiredClass(Type desiredType)
  {
    if (desiredType instanceof Class<?>)
      return (Class<?>) desiredType;
    if (desiredType instanceof ParameterizedType)
    {
      Type rawType = ((ParameterizedType) desiredType).getRawType();
      if (rawType instanceof Class<?>)
        return (Class<?>) rawType;
    }
    return null;
  }

  private static Type getGenericTypeForCollection(Type desiredType)
  {
    if (desiredType instanceof Class<?>)
      return desiredType;
    if (desiredType instanceof ParameterizedType)
    {
      Type rawType = ((ParameterizedType) desiredType).getActualTypeArguments()[0];
      if (rawType instanceof Class<?>)
        return rawType;
      if (rawType instanceof WildcardType)
      {
        WildcardType wildcardType = (WildcardType) rawType;
        if (wildcardType.getUpperBounds().length == 1)
          return wildcardType.getUpperBounds()[0];
      }
    }
    return null;
  }

  private static void notifyRequestor(IRequestor requestor)
  {
    if (requestor.isValid())
    {
      requestor.resolveArguments(false);
      requestor.execute();
    }
  }
}