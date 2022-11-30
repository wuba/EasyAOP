# EasyAOP
## What is EasyAOP?
- EasyAOP is a tool based on ASM to realize the general capability of AOP. General faceting capabilities provided by EasyAOP，You can use the yaml file format to configure the use of EasyAOP.
- The use of EasyAOP does not require developers to know or understand the underlying obscure AOP of expertise，nor does it require relevant AOP development experience.You can complete the relevant AOP functions or capabilities by simple configuration according to your own needs.
- EasyAOP provides Android gradle Plugin, which is convenient for Android developers to access and use.

## Why we do EasyAOP?
1. We found that in the development of AOP requirements, the general functions or capabilities of the AOP used are limited. that is, 80% of the requirements only use 20% of the general functions (capabilities) of the AOP.
2. The underlying AOP knowledge is obscure and difficult to understand. Because the related library is closer to the bytecode, it is more difficult for developers to master. In addition, the demand for AOP is less, and the input-output ratio is very poor.
3. Of the open source AOP tools, none of them is easy to use and stable and reliable.

## Getting Started

Add EasyAOP as a dependency in your main `build.gradle` in the root of your project:

todo

Then you need to "apply" the plugin and add dependencies by adding the following lines to your `app/build.gradle`.

```groovy
apply plugin: 'com.wuba.plugin.easyaop'
```

### Configure Sample

The `easy_aop_config.yaml` configuration file needs to be added to your main module directory

```yaml
version: "1.0.0"
logEnabled: true
#输出日志level; VERBOSE:1;DEBUG:2;WARN:3;ERROR:4
logLevel: 2
needAopCheck: false
needDexCheck: false
skipClazz:
  - _regex_^\S+\.R\.class$
  - _regex_^\S+\.R\$anim\.class$
  - _regex_^\S+\.R\$array\.class$
  - _regex_^\S+\.R\$attr\.class$
  - _regex_^\S+\.R\$animator\.class$
  - _regex_^\S+\.R\$bool\.class$
  - _regex_^\S+\.R\$color\.class$
  - _regex_^\S+\.R\$dimen\.class$
  - _regex_^\S+\.R\$drawable\.class$
  - _regex_^\S+\.R\$font\.class$
  - _regex_^\S+\.R\$integer\.class$
  - _regex_^\S+\.R\$id\.class$
  - _regex_^\S+\.R\$interpolator\.class$
  - _regex_^\S+\.R\$layout\.class$
  - _regex_^\S+\.R\$menu\.class$
  - _regex_^\S+\.R\$mipmap\.class$
  - _regex_^\S+\.R\$navigation\.class$
  - _regex_^\S+\.R\$plurals\.class$
  - _regex_^\S+\.R\$raw\.class$
  - _regex_^\S+\.R\$string\.class$
  - _regex_^\S+\.R\$style\.class$
  - _regex_^\S+\.R\$styleable\.class$
  - _regex_^\S+\.R\$xml\.class$

skipJars:
  # eg: jetified-kotlin-stdlib-jdk7-1.3.50.jar
  - _regex_^\S+kotlin-stdlib-jdk\d-(\d+\.)+jar$
  # eg: jetified-kotlin-stdlib-1.3.50.jar
  - _regex_^\S+kotlin-stdlib-(\d+\.)+jar$

# Method Proxy
proxyItemsCfg:
  #The class of the calling target
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    # The proxy class to use to replace the call
    proxyClass: "com.wuba.proxy.WaiterProxy"
    methodMappingList:
      # format: target function name##Objective function description: The method name of the proxy
      # function description: This can be omitted. If omitted, the default is to match the function name completely.)
      "drink##(Landroid/content/Context;)V": "drinkBeer"
      "tea##(Ljava/lang/String;)Ljava/lang/String;": "specialTea"

  "android.content.pm.PackageManager":
    enabled: true
    proxyClass: "com.wuba.proxy.TestProxy"
    methodMappingList:
      "getInstalledPackages": "getInstalledPackages"

# Method implementation empty
emptyItemsCfg:
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    methodList:
      - "waitingErrorTime##()V"
# Method implementation insert code block
insertItemsCfg:
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    insertEnterClass: "com.wuba.proxy.WaiterProxy"
    insertEnterMethodName: "insertMethodEnter"
    targetEnterMethodList:
      - "makeTea##(I)V"
    insertExitClass: "com.wuba.proxy.WaiterProxy"
    insertExitMethodName: "insertMethodExit"
    targetExitMethodList:
      - "makeTea##(I)V"
```

## Capabilities supported by EasyAOP

| Capabilities                           | explain                                                      | use                                                         | Remark            |
| -------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | --------------- |
| **AOP proxy (replacement) for function calls** | The proxy replaces the specified method call with its own method. | 1. Control the calling logic of the specified code.<br>2. Fix bugs in SDK or system-level methods by proxy.<br>3. Other. | ✅ v1.0 |
| **The body of the method is empty** | Empty the content of the specified method body. If the method has a return value, the default value is returned according to the return value type. | 1. Mask the specified method logic.<br>2. Other. | ✅ v1.0 |
| **Specify the method body, enter and exit insert the specified code** | Insert your own specified code into or out of the specified method body. | 1. It can be used in method instrumentation, such as automatic embedding, method time-sonsuming statistics, etc.<br>2. Other. | ✅ v1.0 |
| **Add try catch exception capture code snippet** | Add a try catch exception catch code segment to the specified method or code. Ensure the normal operation of subsequent code. | 1. Shield the exceptions of some methods or code segments to prevent the exception form causing the app to crash, or the subsequent code cannot continue to execute.<br>2. Other. | 🗒TODO           |
| **Object replacement**        | For the specified object A, use the B object that inherits A to perform code call replacement. | 1. The ability to replace th base class can be entered into the middle layer by using the inheritance relationship of java<br>2. Other. | 🗒TODO           |
| **Add a new mehtod to the class, or override the parent class method** | 1. Add new method to class.<br>2. Override the parent class method. | 1. Override the parent classs method. In some aspect processing, when inserting or modifying logic in a subclass method, the subclass needs to reproduce the method of the parent class.<br>2. Other. | 🗒TODO           |
| **Remove method call**     | Removes the specified method call.       | 1. Remove method call, such as log clearing, etc. | 🗒TODO           |
| **Other**                    | ...                                                          | ...                                                          | ...             |

## Example of using EasyAOP

Note:The "original code" mentioned in the aspect example in this article refers to the code before the aspect modification, which may exist in various forms in the project, such as binary class files in AAR and JAR packages, and java files in source code. ect.

### AOP proxy (replacement) for function calls

> Original code:

```java
//package: com.tea
public class Waiter {
  
		public static void drink(Context context) {
        Toast.makeText(context, "The waiter got you " + new Waiter().tea("black tea"), Toast.LENGTH_SHORT).show();
    }
  
    public String tea(String type) {
        return "a cup " + type;
    }
  	//omit other code
}
```

We need to proxy the `drink` and `tea` method calls in `com.tea.Waiter.class`, and replace them with `drinkBeer` and `specialTea` in our own `com.test.WaiterProxy.class`.

```java
//package: com.test
public final class WaiterProxy {
    public static final String TAG = "WaiterProxy";

    public static void drinkBeer(Context context) {
        Toast.makeText(context, "The waiter brought you a beer🍺", Toast.LENGTH_SHORT).show();
    }

    public static String specialTea(Waiter waiter, String type) {
        return waiter.coffee() + " and " + waiter.tea(type);
    }
	  //omit other code
}
```


After accessing the gradle plugin of EasyAOP on Android, you only need to add the following configuration to the configuration file:

```yaml
# Method Proxy
proxyItemsCfg:
  #The class of the calling target
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    # The proxy class to use to replace the call
    proxyClass: "com.wuba.proxy.WaiterProxy"
    methodMappingList:
      # format: target function name##Objective function description: The method name of the proxy
      # function description: This can be omitted. If omitted, the default is to match the function name completely.)
      "drink##(Landroid/content/Context;)V": "drinkBeer"
      "tea##(Ljava/lang/String;)Ljava/lang/String;": "specialTea"
```

After the compilation is completed, all calls to `drink` and `tea` methods in `com.tea.Waiter.class` will be replaced with calls to `drinkBeer `and specialTea` in `com.test.WaiterProxy.class`.

Note: As everyone noticed above, our method aspect proxy capability supports the proxy of object methods and static methods.

For static method proxy, the method input parameters of the proxy method and the original method are the same (same order and type), no need to make any adjustments.

For the object method proxy, it should be noted that the first parameter of the proxy method must be the object of the proxy method, and the other parameters can be compared in turn. The reason for this is: in many cases, the proxy method needs to get The object of the original method needs to be able to provide the ability to call the original method.

### The body of the method is empty

Let's take the example of "waiter making tea". If there is a wrong process in the tea-making process, the quality of the brewed tea is very poor, and the wrong process should be removed.

> Original code:

```java
//package: com.tea
public class Waiter {
    private static final String TAG = "Waiter";

  	public void waitingErrorTime() {
     		//...
        Log.e(TAG, "Improper brewing time and got tea with bad taste.");
    }
}
```

To get rid of the `waitingErrorTime` error process method in `com.tea.Water.class`, the configuration file after accessing the EasyAOP plugin is written as follows:

```yaml
emptyItemsCfg:
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    methodList:
      - "waitingErrorTime##()V"
```

In this way, the content of the `waitingErrorTime` method body in `com.tea.Water.class` will be blanked. For methods with return values, the method body will return by default after blanking, and the default value of the return value type. Of course, the return value also allows users to configure set up.

### Specify the method body, enter and exit insert the specified code

> For example, the original method `makeTea` is as follows:

```java
//package: com.tea
public class Waiter {
		public void makeTea(int time) {
      	boilWater();
      	addTea();
      	waitingTime(time);
      	waitingErrorTime();
      	makeTeaFinish();
    }
}
```

If you need to insert code before the start of this method body, for example, the insertion method is as follows:

```java
//package: com.test
public final class WaiterProxy {
	 /**
     * @param isStatic   Is it a static method
     * @param classPath  eg：“com.wuba.easyaopdemo.Waiter.class”
     * @param methodName eg：“makeTea”
     * @param methodDesc eg：“(I)V”
     * @param args       If it is a non-static method, the first parameter is the object
     */
    public static void insertMethodEnter(boolean isStatic, String classPath, String methodName, String methodDesc, Object[] args) {
        if (!isStatic && args != null) {
            Waiter waiterObj = (Waiter) args[0];
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call begin===============", waiterObj.hashCode(), methodName));
            for (int i = 1; i < args.length; i++) {
                Log.d(TAG, String.format("args[%d]==>%s", i, args[i]));
            }
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call begin===============", waiterObj.hashCode(), methodName));
        }
    }
}
```

> configuration：

```yaml
insertItemsCfg:
  "com.wuba.easyaopdemo.Waiter":
    enabled: true
    insertEnterClass: "com.wuba.proxy.WaiterProxy"
    insertEnterMethodName: "insertMethodEnter"
    targetEnterMethodList:
      - "makeTea##(I)V"
```

Of course, the configuration example of the insertion part at the end of the method body is also mentioned above, and the usage is the same.

### Other competencies

Other capabilities will be supported in subsequent versions and will be updated in the corresponding documentation. No introduction is made here.

## Comparison of common faceting tools and EasyAOP

Aspect tool frameworks, ASM and AspectJ are commonly used now. These frameworks are difficult to learn and use, and they are comprehensive and low-level frameworks. The bottom layer of EasyAOP invokes and encapsulates the capabilities of ASM. It provides general capabilities oriented to business requirements and can be invoked through simple configuration. The following is a comparison of EasyAOP and common faceting tools in 7 dimensions:

![EasyAOP Comparison](./doc/res/common_tools_comparison.png)

From the comparison, it is obvious that EasyAOP inherits the good aspects of ASM, optimizes and improves the complexity of access matters, allows users to access easily, and can quickly develop aspect function requirements without knowing aspect knowledge.

It is also mentioned in the above "Why EasyAOP" that 80% of the aspect class requirements only use 20% of the aspect capabilities, so the aspect direction of our EasyAOP framework is to provide business-oriented general aspect capabilities, rather than comprehensive, bottom-level capabilities. slicing ability.

## EasyAOP Support

Any problem?

1. Learn more from [easyaop/sample](./sample).
2. Read the [source code](https://github.com/wuba/EasyAOP/tree/master).
3. Read the [wiki](https://github.com/wuba/EasyAOP/wiki) or [FAQ](./doc/常见问题.md) for help.
4. Contact us for help.

## Contributing

For more information about contributing issues or pull requests, see our [EasyAOP Contributing Guide](./CONTRIBUTING.md).

## License

Tinker is under the BSD license. See the [LICENSE](./LICENSE.txt) file for details.
