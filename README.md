# 安卓热修复技术
#####什么是安卓的热修复技术？

-  ﻿热修复是指在上线应用出现bug之后可以随时修复bug，并且不用重新打包上线新版本的一门技术。

#####热修复的好处？

- 可以尽量少上线，并且可以随时更新线上的应用的bug，尽可能的减少对用户不好的体验。

#####怎么做热修复？

- 热修复的核心功能其实就是两个，其一是将打包的apk文件下的.dex文件进分包，其二就是，将修复好的.dex文件注入到apk当中，使得apk在启动加载的时候能够加载被修复的.dex包，从而修复bug.

#####怎么进行 .dex分包？

1. 首先，.dex分包谷歌官网提供的有Multidex的兼容包，支持将.dex文拆分成多个.dex文件。在android5.0之前是不支持Multidex的，需要导入Multidex兼容包，

```
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    compile 'com.android.support:appcompat-v7:24.0.0'
    compile'com.android.support:multidex:1.+'
}

```

但是5.0之后就不需要依赖此包了因为它自身本来就有multidex兼容包，因此在项目所在的build.gradle文件中修改buildToolsVersion  的版本为21.1.0以上的版本，并且要在defaultConfig中配置multidexEnable  true

```
android {
    compileSdkVersion 24
    buildToolsVersion '21.1.2'

    defaultConfig {
        applicationId "com.example.myapplication"
        minSdkVersion 14
        targetSdkVersion 24
        versionCode 1
        versionName "1.0"
        multiDexEnabled true
 

    }

```


并且需要在android这个节点最后的位置中添加这样的配置

```
afterEvaluate {
    tasks.matching {
        it.name.startsWith('dex')
    }.each { dx ->
        if (dx.additionalParameters == null) {
            dx.additionalParameters = []
        }
        dx.additionalParameters += '--multi-dex'
        // 设置multidex.keep文件中class为第一个dex文件中包含的class，如果没有下一项设置此项无作用
        dx.additionalParameters += "--main-dex-list=$projectDir/multidex.keep".toString()
        //此项添加后第一个classes.dex文件只能包含-main-dex-list列表中class
        dx.additionalParameters += '--minimal-main-dex'
    }
}


```


其中的projectDir/multidex.keep 是一个映射文件，跟所要分包的项目的build.gradle配置文件放在同一级目录下面，这个文件是txt格式（在build.gradle同一级目录下面新建一个txt文件，命名就是，multidex.keep），并且里面写的都是在主dex文件中需要加载的类，一般里面放的类有自定义的Applicaton类，和应用的入口activity
如下面所示：

```
android/support/multidex/MultiDexExtractor.class
android/support/multidex/MultiDex.class
android/support/multidex/MultiDexExtractor$1.class
android/support/multidex/MultiDex$V4.class
android/support/v7/appcompat/R.class
android/support/multidex/MultiDex$V19.class
com/example/myapplication/MainActivity.class
android/support/multidex/MultiDexApplication.class
android/support/multidex/ZipUtil.class
android/support/multidex/MultiDex$V14.class
android/support/multidex/ZipUtil$CentralDirectory.class

```

那么其余的类将来打包成apk之后就会打包到第二个.dex文件中


随后要在清单文件中的Manifest节点做如下的操作

```


<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/AppTheme"
    android:name=".App">

```


其中在App是个自定义的Application,使这个App类要继承自MultidexApplication,

```

public class App extends MultiDexApplication {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
     new InjectUtil(this).fix();
    }


}

```


这样就基本上配置好了，然后就是让项目编译，就会生成apk文件，使用as的apk在项目的build-->output-->apk,文件目录下面，
将刚才编译的apk的后缀名改为.zip,并且解压这个压缩包，就可以看到有两个.dex文件，一般classes.dex是主包，其中里面放的就是刚才在multidex.keep文件中需要加载的类打包的.dex文件
classes2.dex就是从包，里面放的就是其他的类的.dex文件
自此，apk的.dex文件分包结束


2. 第二个主要的步骤是将修复bug的.dex文件放在公司的服务器端，然后开启后台的服务把这个.dex文件下载到手机上，目录可以是外部存储目录，但是为了保险起见，最好下载到手机内部data/data/应用包名/fix_bug/路径之下，防止被手机清理掉，
我个人为了掩饰方便暂时把下载好的.dex文件就放在外部存储卡里面，
这些操作做完之后就是最关键的，.dex文件注入，代码如下

####郑重声明

注入.dex的 操作要在Application的onCreate方法中执行

如

```
 @Override
    public void onCreate() {
        super.onCreate();
        // 获取补丁，如果存在就执行注入操作
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/musikid_reader";
        //外部存储卡的目录
        File root = new File(dexPath);
        File dex = new File(root, "classes2.dex");
        if (dex.exists()) {
            Log.e(TAG, dex.getAbsolutePath() + "" + dex.length());
            //将修复好bug的.dex注入,可以替换之前的有问题的.dex文件
            inject(dex.getAbsolutePath());
        }

    }

```



一下便是具体的实现


```


**
 * 要注入的dex的路径
 *
 * @param path
 */

private void inject(String path) {
    try {
        // 获取classes的dexElements
        Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
        Object pathList = getField(cl, "pathList", context.getClassLoader());
        Object baseElements = getField(pathList.getClass(), "dexElements", pathList);

        // 获取patch_dex的dexElements（需要先加载dex）
        String dexopt = context.getDir("dexopt", 0).getAbsolutePath();
        DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt, context.getClassLoader());
        Object obj = getField(cl, "pathList", dexClassLoader);
        Object dexElements = getField(obj.getClass(), "dexElements", obj);

        // 合并两个Elements
        Object combineElements = combineArray(dexElements, baseElements);

        // 将合并后的Element数组重新赋值给app的classLoader
        setField(pathList.getClass(), "dexElements", pathList, combineElements);

        //======== 以下是测试是否成功注入 =================
        Object object = getField(pathList.getClass(), "dexElements", pathList);
        int length = Array.getLength(object);
        Log.e("BugFixApplication", "length = " + length);

    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    } catch (NoSuchFieldException e) {
        e.printStackTrace();
    }
}


/**
 * 通过反射获取对象的属性值
 */
private Object getField(Class<?> cl, String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
    Field field = cl.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(object);
}


/**
 * 通过反射设置对象的属性值
 */
private void setField(Class<?> cl, String fieldName, Object object, Object value) throws NoSuchFieldException, IllegalAccessException {
    Field field = cl.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(object, value);
}

/**
 * 通过反射合并两个数组
 */
private Object combineArray(Object firstArr, Object secondArr) {
    int firstLength = Array.getLength(firstArr);
    int secondLength = Array.getLength(secondArr);
    int length = firstLength + secondLength;

    Class<?> componentType = firstArr.getClass().getComponentType();
    Object newArr = Array.newInstance(componentType, length);
    for (int i = 0; i < length; i++) {
        if (i < firstLength) {
            Array.set(newArr, i, Array.get(firstArr, i));
        } else {
            Array.set(newArr, i, Array.get(secondArr, i - firstLength));
        }
    }
    return newArr;

```


这些操作做完的话基本上算是完成了，当下次进入应用的时候就运行的是经过修复的apk了。
