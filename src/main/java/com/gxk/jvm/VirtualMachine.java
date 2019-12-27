package com.gxk.jvm;

import com.gxk.jvm.classloader.ClassLoader;
import com.gxk.jvm.classpath.Classpath;
import com.gxk.jvm.classpath.Entry;
import com.gxk.jvm.interpret.Interpreter;
import com.gxk.jvm.rtda.Slot;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KArray;
import com.gxk.jvm.rtda.heap.KClass;
import com.gxk.jvm.rtda.heap.KField;
import com.gxk.jvm.rtda.heap.KMethod;
import com.gxk.jvm.rtda.heap.KObject;
import com.gxk.jvm.util.EnvHolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class VirtualMachine {

  public void run(Args cmd) {
    String home = System.getenv("JAVA_HOME");
    if (home == null) {
      throw new IllegalStateException("must set env JAVA_HOME");
    }

    EnvHolder.init();
    if (cmd.verbose) {
      EnvHolder.verbose = true;
    }
    if (cmd.verboseTrace) {
      EnvHolder.verboseTrace = true;
    }
    if (cmd.verboseClass) {
      EnvHolder.verboseClass = true;
    }
    if (cmd.verboseDebug) {
      EnvHolder.verboseDebug = true;
    }

    Path jarPath = Paths.get(home, "jre", "lib");
    String classpath = cmd.classpath + EnvHolder.PATH_SEPARATOR + jarPath.toFile().getAbsolutePath() + EnvHolder.FILE_SEPARATOR + "*";
    Entry entry = Classpath.parse(classpath);

    ClassLoader classLoader = new ClassLoader("boot", entry);
    initVm(classLoader);

    String mainClass = cmd.clazz.replace(".", EnvHolder.FILE_SEPARATOR);
    classLoader.loadClass(mainClass);

    KClass clazz = Heap.findClass(mainClass);
    KMethod method = clazz.getMainMethod();
    if (method == null) {
      throw new IllegalStateException("not found main method");
    }

    new Interpreter().interpret(method, cmd.args);
  }

  public static void initVm(ClassLoader classLoader) {
    loadLibrary();
    loadFoundationClass(classLoader);
  }

  public static void loadLibrary() {
    // hack for out.println
    Heap.registerMethod("java/io/PrintStream_println_(Ljava/lang/String;)V", frame -> {
      Object val = frame.popRef();
      frame.popRef();
      if (val instanceof String) {
        System.out.println(val);
        return;
      }

      KField value = ((KObject) val).getField("value", "[C");
      KArray arr = (KArray) value.val[0].ref;
      char[] chars = new char[arr.items.length];
      for (int i = 0; i < arr.items.length; i++) {
        chars[i] = ((Character) arr.items[i]);
      }
      System.out.println(new String(chars));
    });
    Heap.registerMethod("java/io/PrintStream_println_()V", frame -> {
      frame.popRef();
      System.out.println();
    });
    Heap.registerMethod("java/io/PrintStream_println_(Z)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.println(val == 1);
    });
    Heap.registerMethod("java/io/PrintStream_println_(C)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.println(((char) val));
    });
    Heap.registerMethod("java/io/PrintStream_println_(B)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.println(((byte) val));
    });
    Heap.registerMethod("java/io/PrintStream_println_(S)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.println(((short) val));
    });
    Heap.registerMethod("java/io/PrintStream_println_(I)V", frame -> {
      Object val = frame.popInt();
      frame.popRef();
      System.out.println(val);
    });
    Heap.registerMethod("java/io/PrintStream_println_(F)V", frame -> {
      float val = frame.popFloat();
      frame.popRef();
      System.out.println((val));
    });
    Heap.registerMethod("java/io/PrintStream_println_(J)V", frame -> {
      long val = frame.popLong();
      frame.popRef();
      System.out.println(val);
    });
    Heap.registerMethod("java/io/PrintStream_println_(D)V", frame -> {
      double val = frame.popDouble();
      frame.popRef();
      System.out.println(val);
    });
    Heap.registerMethod("java/io/PrintStream_println_(Ljava/lang/Object;)V", frame -> {
      Object val = frame.popRef();
      if (val instanceof KObject) {
        KObject valObj = (KObject) val;
        if (valObj.clazz.name.equals("java/lang/Integer")) {
          val = valObj.getField("value", "I").val[0].num;
        }
      }
      frame.popRef();
      System.out.println(val);
    });

    Heap.registerMethod("java/io/PrintStream_print_(Ljava/lang/String;)V", frame -> {
      Object val = frame.popRef();
      frame.popRef();
      if (val instanceof String) {
        System.out.print(val);
        return;
      }

      KField value = ((KObject) val).getField("value", "[C");
      KArray arr = (KArray) value.val[0].ref;
      char[] chars = new char[arr.items.length];
      for (int i = 0; i < arr.items.length; i++) {
        chars[i] = ((Character) arr.items[i]);
      }
      System.out.print(new String(chars));
    });
    Heap.registerMethod("java/io/PrintStream_print_(Z)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.print(val == 1);
    });
    Heap.registerMethod("java/io/PrintStream_print_(C)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.print(((char) val));
    });
    Heap.registerMethod("java/io/PrintStream_print_(B)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.print(((byte) val));
    });
    Heap.registerMethod("java/io/PrintStream_print_(S)V", frame -> {
      int val = frame.popInt();
      frame.popRef();
      System.out.print(((short) val));
    });
    Heap.registerMethod("java/io/PrintStream_print_(I)V", frame -> {
      Object val = frame.popInt();
      frame.popRef();
      System.out.print(val);
    });
    Heap.registerMethod("java/io/PrintStream_print_(F)V", frame -> {
      float val = frame.popFloat();
      frame.popRef();
      System.out.print((val));
    });
    Heap.registerMethod("java/io/PrintStream_print_(J)V", frame -> {
      long val = frame.popLong();
      frame.popRef();
      System.out.print(val);
    });
    Heap.registerMethod("java/io/PrintStream_print_(D)V", frame -> {
      double val = frame.popDouble();
      frame.popRef();
      System.out.print(val);
    });
    Heap.registerMethod("java/io/PrintStream_print_(Ljava/lang/Object;)V", frame -> {
      Object val = frame.popRef();
      frame.popRef();
      System.out.print(val);
    });
    // object
    Heap.registerMethod("java/lang/Object_hashCode_()I", (frame) -> {
      int val = frame.popRef().hashCode();
      frame.pushInt(val);
    });
    Heap.registerMethod("java/lang/Object_registerNatives_()V", (frame) -> {
    });
    Heap.registerMethod("java/lang/Object_clone_()Ljava/lang/Object;", (frame) -> {
      KObject obj = (KObject) frame.popRef();
      Object newObj = null;
      try {
        newObj = obj.clone();
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
      frame.pushRef(newObj);
    });
    Heap.registerMethod("java/lang/Object_getClass_()Ljava/lang/Class;", (frame) -> {
      KObject val = (KObject) frame.popRef();
      frame.pushRef(val.clazz.getRuntimeClass());
    });

    Heap.registerMethod("java/lang/Object_wait_(J)V", (frame) -> {
    });
    Heap.registerMethod("java/lang/Object_notify_()V", (frame) -> {
    });
    Heap.registerMethod("java/lang/Object_notifyAll_()V", (frame) -> {
    });

    // class
    Heap.registerMethod("java/lang/Class_getName0_()Ljava/lang/String;", frame -> {
      KObject obj = (KObject) frame.popRef();
      String name = obj.getMetaClass().name;
      KClass strClazz = Heap.findClass("java/lang/String");
      KObject nameObj = strClazz.newObject();
      char[] chars = name.replaceAll("/", ".").toCharArray();
      Character[] characters = new Character[chars.length];
      for (int i = 0; i < chars.length; i++) {
        characters[i] = chars[i];
      }
      KArray kArray = new KArray(Heap.findClass("java/lang/Character"), characters);
      nameObj.setField("value", "[C", new Slot[] {new Slot(kArray)});
      frame.pushRef(nameObj);
    });
    Heap.registerMethod("java/lang/Class_forName0_(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_isInstance_(Ljava/lang/Object;)Z", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_isAssignableFrom_(Ljava/lang/Class;)Z", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_isInterface_()Z", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_isArray_()Z", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_isPrimitive_()Z", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getSuperclass_()Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getInterfaces0_()[Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getComponentType_()Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getModifiers_()I", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getSigners_()[Ljava/lang/Object;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_setSigners_([Ljava/lang/Object;)V", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getEnclosingMethod0_()[Ljava/lang/Object;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getDeclaringClass0_()Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getProtectionDomain0_()Ljava/security/ProtectionDomain;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getGenericSignature0_()Ljava/lang/String;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getRawAnnotations_()[B", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getRawTypeAnnotations_()[B", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getConstantPool_()Lsun/reflect/ConstantPool;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getDeclaredFields0_(Z)[Ljava/lang/reflect/Field;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getDeclaredMethods0_(Z)[Ljava/lang/reflect/Method;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getDeclaredConstructors0_(Z)[Ljava/lang/reflect/Constructor;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_getDeclaredClasses0_()[Ljava/lang/Class;", frame -> {
      throw new UnsupportedOperationException();
    });
    Heap.registerMethod("java/lang/Class_desiredAssertionStatus0_(Ljava/lang/Class;)Z", frame -> {
      throw new UnsupportedOperationException();
    });

    Heap.registerMethod("java/lang/Class_registerNatives_()V", (frame) -> {
    });
    Heap.registerMethod("java/lang/Class_getPrimitiveClass_(Ljava/lang/String;)Ljava/lang/Class;", (frame) -> {
      Character[] values = (Character[]) ((KArray) ((KObject) frame.popRef()).getField("value", "[C").val[0].ref).items;
      char[] v2 = new char[values.length];
      for (int i = 0; i < values.length; i++) {
        v2[i] = values[i];
      }
      String val = new String(v2);
      KClass cls = Heap.findClass(val);
      frame.pushRef(cls.getRuntimeClass());
    });
    // hack for class
    Heap.registerMethod("java/lang/Class_desiredAssertionStatus_()Z", frame -> {
      Object xx = frame.popRef();
      frame.pushInt(1);
    });

    // math
    // hack for math
    Heap.registerMethod("java/lang/Math_min_(II)I", frame -> {
      Integer v2 = frame.popInt();
      Integer v1 = frame.popInt();
      if (v1 <= v2) {
        frame.pushInt(v1);
      } else {
        frame.pushInt(v2);
      }
    });
    Heap.registerMethod("java/lang/Math_max_(II)I", frame -> {
      Integer v2 = frame.popInt();
      Integer v1 = frame.popInt();
      if (v1 >= v2) {
        frame.pushInt(v1);
      } else {
        frame.pushInt(v2);
      }
    });

    // system
    Heap.registerMethod("java/lang/System_registerNatives_()V", (frame) -> {
    });
    Heap.registerMethod("java/lang/System_setIn0_(Ljava/io/InputStream;)V", (frame) -> {
    });
    Heap.registerMethod("java/lang/System_setOut0_(Ljava/io/PrintStream;)V", (frame) -> {
    });
    Heap.registerMethod("java/lang/System_setErr0_(Ljava/io/PrintStream;)V", (frame) -> {
    });
    Heap.registerMethod("java/lang/System_currentTimeMillis_()J", (frame) -> frame.pushLong(System.currentTimeMillis()));
    Heap.registerMethod("java/lang/System_nanoTime_()J", (frame) -> frame.pushLong(System.nanoTime()));
    Heap.registerMethod("java/lang/System_arraycopy_(Ljava/lang/Object;ILjava/lang/Object;II)V", (frame) -> {
      Integer len = frame.popInt();
      Integer dsp = frame.popInt();
      KArray dest = (KArray) frame.popRef();
      Integer ssp = frame.popInt();
      KArray source = (KArray) frame.popRef();

      for (int i = 0; i < len; i++) {
        dest.items[dsp++] = source.items[ssp++];
      }
    });
    Heap.registerMethod("java/lang/System_identityHashCode_(Ljava/lang/Object;)I", (frame) -> frame.pushInt(frame.popRef().hashCode()));
    Heap.registerMethod("java/lang/System_initProperties_(Ljava/util/Properties;)Ljava/util/Properties;", (frame) -> {
    });
    Heap.registerMethod("java/lang/System_mapLibraryName_(Ljava/lang/String;)Ljava/lang/String;", (frame) -> {
    });

    // Unsafe
    Heap.registerMethod("sun/misc/Unsafe_registerNatives_()V", frame -> {
    });
    Heap.registerMethod("sun/misc/Unsafe_getUnsafe_()Lsun/misc/Unsafe;", frame -> {
      frame.pushRef(null);
    });

    // string
    Heap.registerMethod("java/lang/String_intern_()Ljava/lang/String;", frame -> {
      System.out.println();
    });

    // Float
    Heap.registerMethod("java/lang/Float_intBitsToFloat_(I)F", frame -> {
      Integer tmp = frame.popInt();
      float v = Float.intBitsToFloat(tmp);
      frame.pushFloat(v);
    });
    Heap.registerMethod("java/lang/Float_floatToRawIntBits_(F)I", frame -> {
      float tmp = frame.popFloat();
      int v = Float.floatToRawIntBits(tmp);
      frame.pushInt(v);
    });

    // Double
    Heap.registerMethod("java/lang/Double_doubleToRawLongBits_(D)J", frame -> {
      Double tmp = frame.popDouble();
      long v = Double.doubleToRawLongBits(tmp);
      frame.pushLong(v);
    });
    Heap.registerMethod("java/lang/Double_longBitsToDouble_(J)D", frame -> {
      Long tmp = frame.popLong();
      double v = Double.longBitsToDouble(tmp);
      frame.pushDouble(v);
    });

    // Integer
    Heap.registerMethod("java/lang/Integer_valueOf_(I)Ljava/lang/Integer;", frame -> {
      KClass clazz = Heap.findClass("java/lang/Integer");
      KObject kObject = clazz.newObject();
      kObject.setField("value", "I", new Slot[] {new Slot(frame.popInt(), Slot.INT)});
      frame.pushRef(kObject);
    });

    // Exception
    Heap.registerMethod("java/lang/Exception_<init>_(Ljava/lang/String;)V", frame -> {
      KObject str = (KObject) frame.popRef();
      KObject thisObj = (KObject) frame.popRef();
      KField msgField = thisObj.getField("detailMessage", "Ljava/lang/String;");
      msgField.val = new Slot[] {new Slot(str)};
    });

    // Throwable
    Heap.registerMethod("java/lang/Throwable_<clinit>_()V", frame -> {
    });
    Heap.registerMethod("java/lang/Throwable_<init>_(Ljava/lang/String)Ljava/lang/Throwable;", frame -> {
    });
    Heap.registerMethod("java/lang/Throwable_fillInStackTrace_(I)Ljava/lang/Throwable;", frame -> {
    });
    Heap.registerMethod("java/lang/Throwable_fillInStackTrace_()Ljava/lang/Throwable;", frame -> {
    });
    Heap.registerMethod("java/lang/Throwable_getStackTraceDepth_()I", frame -> {
    });
    Heap.registerMethod("java/lang/Throwable_getStackTraceElement_(I)Ljava/lang/StackTraceElement;", frame -> {
    });

    // random
    Heap.registerMethod("java/util/Random_<clinit>_()V", frame -> {
    });
    Heap.registerMethod("java/util/Random_<init>_()V", frame -> {
      frame.popRef();
    });
    Heap.registerMethod("java/util/Random_<init>_(J)V", frame -> {
      frame.popLong();
      frame.popRef();
    });
    Heap.registerMethod("java/util/Random_nextInt_()I", frame -> {
      int tmp = new Random().nextInt();
      frame.popRef();
      frame.pushInt(tmp);
    });

    Heap.registerMethod("java/util/Random_nextInt_(I)I", frame -> {
      Integer rg = frame.popInt();
      frame.popRef();
      int tmp = new Random().nextInt(rg);
      frame.pushInt(tmp);
    });

    // atomic
    Heap.registerMethod("java/util/concurrent/atomic/AtomicLong_VMSupportsCS8_()Z", frame -> {
      frame.pushInt(0);
    });
    Heap.registerMethod("java/util/concurrent/atomic/AtomicLong_<clinit>_()V", frame -> {
    });
  }

  private static void loadFoundationClass(ClassLoader classLoader) {
    // class
    KClass metaClass = classLoader.loadClass("java/lang/Class");
    for (KClass cls : Heap.getClasses()) {
      if (cls.getRuntimeClass() == null) {
        KObject obj = metaClass.newObject();
        cls.setRuntimeClass(obj);
        obj.setMetaClass(cls);
      }
    }

    // primitive
    classLoader.loadPrimitiveClass("char");
    classLoader.loadPrimitiveClass("boolean");
    classLoader.loadPrimitiveClass("byte");
    classLoader.loadPrimitiveClass("short");
    classLoader.loadPrimitiveClass("int");
    classLoader.loadPrimitiveClass("long");
    classLoader.loadPrimitiveClass("float");
    classLoader.loadPrimitiveClass("double");

    // string
    classLoader.loadClass("java/lang/String");

    // primitive wrapper
    classLoader.loadClass("java/lang/Character");
    classLoader.loadClass("java/lang/Boolean");
    classLoader.loadClass("java/lang/Byte");
    classLoader.loadClass("java/lang/Short");
    classLoader.loadClass("java/lang/Integer");
    classLoader.loadClass("java/lang/Long");
    classLoader.loadClass("java/lang/Float");
    classLoader.loadClass("java/lang/Double");
  }
}
