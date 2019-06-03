# OSGi annotation for injection v1.2.0

Allow to inject the OSGi service with multiple criterions

# Multiple service with same interface
```
@Component(name = "TODO1", property = {"service.ranking:Integer=1", "key=value")
@MyAnnotation1
public class TodoService1 implements ITodoService, IRunnable
{
	...
}

@Component(name = "TODO2", property = "service.ranking:Integer=2", "key=value")
@MyAnnotation2
public class TodoService2 implements ITodoService
{
	...
}
```
# To inject service with component.name
```
@Inject
@OSGiNamed("TODO1")
ITodoService todoService; // get TodoService1 implementation
```
# To inject service with property values
```
@Inject
@OSGiNamed(property = {"service.ranking=2"})
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with filter using LDAP format
```
@Inject
@OSGiNamed(filter = "(&(service.ranking=2)(key=value))")
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with annotation
```
@Inject
@OSGiNamed(annotations = MyAnnotation2.class)
ITodoService todoService; // get TodoService2 implementation
```
# To inject service with type
```
@Inject
@OSGiNamed(types = Runnable.class)
ITodoService todoService; // get TodoService1 implementation
```
# To inject service with highest ranking
```
@Inject
@OSGiNamed(property = "key=value")
ITodoService todoService; // get TodoService2 implementation service.ranking is highest

@Inject
@OSGiNamed(property = "key=value", takeHighestRankingIfMultiple = false)
ITodoService todoService; // InjectionException 2 implementations exist with property "key=value"
```
# To inject service with bundle name
```
@Inject
@OSGiNamed(bundleNames = "cl.annotation.test")
ITodoService todoService;
```
```
@Inject
@OSGiNamed(bundleNames = "cl.annotation.*")
ITodoService todoService;
```
# To inject service with bundle version range
```
@Inject
@OSGiNamed(bundleVersionRanges = "1.0.0")
ITodoService todoService;
```
```
@Inject
@OSGiNamed(bundleVersionRanges = "[1.0.0,2.0.0)")
ITodoService todoService;
```
# To retrieve all implementations
```
@Inject
@OSGiNamed(<criterion>)
Collection<? extends ITodoService> todoServices; // get TodoService1 and TodoService2 implementations
```



