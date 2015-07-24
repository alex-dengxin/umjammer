

# HOWTO #

## [apktools](http://code.google.com/p/android-apktool/) ##

  * unpack

```
 $ apktool d foo.apk
```

  * pack

```
 $ smali -o target/classes.dex smali
 $ tree -L 1 target
 target/
 ├── AndroidManifest.xml
 ├── apktool.yml
 ├── assets
 ├── classes.dex
 ├── lib
 ├── proguard.pro
 ├── project.properties
 └── res
 $ apktool b target tmp.apk
 $ jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore ~/.android/debug.keystore -storepass android tmp.apk androiddebugkey
 $ $ANDROID_SDK/tools/zipalign -v 4 tmp.apk out.apk
```


## [dex2jar](http://code.google.com/p/dex2jar/) ##

```
 $ unzip foo.apk classes.dex
 $ dex2jar.sh classes.dex
```

## proguard ##
### ライブラリーの設定 (proguard) ###

Java SE のものは消しておく
```
-libraryjars /.../android-sdk/platforms/android-8/android.jar
```

### dex2jar 0.0.9.2 のバグ対応 ###

AnnotationDefault.java を適当に作って jar しておく
```
package dalvik.annotation;
import java.lang.annotation.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

@Retention(value=RUNTIME)
@Target(value=ANNOTATION_TYPE)
@interface AnnotationDefault {
}
```

ライブラリに追加
```
-libraryjars .../annotaion_default.jar
```

## known package ##

| org.acra | http://code.google.com/p/acra/ |
|:---------|:-------------------------------|