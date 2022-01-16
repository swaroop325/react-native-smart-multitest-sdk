package com.reactnativesmartmultitestsdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

@ReactModule(name = SmartMultitestSdkModule.NAME)
public class SmartMultitestSdkModule extends ReactContextBaseJavaModule {
    public static final String NAME = "SmartMultitestSdk";

    public SmartMultitestSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    public void sendEvent(String eventName, @Nullable WritableMap params) {
        getReactApplicationContext().getJSModule(RCTNativeAppEventEmitter.class).emit(eventName, params);
    }

    @ReactMethod
    public void start(Callback callback) {
        //to be implemented
    
    }

    @ReactMethod
    public void connect(Callback callback) {
        //to be implemented
    
    }

    @ReactMethod
    public void read(Callback callback) {
        //to be implemented
    
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(int a, int b, Promise promise) {
        promise.resolve(a * b);
    }

    public static native int nativeMultiply(int a, int b);
}
