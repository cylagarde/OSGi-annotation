package cl.annotation.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;
import org.eclipse.e4.core.internal.di.ObjectDescriptor;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

import cl.annotation.OSGiNamed;
import cl.annotation.internal.OSGiNamedObjectSupplier;

/**
 * The class <b>OSGiNamed_TestCase</b> allows to.<br>
 */
@SuppressWarnings({"unchecked", "restriction", "unused", "static-method"})
public class OSGiNamed_TestCase
{
  static OSGiNamedObjectSupplier osgiNamedObjectSupplier = new OSGiNamedObjectSupplier();

  @Test
  public void testFake()
  {
    Type desiredType = IFake.class;
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertSame(IInjector.NOT_A_VALUE, service);
  }

  @Test
  public void testOptionalFake()
  {
    Type desiredType = IFake.class;
    Annotation[] annotations = {new OSGiNamedBuilder().build(), new OptionalImpl()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNull(service);
  }

  @Test
  public void testOneService()
  {
    Type desiredType = IOneService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertTrue(OneService.class.isInstance(service));
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_takeHighestRankingIfMultiple()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withTakeHighestRankingIfMultiple(false).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
  }

  @Test
  public void testMultipleService()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_name()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withName("Run1").build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withName("Run2").build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_property()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withProperty(new String[]{"p=Run1"}).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withProperty(new String[]{"p=Run2"}).build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_filter()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withFilter("(p=Run1)").build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withFilter("(p=Run2)").build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_annotations()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withAnnotations(new Class[]{ARun1.class}).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withAnnotations(new Class[]{ARun2.class}).build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_notHaveAnnotations()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withNotHaveAnnotations(new Class[]{ARun1.class}).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withNotHaveAnnotations(new Class[]{ARun2.class}).build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));
  }

  @Test
  public void testMultipleService_types()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withTypes(new Class[]{IRun1.class}).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withTypes(new Class[]{IRun2.class}).build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));
  }

  @Test
  public void testMultipleService_notHaveTypes()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withNotHaveTypes(new Class[]{IRun1.class}).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run2.class.isInstance(service));

    annotations[0] = new OSGiNamedBuilder().withNotHaveTypes(new Class[]{IRun2.class}).build();
    descriptor = new ObjectDescriptor(desiredType, annotations);
    service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertNotNull(service);
    assertTrue(Run1.class.isInstance(service));
  }

  @Test
  public void testMultipleService_tracking()
  {
    Type desiredType = IOneService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = new IRequestor() {
      @Override
      public boolean uninject(Object object, PrimaryObjectSupplier objectSupplier) throws InjectionException
      {
        return false;
      }

      @Override
      public void resolveArguments(boolean initial) throws InjectionException
      {
      }

      @Override
      public boolean isValid()
      {
        return false;
      }

      @Override
      public Class<?> getRequestingObjectClass()
      {
        return null;
      }

      @Override
      public Object getRequestingObject()
      {
        return null;
      }

      @Override
      public Object execute() throws InjectionException
      {
        return null;
      }

      @Override
      public void disposed(PrimaryObjectSupplier objectSupplier) throws InjectionException
      {
      }
    };

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, true, false);
    assertTrue(OneService.class.isInstance(service));

    //
    try
    {
      Field listenersField = OSGiNamedObjectSupplier.class.getDeclaredField("listeners");
      listenersField.setAccessible(true);
      Map<?, ?> listeners = (Map<?, ?>) listenersField.get(osgiNamedObjectSupplier);
      assertEquals(1, listeners.size());

      Field serviceListenersField = OSGiNamedObjectSupplier.class.getDeclaredField("serviceListeners");
      serviceListenersField.setAccessible(true);
      Map<?, ?> serviceListeners = (Map<?, ?>) serviceListenersField.get(osgiNamedObjectSupplier);
      assertEquals(1, serviceListeners.size());
    }
    catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      fail(e.getMessage());
    }

    // untrack
    osgiNamedObjectSupplier.get(descriptor, requestor, false, false);

    //
    try
    {
      Field listenersField = OSGiNamedObjectSupplier.class.getDeclaredField("listeners");
      listenersField.setAccessible(true);
      Map<?, ?> listeners = (Map<?, ?>) listenersField.get(osgiNamedObjectSupplier);
      assertEquals(0, listeners.size());

      Field serviceListenersField = OSGiNamedObjectSupplier.class.getDeclaredField("serviceListeners");
      serviceListenersField.setAccessible(true);
      Map<?, ?> serviceListeners = (Map<?, ?>) serviceListenersField.get(osgiNamedObjectSupplier);
      assertEquals(0, serviceListeners.size());
    }
    catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
    {
      fail(e.getMessage());
    }
  }

  @Test
  public void testMultipleService_trackToReinject()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    InjectService injectService = ContextInjectionFactory.make(InjectService.class, eclipseCtx);
    assertTrue(Run2.class.isInstance(injectService.multipleService));

    Hashtable<String, Object> properties = new Hashtable<>();
    properties.put("service.ranking", 3);
    properties.put("key", "value");
    ServiceRegistration<IMultipleService> registerService = bundleContext.registerService(IMultipleService.class, new Run3(), properties);

    try
    {
      assertTrue(Run3.class.isInstance(injectService.multipleService));
    }
    finally
    {
      registerService.unregister();
    }

    assertTrue(Run2.class.isInstance(injectService.multipleService));

    ContextInjectionFactory.uninject(injectService, eclipseCtx);
  }

  @Test
  public void testMultipleService_collection()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    CollectionService collectionService = ContextInjectionFactory.make(CollectionService.class, eclipseCtx);
    assertEquals(1, collectionService.call);
    assertEquals(2, collectionService.multipleServices.size());
    assertEquals(2, collectionService.multipleServices2.size());
    assertEquals(0, collectionService.multipleServices3.size());
    assertEquals(2, collectionService.multipleServices4.size());
    assertEquals(collectionService.multipleServices, collectionService.multipleServices2);

    List<?> list = Arrays.asList(collectionService.multipleServices.toArray());
    assertTrue(Run2.class.isInstance(list.get(0)));
    assertTrue(Run1.class.isInstance(list.get(1)));

    Hashtable<String, Object> properties = new Hashtable<>();
    properties.put("component.name", "Run3");
    properties.put("service.ranking", 3);
    properties.put("key", "value");
    Run3 run3 = new Run3();
    ServiceRegistration<IMultipleService> registerService = bundleContext.registerService(IMultipleService.class, run3, properties);

    try
    {
      assertEquals(2, collectionService.call);
      assertEquals(3, collectionService.multipleServices.size());
      assertEquals(3, collectionService.multipleServices2.size());
      assertEquals(1, collectionService.multipleServices3.size());
      assertEquals(3, collectionService.multipleServices4.size());
      assertTrue(collectionService.multipleServices.contains(run3));
      assertTrue(collectionService.multipleServices2.contains(run3));
      assertEquals(collectionService.multipleServices, collectionService.multipleServices2);

      list = Arrays.asList(collectionService.multipleServices.toArray());
      assertTrue(Run3.class.isInstance(list.get(0)));
      assertTrue(Run2.class.isInstance(list.get(1)));
      assertTrue(Run1.class.isInstance(list.get(2)));
    }
    finally
    {
      registerService.unregister();
    }

    assertEquals(3, collectionService.call);
    assertEquals(2, collectionService.multipleServices.size());
    assertEquals(2, collectionService.multipleServices2.size());
    assertEquals(0, collectionService.multipleServices3.size());
    assertEquals(2, collectionService.multipleServices4.size());
    assertFalse(collectionService.multipleServices.contains(run3));
    assertFalse(collectionService.multipleServices2.contains(run3));

    ContextInjectionFactory.uninject(collectionService, eclipseCtx);
  }

  @Test
  public void testTypeGeneric() throws Exception
  {
    Type desiredType = TypeGeneric.class.getDeclaredField("typeGeneric").getGenericType();
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertSame(IInjector.NOT_A_VALUE, service);
  }

  @Test
  public void testCollectionTypeGeneric() throws Exception
  {
    Type desiredType = TypeGeneric.class.getDeclaredField("typeGenerics").getGenericType();
    Annotation[] annotations = {new OSGiNamedBuilder().build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertSame(IInjector.NOT_A_VALUE, service);
  }

  @Test
  public void testMultipleService_bundleNameAndVersion()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    BundleService bundleNameService = ContextInjectionFactory.make(BundleService.class, eclipseCtx);
    assertNull(bundleNameService.badVersionName);
    assertTrue(Run2.class.isInstance(bundleNameService.validVersionName));
    assertTrue(Run2.class.isInstance(bundleNameService.validVersionName2));
    assertNull(bundleNameService.badVersionRange);
    assertTrue(Run2.class.isInstance(bundleNameService.validVersionRange));
    assertTrue(Run2.class.isInstance(bundleNameService.validVersionRange2));
    assertTrue(Run2.class.isInstance(bundleNameService.validVersionNameRange));
    assertNull(bundleNameService.badVersionRange2);

    ContextInjectionFactory.uninject(bundleNameService, eclipseCtx);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bundleNameAndVersion_badInput()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    ContextInjectionFactory.make(BundleServiceBadInput.class, eclipseCtx);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bundleNameAndVersion_badInput2()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    ContextInjectionFactory.make(BundleServiceBadInput2.class, eclipseCtx);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bundleNameAndVersion_badInput3()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    ContextInjectionFactory.make(BundleServiceBadInput3.class, eclipseCtx);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bundleNameAndVersion_badInput4()
  {
    Bundle bundle = FrameworkUtil.getBundle(OSGiNamedObjectSupplier.class);
    BundleContext bundleContext = bundle.getBundleContext();
    IEclipseContext eclipseCtx = EclipseContextFactory.getServiceContext(bundleContext);

    ContextInjectionFactory.make(BundleServiceBadInput4.class, eclipseCtx);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bad_criterion()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withName("visa").withProperty(new String[]{"p=Run1"}).withFilter("p:=Run1").build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
  }

  @Test(expected = InjectionException.class)
  public void testMultipleService_bad_criterion2()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations =
      {new OSGiNamedBuilder().withProperty(new String[]{"key=value"}).withFilter("(|(p=Run1)(p=Run2))").withAnnotations(new Class[]{ACommon.class}).withTypes(new Class[]{IMultipleService.class}).withTakeHighestRankingIfMultiple(false).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
  }

  @Test
  public void testMultipleService_bad_criterion3()
  {
    Type desiredType = IMultipleService.class;
    Annotation[] annotations = {new OSGiNamedBuilder().withName("fake").withProperty(new String[]{"key=value"}).withFilter("(|(p=Run1)(p=Run2))").withAnnotations(new Class[]{ACommon.class}).withTypes(new Class[]{IMultipleService.class})
      .withTakeHighestRankingIfMultiple(false).build()};
    IObjectDescriptor descriptor = new ObjectDescriptor(desiredType, annotations);
    IRequestor requestor = null;

    Object service = osgiNamedObjectSupplier.get(descriptor, requestor, false, false);
    assertSame(IInjector.NOT_A_VALUE, service);
  }

  ///////////////////////////////////////////

  static final class OSGiNamedBuilder
  {
    String[] names = {};
    String[] property = {};
    String filter = "";
    Class<? extends Annotation>[] annotations = new Class[0];
    Class<? extends Annotation>[] notHaveAnnotations = new Class[0];
    Class<?>[] types = {};
    Class<?>[] notHaveTypes = {};
    boolean takeHighestRankingIfMultiple = true;
    String[] bundleNames = {};
    String[] bundleVersions = {};

    OSGiNamed build()
    {
      return new OSGiNamedImpl(names, property, filter, annotations, notHaveAnnotations, types, notHaveTypes, takeHighestRankingIfMultiple, bundleNames, bundleVersions);
    }

    OSGiNamedBuilder withFilter(String filter)
    {
      this.filter = filter;
      return this;
    }

    OSGiNamedBuilder withNotHaveTypes(Class<?>[] notHaveTypes)
    {
      this.notHaveTypes = notHaveTypes;
      return this;
    }

    OSGiNamedBuilder withNotHaveAnnotations(Class<? extends Annotation>[] notHaveAnnotations)
    {
      this.notHaveAnnotations = notHaveAnnotations;
      return this;
    }

    OSGiNamedBuilder withTypes(Class<?>[] types)
    {
      this.types = types;
      return this;
    }

    OSGiNamedBuilder withAnnotations(Class<? extends Annotation>[] annotations)
    {
      this.annotations = annotations;
      return this;
    }

    OSGiNamedBuilder withProperty(String[] property)
    {
      this.property = property;
      return this;
    }

    OSGiNamedBuilder withTakeHighestRankingIfMultiple(boolean takeHighestRankingIfMultiple)
    {
      this.takeHighestRankingIfMultiple = takeHighestRankingIfMultiple;
      return this;
    }

    OSGiNamedBuilder withName(String name)
    {
      return withNames(new String[]{name});
    }

    OSGiNamedBuilder withNames(String[] names)
    {
      this.names = names;
      return this;
    }
  }

  static final class OSGiNamedImpl implements OSGiNamed
  {
    final String[] names;
    final String[] property;
    final String filter;
    final Class<? extends Annotation>[] annotations;
    final Class<? extends Annotation>[] notHaveAnnotations;
    final Class<?>[] types;
    final Class<?>[] notHaveTypes;
    final boolean takeHighestRankingIfMultiple;
    final String[] bundleNames;
    final String[] bundleVersions;

    OSGiNamedImpl(String[] names, String[] property, String filter,
      Class<? extends Annotation>[] annotations, Class<? extends Annotation>[] notHaveAnnotations,
      Class<?>[] types, Class<?>[] notHaveTypes,
      boolean takeHighestRankingIfMultiple, String[] bundleNames, String[] bundleVersions)
    {
      this.names = names;
      this.property = property;
      this.filter = filter;
      this.annotations = annotations;
      this.notHaveAnnotations = notHaveAnnotations;
      this.types = types;
      this.notHaveTypes = notHaveTypes;
      this.takeHighestRankingIfMultiple = takeHighestRankingIfMultiple;
      this.bundleNames = bundleNames;
      this.bundleVersions = bundleVersions;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
      return OSGiNamed.class;
    }

    @Override
    public String[] name()
    {
      return names;
    }

    @Override
    public String[] property()
    {
      return property;
    }

    @Override
    public String filter()
    {
      return filter;
    }

    @Override
    public boolean takeHighestRankingIfMultiple()
    {
      return takeHighestRankingIfMultiple;
    }

    @Override
    public Class<? extends Annotation>[] annotation()
    {
      return annotations;
    }

    @Override
    public Class<?>[] type()
    {
      return types;
    }

    @Override
    public String[] bundleName()
    {
      return bundleNames;
    }

    @Override
    public String[] bundleVersionRange()
    {
      return bundleVersions;
    }

    @Override
    public Class<? extends Annotation>[] notHaveAnnotation()
    {
      return notHaveAnnotations;
    }

    @Override
    public Class<?>[] notHaveType()
    {
      return notHaveTypes;
    }
  }

  class OptionalImpl implements org.eclipse.e4.core.di.annotations.Optional
  {
    @Override
    public Class<? extends Annotation> annotationType()
    {
      return org.eclipse.e4.core.di.annotations.Optional.class;
    }
  }

  class TypeGeneric<T>
  {
    T typeGeneric;

    Collection<T> typeGenerics;
  }

  interface IFake
  {
  }

  interface IOneService
  {
  }

  @Component
  public static class OneService implements IOneService
  {
  }

  public static class InjectService
  {
    @Inject
    @OSGiNamed(name = "Run1")
    IMultipleService multipleService1;

    @Inject
    @OSGiNamed(property = {"key=value"}, takeHighestRankingIfMultiple = true)
    IMultipleService multipleService;

    @Inject
    @OSGiNamed(name = "Run2")
    IMultipleService multipleService2;
  }

  public static class CollectionService
  {
    int call = 0;

    @Inject
    @OSGiNamed
    Collection<? extends IMultipleService> multipleServices;

    @Inject
    @OSGiNamed
    Collection<IMultipleService> multipleServices2;

    @Inject
    @OSGiNamed(property = "service.ranking=3")
    Collection<IMultipleService> multipleServices3;

    @Inject
    @OSGiNamed(name = {"Run1", "Run2", "Run3"})
    Collection<IMultipleService> multipleServices4;

    @Inject
    void setServices(@OSGiNamed Collection<IMultipleService> multipleServices2)
    {
      call++;
    }
  }

  public static class BundleService
  {
    @Inject
    @Optional
    @OSGiNamed(bundleName = "!fake!")
    IMultipleService badVersionName;

    @Inject
    @OSGiNamed(bundleName = "cl.annotation.test")
    IMultipleService validVersionName;

    @Inject
    @OSGiNamed(bundleName = "cl.annotation.*")
    IMultipleService validVersionName2;

    @Inject
    @OSGiNamed(bundleVersionRange = "1.0.0")
    IMultipleService validVersionRange;

    @Inject
    @OSGiNamed(bundleVersionRange = "[1.0.0,100)")
    IMultipleService validVersionRange2;

    @Inject
    @Optional
    @OSGiNamed(bundleVersionRange = "[0.0.1, 1.0.0)")
    IMultipleService badVersionRange;

    @Inject
    @OSGiNamed(bundleName = "cl.annotation.test", bundleVersionRange = "1.0.0")
    IMultipleService validVersionNameRange;

    @Inject
    @OSGiNamed(bundleName = {"cl.annotation.test", "cl.annotation"}, bundleVersionRange = {"1.0.0", "1.1.0"})
    IMultipleService validVersionNameRange2;

    @Inject
    @Optional
    @OSGiNamed(bundleName = "cl.annotation.test", bundleVersionRange = "100.0.0")
    IMultipleService badVersionRange2;

  }

  public static class BundleServiceBadInput
  {
    @Inject
    @OSGiNamed(bundleName = "cl.annotation.test", bundleVersionRange = {"1.0.0", "1.0.0"})
    IMultipleService badInput;
  }

  public static class BundleServiceBadInput2
  {
    @Inject
    @OSGiNamed(bundleName = {"cl.annotation.test", "cl.annotation"}, bundleVersionRange = {"1.0.0"})
    IMultipleService badInput;
  }

  public static class BundleServiceBadInput3
  {
    @Inject
    @OSGiNamed(bundleName = {"cl.annotation.test"}, bundleVersionRange = {"fake"})
    IMultipleService badInput;
  }

  public static class BundleServiceBadInput4
  {
    @Inject
    @OSGiNamed(bundleVersionRange = {"fake"})
    IMultipleService badInput;
  }

  interface IMultipleService
  {
  }

  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface ARun1
  {
  }

  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface ARun2
  {
  }

  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @interface ACommon
  {
  }

  interface IRun1
  {
  }

  interface IRun2
  {
  }

  @Component(name = "Run1", property = {"p=Run1", "key=value", "service.ranking:Integer=1"}, service = {Run1.class, IMultipleService.class})
  @ARun1
  @ACommon
  public static class Run1 implements IMultipleService, IRun1
  {
  }

  @Component(name = "Run2", property = {"p=Run2", "key=value", "service.ranking:Integer=2"}, service = {Run2.class, IMultipleService.class})
  @ARun2
  @ACommon
  public static class Run2 implements IMultipleService, IRun2
  {
  }

  public static class Run3 implements IMultipleService
  {
  }
}
