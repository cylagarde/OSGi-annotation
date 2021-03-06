package cl.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import javax.inject.Qualifier;

import org.osgi.framework.ServiceReference;

/**
 * The annotation <b>OSGiNamed</b> allows to inject the OSGi service with multiple criterion.
 * <ul>
 * <li><b><u>Example with multiple service and same interface ITodoService</u></b>
 *
 * <pre>
 * <font style="color:red">@Component</font>(name = <font style="color:green">"TODO1"</font>, property = {<font style="color:green">"service.ranking:Integer=1"</font>, <font style="color:green">"key=value"</font>})
 * <font style="color:magenta">@MyAnnotation1</font>
 * public class TodoService1 implements ITodoService, IRunnable
 * {
 *     ...
 * }
 *
 * <font style="color:red">@Component</font>(name = <font style="color:green">"TODO2"</font>, property = {<font style="color:green">"service.ranking:Integer=2"</font>, <font style="color:green">"key=value"</font>})
 * <font style="color:magenta">@MyAnnotation2</font>
 * public class TodoService2 implements ITodoService
 * {
 *     ...
 * }
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with component.name</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(name = <font style="color:green">"TODO1"</font>)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService1 instance</font>
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(name = {<font style="color:green">"TODO1"</font>, <font style="color:green">"TODO2"</font>})
 * Collection&lt;ITodoService&gt; todoServices; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService1 and TodoService2 instances</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with property values</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(property = {<font style="color:green">"service.ranking=2"</font>})
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService2 instance</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with filter using LDAP format</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(filter = <font style="color:green">"(&(service.ranking=2)(key=value))"</font>)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService2 instance</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with annotation</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(annotation = MyAnnotation1.class)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService1 instance</font>
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(notHaveAnnotation = MyAnnotation1.class)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService2 instance</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with type</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(type = Runnable.class)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService1 instance</font>
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(notHaveType = Runnable.class)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService2 instance</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with highest ranking</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(property = <font style="color:green">"key=value"</font>)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService2 instance service.ranking is highest</font>
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(property = <font style="color:green">"key=value"</font>, takeHighestRankingIfMultiple = false)
 * ITodoService todoService; <font style="color:#B22222; background-color:#FFE4B5;">// InjectionException 2 instances exist with property "key=value"</font>
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with bundle name</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(bundleName = <font style="color:green">"cl.annotation.test"</font>)
 * ITodoService todoService;
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(bundleName = <font style="color:green">"cl.annotation.*"</font>)
 * ITodoService todoService;
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with bundle version range</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(bundleVersionRange = <font style="color:green">"1.0.0"</font>)
 * ITodoService todoService;
 *
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(bundleVersionRange = <font style="color:green">"[1.0.0,2.0.0)"</font>)
 * ITodoService todoService;
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with serviceReference predicate</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(serviceReferencePredicate = MyServiceReferencePredicate.class)
 * ITodoService todoService;
 *
 * <font style="color:blue">class</font> MyServiceReferencePredicate <font style="color:blue">implements</font> Predicate&lt;ServiceReference&lt;?&gt;&gt;
 * {
 *   <font style="color:blue">public boolean</font> test(ServiceReference&lt;?&gt; ref)
 *   {
 *     <font style="color:blue">return</font> "TODO1".equals(ref.getProperty("component.name"));
 *   }
 * }
 * </pre>
 *
 * </li>
 * <li><b><u>To inject service with configuration</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(configuration = MyConfiguration.class)
 * ITodoService todoService;
 *
 * <font style="color:blue">class</font> MyConfiguration <font style="color:blue">extends</font> AbstractConfiguration
 * {
 *   <font style="color:blue">public</font> String[] name()
 *   {
 *     <font style="color:blue">return new</font> String[]{"TODO1"};
 *   }
 * }
 * </pre>
 *
 * </li>
 * <li><b><u>To retrieve all instances</u></b>
 *
 * <pre>
 * <font style="color:red">@Inject</font>
 * <font style="color:blue">@OSGiNamed</font>(...)
 * Collection&lt;ITodoService&gt; todoServices; <font style="color:#B22222; background-color:#FFE4B5;">// get TodoService1 and TodoService2 instances</font>
 * </pre>
 *
 * </li>
 * </ul>
 */
@Qualifier
@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OSGiNamed
{
  /** service name 'component.name' */
  String[] name() default {};

  /** service property */
  String[] property() default {};

  /** component filter in LDAP format */
  String filter() default "";

  /** take highest 'service.ranking' property if multiple otherwise throw InjectionException */
  boolean takeHighestRankingIfMultiple() default true;

  /** service annotations */
  Class<? extends Annotation>[] annotation() default {};

  /** service annotations */
  Class<? extends Annotation>[] notHaveAnnotation() default {};

  /** service type */
  Class<?>[] type() default {};

  /** service type */
  Class<?>[] notHaveType() default {};

  /** bundle name */
  String[] bundleName() default {};

  /** bundle version range */
  String[] bundleVersionRange() default {};

  Class<? extends Predicate<ServiceReference<?>>> serviceReferencePredicate() default DefaultServiceReferencePredicate.class;

  /** configuration (priority over other properties) */
  Class<? extends AbstractConfiguration> configuration() default AbstractConfiguration.class;
}