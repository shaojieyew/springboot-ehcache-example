# springboot-ehcache-example

Example configure caching in springboot using EhCache
EhCache is an open-source, Java-based cache used to boost performance. 

### Dependecies

pom.xml/gradle.build
```
<dependency>
    <groupId>javax.cache</groupId>
    <artifactId>cache-api</artifactId>
    <version>1.1.1</version>
</dependency>
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>3.8.1</version>
</dependency> 

implementation "org.ehcache:ehcache:3.8.1"
implementation "javax.cache:cache-api:1.1.1"
```

### EhCache Config

create resource/ehcache.xml with a cache named testCache
```
<config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns="http://www.ehcache.org/v3"
        xmlns:jsr107="http://www.ehcache.org/v3/jsr107"
        xsi:schemaLocation="
            http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd
            http://www.ehcache.org/v3/jsr107 http://www.ehcache.org/schema/ehcache-107-ext-3.0.xsd">

    <cache alias="testCache">
        <expiry>
            <ttl unit="seconds">3600</ttl>
        </expiry>

        <listeners>
            <listener>
                <class>app.CacheEventLogger</class>
                <event-firing-mode>ASYNCHRONOUS</event-firing-mode>
                <event-ordering-mode>UNORDERED</event-ordering-mode>
                <events-to-fire-on>CREATED</events-to-fire-on>
                <events-to-fire-on>EXPIRED</events-to-fire-on>
            </listener>
        </listeners>

        <resources>
            <heap unit="entries">2</heap>
            <offheap unit="MB">10</offheap>
        </resources>
    </cache>
</config>

```

add the following in to resource/application.yml
```
spring:
  cache:
    jcache:
      config: classpath:ehcache.xml
```

create a CacheEventListener to implement logging of cache event. This is specified in ehcache.xml listeners section
```
package app;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger
        implements CacheEventListener<Object, Object> {

    private final static Logger log =  LoggerFactory.getLogger(CacheEventLogger.class);

    public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        log.info("{},{},{}",cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}

```

### Implementation
Example used app.Main -> app.TestService -> app.ApiClientService. Where caching will be done in the app.TestService method.

add Spring's @EnableCaching annotation to a Spring bean so that Spring's annotation-driven cache management is enabled.
```

@EnableCaching
@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String []args ){
        SpringApplication application = new SpringApplication(Main.class);
        application.run(args);
    }
...
}

```

add @Cacheable to methods to be cached
- value is the name of the cache
- key is optional; without specifying any key, the whole method signature & param will be used as key
```
    @Cacheable(value = "testCache")
    public void getTest(int i) throws InterruptedException {
        apiClientService.getData(i);
    }

    @Cacheable(value = "testCache")
    public void getTestExtend(Class clazz, Map<Integer,Integer> map) throws InterruptedException {
        apiClientService.getData(clazz.hashCode()+map.size());
    }

    @Cacheable(value = "testCache", key = "{#clazz.getSimpleName(), #map}" )
    public void getTestCachBySpecificParam(Class clazz, Map<Integer,Integer> map, int i) throws InterruptedException {
        log.info("getTestCachBySpecificParam: {}, {}, {}", clazz.getSimpleName(), map.toString(), i);
        apiClientService.getData(clazz.hashCode()+map.size());
    }

```

### Example Log Result
```
2021-06-11 12:51:16.006  INFO 16032 --- [           main] app.Main                                 : Started Main in 0.911 seconds (JVM running for 1.24)
2021-06-11 12:51:16.023  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {0=0}, 0
2021-06-11 12:51:16.023  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:17.042  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {0=0}],null,null
2021-06-11 12:51:17.042  INFO 16032 --- [           main] app.Main                                 : completed cycle = 1
2021-06-11 12:51:17.147  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {1=1}, 1
2021-06-11 12:51:17.147  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:18.150  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {1=1}],null,null
2021-06-11 12:51:18.150  INFO 16032 --- [           main] app.Main                                 : completed cycle = 2
2021-06-11 12:51:18.261  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {2=2}, 2
2021-06-11 12:51:18.261  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:19.265  INFO 16032 --- [           main] app.Main                                 : completed cycle = 3
2021-06-11 12:51:19.265  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {2=2}],null,null
2021-06-11 12:51:19.373  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {0=3}, 3
2021-06-11 12:51:19.373  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:20.375  INFO 16032 --- [           main] app.Main                                 : completed cycle = 4
2021-06-11 12:51:20.375  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {0=3}],null,null
2021-06-11 12:51:20.483  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {1=4}, 4
2021-06-11 12:51:20.483  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:21.498  INFO 16032 --- [           main] app.Main                                 : completed cycle = 5
2021-06-11 12:51:21.498  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {1=4}],null,null
2021-06-11 12:51:21.605  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {2=0}, 5
2021-06-11 12:51:21.605  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:22.607  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {2=0}],null,null
2021-06-11 12:51:22.607  INFO 16032 --- [           main] app.Main                                 : completed cycle = 6
2021-06-11 12:51:22.715  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {0=1}, 6
2021-06-11 12:51:22.715  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:23.722  INFO 16032 --- [           main] app.Main                                 : completed cycle = 7
2021-06-11 12:51:23.722  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {0=1}],null,null
2021-06-11 12:51:23.832  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {1=2}, 7
2021-06-11 12:51:23.832  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:24.847  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {1=2}],null,null
2021-06-11 12:51:24.847  INFO 16032 --- [           main] app.Main                                 : completed cycle = 8
2021-06-11 12:51:24.957  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {2=3}, 8
2021-06-11 12:51:24.957  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:25.972  INFO 16032 --- [           main] app.Main                                 : completed cycle = 9
2021-06-11 12:51:25.972  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {2=3}],null,null
2021-06-11 12:51:26.082  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {0=4}, 9
2021-06-11 12:51:26.082  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:27.085  INFO 16032 --- [           main] app.Main                                 : completed cycle = 10
2021-06-11 12:51:27.085  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {0=4}],null,null
2021-06-11 12:51:27.196  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {1=0}, 10
2021-06-11 12:51:27.196  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:28.203  INFO 16032 --- [           main] app.Main                                 : completed cycle = 11
2021-06-11 12:51:28.203  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {1=0}],null,null
2021-06-11 12:51:28.314  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {2=1}, 11
2021-06-11 12:51:28.314  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:29.319  INFO 16032 --- [           main] app.Main                                 : completed cycle = 12
2021-06-11 12:51:29.319  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {2=1}],null,null
2021-06-11 12:51:29.427  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {0=2}, 12
2021-06-11 12:51:29.427  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:30.429  INFO 16032 --- [           main] app.Main                                 : completed cycle = 13
2021-06-11 12:51:30.429  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {0=2}],null,null
2021-06-11 12:51:30.539  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {1=3}, 13
2021-06-11 12:51:30.540  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:31.555  INFO 16032 --- [           main] app.Main                                 : completed cycle = 14
2021-06-11 12:51:31.555  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {1=3}],null,null
2021-06-11 12:51:31.663  INFO 16032 --- [           main] app.TestService                          : getTestCachBySpecificParam: Main, {2=4}, 14
2021-06-11 12:51:31.663  INFO 16032 --- [           main] app.ApiClientService                     : getData called 2083117812
2021-06-11 12:51:32.664  INFO 16032 --- [           main] app.Main                                 : completed cycle = 15
2021-06-11 12:51:32.664  INFO 16032 --- [e [_default_]-0] app.CacheEventLogger                     : [Main, {2=4}],null,null
2021-06-11 12:51:32.774  INFO 16032 --- [           main] app.Main                                 : completed cycle = 16
2021-06-11 12:51:32.880  INFO 16032 --- [           main] app.Main                                 : completed cycle = 17
2021-06-11 12:51:32.991  INFO 16032 --- [           main] app.Main                                 : completed cycle = 18
2021-06-11 12:51:33.098  INFO 16032 --- [           main] app.Main                                 : completed cycle = 19
2021-06-11 12:51:33.208  INFO 16032 --- [           main] app.Main                                 : completed cycle = 20
2021-06-11 12:51:33.316  INFO 16032 --- [           main] app.Main                                 : completed cycle = 21
2021-06-11 12:51:33.425  INFO 16032 --- [           main] app.Main                                 : completed cycle = 22
2021-06-11 12:51:33.534  INFO 16032 --- [           main] app.Main                                 : completed cycle = 23
2021-06-11 12:51:33.642  INFO 16032 --- [           main] app.Main                                 : completed cycle = 24
2021-06-11 12:51:33.751  INFO 16032 --- [           main] app.Main                                 : completed cycle = 25
2021-06-11 12:51:33.859  INFO 16032 --- [           main] app.Main                                 : completed cycle = 26
2021-06-11 12:51:33.968  INFO 16032 --- [           main] app.Main                                 : completed cycle = 27
2021-06-11 12:51:34.077  INFO 16032 --- [           main] app.Main                                 : completed cycle = 28
2021-06-11 12:51:34.186  INFO 16032 --- [           main] app.Main                                 : completed cycle = 29
2021-06-11 12:51:34.295  INFO 16032 --- [           main] app.Main                                 : completed cycle = 30
2021-06-11 12:51:34.404  INFO 16032 --- [           main] app.Main                                 : completed cycle = 31
2021-06-11 12:51:34.514  INFO 16032 --- [           main] app.Main                                 : completed cycle = 32
2021-06-11 12:51:34.622  INFO 16032 --- [           main] app.Main                                 : completed cycle = 33
2021-06-11 12:51:34.731  INFO 16032 --- [           main] app.Main                                 : completed cycle = 34
2021-06-11 12:51:34.840  INFO 16032 --- [           main] app.Main                                 : completed cycle = 35
2021-06-11 12:51:34.949  INFO 16032 --- [           main] app.Main                                 : completed cycle = 36
2021-06-11 12:51:35.057  INFO 16032 --- [           main] app.Main                                 : completed cycle = 37
2021-06-11 12:51:35.166  INFO 16032 --- [           main] app.Main                                 : completed cycle = 38
2021-06-11 12:51:35.275  INFO 16032 --- [           main] app.Main                                 : completed cycle = 39
2021-06-11 12:51:35.387  INFO 16032 --- [           main] app.Main                                 : completed cycle = 40
2021-06-11 12:51:35.495  INFO 16032 --- [           main] app.Main                                 : completed cycle = 41
2021-06-11 12:51:35.605  INFO 16032 --- [           main] app.Main                                 : completed cycle = 42

```