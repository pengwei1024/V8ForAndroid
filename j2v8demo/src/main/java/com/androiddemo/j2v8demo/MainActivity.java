package com.androiddemo.j2v8demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.ReferenceHandler;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8Value;
import com.eclipsesource.v8.utils.V8Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 执行JS
        V8 runtime = V8.createV8Runtime();
        int result = runtime.executeIntegerScript(""
                + "var hello = 'hello, ';\n"
                + "var world = 'world!';\n"
                + "hello.concat(world).length;\n");
        runtime.release();
        Log.d(TAG, "length=" + result);

        // 获取对象
        runtime = V8.createV8Runtime();
        runtime.executeVoidScript(""
                + "var person = {};\n"
                + "var hockeyTeam = {name : 'WolfPack'};\n"
                + "person.first = 'Ian';\n"
                + "person['last'] = 'Bull';\n"
                + "person.hockeyTeam = hockeyTeam;\n");
        V8Object person = runtime.getObject("person");
        V8Object hockeyTeam = person.getObject("hockeyTeam");
        Log.d(TAG, "person.hockeyTeam.name=" + hockeyTeam.getString("name"));
        person.release();
        hockeyTeam.release();
        runtime.release();

        // 注册方法
        runtime = V8.createV8Runtime();
        JavaVoidCallback callback = new JavaVoidCallback() {
            @Override
            public void invoke(V8Object receiver, V8Array parameters) {
                if (parameters.length() > 0) {
                    Object arg1 = parameters.get(0);
                    Log.d(TAG, "arg1=" + arg1);
                    if (arg1 instanceof Releasable) {
                        ((Releasable) arg1).release();
                    }
                }
            }
        };
        runtime.registerJavaMethod(callback, "print");
        runtime.executeScript("print('hello, world');");
        runtime.release();

        // 注册对象
        runtime = V8.createV8Runtime();
        Console console = new Console();
        V8Object v8Console = new V8Object(runtime);
        runtime.add("console", v8Console);
        v8Console.registerJavaMethod(console, "log", "log", new Class<?>[]{String.class});
        v8Console.registerJavaMethod(console, "err", "err", new Class<?>[]{String.class});
        v8Console.release();
        runtime.executeScript("console.log('hello, world');");
        runtime.release();

        // 独立线程执行
        V8Executor executor = new V8Executor("var a='a';var b='b';a+b");
        executor.start();
        try {
            executor.join();
            String executorResult = executor.getResult();
            Log.d(TAG, "executorResult=" + executorResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    class Console {
        public void log(final String message) {
            Log.d(TAG, "Console:" + message);
        }

        public void err(final String message) {
            Log.e(TAG, "Console:" + message);
        }
    }
}
