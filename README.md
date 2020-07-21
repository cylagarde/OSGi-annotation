# OSGi annotation for injection v1.5.0

Allow to inject the OSGi service with multiple criterion

## Install
```
https://raw.githubusercontent.com/cylagarde/OSGi-annotation/master/cl.annotation.update_site
```

# Multiple service with same interface
```java
@Component(name = "TODO1", property = {"service.ranking:Integer=1", "key=value"})
@MyAnnotation1
public class TodoService1 implements ITodoService, IRunnable
{
	...
}

@Component(name = "TODO2", property = {"service.ranking:Integer=2", "key=value"})
@MyAnnotation2
public class TodoService2 implements ITodoService
{
	...
}
```
# To inject service with component.name
```java
@Inject
@OSGiNamed(name = "TODO1")
ITodoService todoService; // get TodoService1 implementation
```
```java
@Inject
@OSGiNamed(name = {"TODO1", "TODO2"})
Collection<ITodoService> todoServices; // get TodoService1 and TodoService2 implementations
```
# To inject service with property values
```java
@Inject
@OSGiNamed(property = {"service.ranking=2"})
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with filter using LDAP format
```java
@Inject
@OSGiNamed(filter = "(&(service.ranking=2)(key=value))")
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with annotation
```java
@Inject
@OSGiNamed(annotation = MyAnnotation1.class)
ITodoService todoService; // get TodoService1 implementation
```
```java
@Inject
@OSGiNamed(notHaveAnnotation = MyAnnotation1.class)
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with type
```java
@Inject
@OSGiNamed(type = Runnable.class)
ITodoService todoService; // get TodoService1 implementation
```
```java
@Inject
@OSGiNamed(notHaveType = Runnable.class)
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with highest ranking
```java
@Inject
@OSGiNamed(property = "key=value")
ITodoService todoService; // get TodoService2 implementation service.ranking is highest
```
```java
@Inject
@OSGiNamed(property = "key=value", takeHighestRankingIfMultiple = false)
ITodoService todoService; // InjectionException 2 implementations exist with property "key=value"
```
# To inject service with bundle name
```java
@Inject
@OSGiNamed(bundleName = "cl.annotation.test")
ITodoService todoService;
```
```java
@Inject
@OSGiNamed(bundleName = "cl.annotation.*")
ITodoService todoService;
```
# To inject service with configuration
```java
@Inject
@OSGiNamed(configuration = MyConfiguration.class)
ITodoService todoService;

class MyConfiguration implements OSGiNamed
{
  @Override
  public String[] name()
  {
    return new String[]{"TODO1"};
  }
  
  ...
}
```
# To retrieve all implementations
```java
@Inject
@OSGiNamed(<criterion>)
Collection<? extends ITodoService> todoServices; // get TodoService1 and TodoService2 implementations
```
