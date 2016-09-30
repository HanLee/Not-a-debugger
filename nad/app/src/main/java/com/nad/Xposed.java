package com.nad;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.thoughtworks.xstream.XStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexFile;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import shared.HookStates;
import shared.MethodInfo;
import shared.ParameterInfo;

/**
 * Created by Administrator on 4/28/2016.
 */
public class Xposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    XSharedPreferences selectedAppPref;
    HashMap<String, ArrayList<String>> classAndMethodsList = new HashMap<>();
    private Context targetContext;
    final ConcurrentHashMap<String, XC_MethodHook.Unhook> hookedMethodList = new ConcurrentHashMap<>();
    final HashMap<String, ArrayList<String>> hookedMethodsTracker = new HashMap<>();
    private final Object mPauseLock = new Object();
    private boolean mPaused;
    private MethodInfo globalMethodInfo;
    private int processId =0;
    private boolean traceMode=false;
    boolean disableANR = true;
    String canaryToken = "";
    boolean canaryMode = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        /*
        First we shall open up nad preference file from
        com.nad in order to see what is the application the user would like to hook.
         */
        selectedAppPref = new XSharedPreferences("com.nad", "NAD_HOOKED_APP");
        selectedAppPref.makeWorldReadable();
        String selectedApp;

        /*
        The name of the application to be hooked is stored with the key "app" in selectedAppPref
         */
        selectedApp = selectedAppPref.getString("app", "");

        XSharedPreferences traceModePref = new XSharedPreferences("com.nad", "NAD_TRACE_MODE");
        traceModePref.makeWorldReadable();
        traceMode = traceModePref.getBoolean("traceMode", false);

        XSharedPreferences canaryModePref = new XSharedPreferences("com.nad", "NAD_CANARY_MODE");
        canaryModePref.makeWorldReadable();
        canaryMode = canaryModePref.getBoolean("canaryMode", false);
        //Log.v("nad", "canarymode debug: " + canaryMode);
        canaryToken = canaryModePref.getString("canaryToken", "a1b2c3d4");
        //Log.v("nad", "canarytoken debug: " + canaryToken);

        /*
        set this to false always, just in case.
         */
        /*
        XSharedPreferences disableANRStatusPref = new XSharedPreferences("com.nad", "NAD_ANR_MODE");
        disableANRStatusPref.makeWorldReadable();
        disableANR = disableANRStatusPref.getBoolean("anrDisable", false);
        */


/*
        Class<?> foundClass = XposedHelpers.findClass("com.android.server.am.MainHandler", lpparam.classLoader);
        Method foundMethod = XposedHelpers.findMethodExact(foundClass, "validateNotAppThread");
  */

        if(!lpparam.packageName.equals(selectedApp))
            return;

        String sourceDir = lpparam.appInfo.sourceDir;
        Log.v("nad", ">> in " + selectedApp);

        long startTime = System.nanoTime();

        processId = android.os.Process.myPid();
        Log.v("nad", "got processId: " + processId);

        analyzeApp(sourceDir, lpparam);

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);

        Log.v("nad", "duration to analyze: " + duration / 100000 + "milliseconds");

    }



    public void analyzeApp(String sourceDir, final XC_LoadPackage.LoadPackageParam lpparam)
    {
        Log.v("nad", "going to analyze");
        /*
        Get the DexFile by using the path to the apk file.
         */
        DexFile dexFile = null;
        try{
            dexFile = new DexFile(sourceDir);
        } catch (Exception ex)
        {
            Log.e("nad", "Unable to load DexFile");
        }

        /*
        If we get thus far, it should be okay, get all the classes!
         */
        Enumeration<String> foundClasses = dexFile.entries();

        ArrayList<String> tmpMethodsList;

        while (foundClasses.hasMoreElements()) {

            //Han: Get class name
            final String entry = foundClasses.nextElement();

            //Create a new empty list for the methods
            tmpMethodsList = new ArrayList<>();

            //We dont' want all the android stuff.
            if(!entry.startsWith("android."))
            {
                Class foundClass = null;
                try {
                    foundClass = XposedHelpers.findClass(entry, lpparam.classLoader);
                } catch (XposedHelpers.ClassNotFoundError e) {
                    //Oops
                }

                if (foundClass != null && !foundClass.isInterface() /*&& !Modifier.isAbstract(foundClass.getModifiers())*/)
                {
                    //Use java's method to get all declared methods of the class
                    Method[] foundMethods = null;
                    try {
                        foundMethods = foundClass.getDeclaredMethods();
                    } catch (Error e) {
                        //Oops
                    }
                    if (foundMethods != null) {
                        if (foundMethods.length > 0) {
                            for (final Method method : foundMethods) {
                                if(!Modifier.isAbstract(method.getModifiers())){
                                    tmpMethodsList.add(Util.generateHRMD(method));

                                    /*
                                    Here we hook the onCreate function.
                                    By the time the onCreate function is called, we will have finished
                                    analysing the application and stored them all in the "classAndMethodsList".

                                    In onCreate, we will do a few things.
                                    1) We use the application context to write the functions out into nad.dat.
                                    2) We spawn a thread and use it to listen to intents for changes.
                                       The reason for this is that the dirtyWait() function blocks the main
                                       application from receiving messages.
                                     */
                                    if (targetContext == null && method.getName().equals("onCreate") && method.getParameterTypes().length==1 && traceMode == false && canaryMode == false)
                                    {
                                        Log.v("nad", "got oncreate!");
                                        Method foundMethod = XposedHelpers.findMethodExact(foundClass, method.getName(), method.getParameterTypes());

                                        XC_MethodHook xcMethodHook = new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                                                /*
                                                Getting the application context
                                                 */
                                                targetContext = AndroidAppHelper.currentApplication().getApplicationContext();

                                                /*
                                                The name of the file where application class and methods will be written to in the
                                                target victim's temporary folder.
                                                 */
                                                String nad_data = "nad.dat";

                                                String nad_data_path = targetContext.getFilesDir() + "/" + nad_data;
                                                FileOutputStream fos = targetContext.openFileOutput(nad_data, Context.MODE_WORLD_READABLE);
                                                ObjectOutputStream oos = new ObjectOutputStream(fos);
                                                oos.writeObject(classAndMethodsList);
                                                oos.close();

                                                /*
                                                Notify NAD that we have done the analysis and to grab the file to parse for user.
                                                 */
                                                Intent intent = new Intent();
                                                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                                intent.setAction("com.nad.GOT_INFO");
                                                intent.putExtra("action", "broadcastMethods");
                                                intent.putExtra("info", nad_data_path);
                                                String target_app_dir = targetContext.getFilesDir().toString();
                                                intent.putExtra("info2", target_app_dir);
                                                targetContext.sendBroadcast(intent);

                                                //let's receive notifications when we want to hook/unhook methods
                                                //for reasons described in 2) above.
                                                HandlerThread handlerThreadx = new HandlerThread("htx");
                                                handlerThreadx.start();
                                                Looper looperx = handlerThreadx.getLooper();
                                                Handler handlerx = new Handler(looperx);

                                                BroadcastReceiver myDiorandReceiver;

                                                myDiorandReceiver = new BroadcastReceiver() {
                                                    @Override
                                                    public void onReceive(Context ctx, Intent data) {
                                                        //Read the action out of the intent
                                                        String action = data.getStringExtra("action");
                                                        Log.v("nad", "in receiver: ");
                                                        if (action.equals("HOOKED_METHODS_CHANGED"))
                                                        {
                                                            String hookedMethod = data.getStringExtra("hooked_method");
                                                            String hookedState = data.getStringExtra("hooked_state");
                                                            if (HookStates.valueOf(hookedState).equals(HookStates.HOOK_NONE))
                                                                removeHook(hookedMethod);
                                                            else
                                                            {
                                                                removeHook(hookedMethod);
                                                                generateHook(hookedMethod, hookedState, lpparam);
                                                            }
                                                        }
                                                        else if (action.equals("methodInfo")) {
                                                            globalMethodInfo = (MethodInfo) data.getSerializableExtra("methodInfo");
                                                            mPaused = false;
                                                        }
                                                        else if (action.equals("toggleANRStatus")) {
                                                            boolean status = data.getBooleanExtra("data", false);
                                                            disableANR = status;
                                                        }
                                                    }
                                                };


                                                //Create an IntentFilter to listen for our ACTION string.
                                                IntentFilter changeFilter = new IntentFilter("com.nad.CHANGED");

                                                //Finally, register the receiver.
                                                AndroidAppHelper.currentApplication().getApplicationContext().registerReceiver(myDiorandReceiver, changeFilter, null, handlerx);
                                            }
                                        };
                                                XposedBridge.hookMethod(foundMethod, xcMethodHook);
                                    }
                                    else if(traceMode == true && canaryMode == false)
                                    {
                                        //Log.v("nad", "tracemodeeee");
                                        generateTraceHook(foundClass, method, lpparam);
                                    }
                                    else if(canaryMode == true && traceMode == false)
                                    {
                                        //Log.v("nad", "canary mode");
                                        generateCanaryHook(foundClass, method, lpparam);
                                    }
                                }
                            }
                        }
                    }
                    if(tmpMethodsList.size() > 0)
                        classAndMethodsList.put(entry,tmpMethodsList);
                }
            }

        }
        Log.v("nad", "Done analyzing");
    }

    public void generateTraceHook(Class foundClass, final Method foundMethod, XC_LoadPackage.LoadPackageParam lpparam) {
        final Method method = null;
        if (foundClass != null && !foundClass.isInterface()) {
            XC_MethodHook xcMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.v("nad", Util.generateHRMD(foundMethod) + " was called.");
                }
            };

        /*
        Hook it!
         */
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(foundMethod, xcMethodHook);
        }
    }

    public void generateCanaryHook(Class foundClass, final Method foundMethod, XC_LoadPackage.LoadPackageParam lpparam) {
        final Method method = null;
        if (foundClass != null && !foundClass.isInterface()) {
            XC_MethodHook xcMethodHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    //Log.v("nad", Util.generateHRMD(foundMethod) + " CANARY DEBUG");
                    for (int i=0; i<param.args.length; i++)
                    {
                        if (param.args[i] instanceof String)
                        {
                            if (((String) param.args[i]).contains(canaryToken)){
                                Log.v("nad", Util.generateHRMD(foundMethod) + " contains canary token");
                            }
                        }
                    }
                }
            };

        /*
        Hook it!
         */
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(foundMethod, xcMethodHook);
        }
    }

    public void generateHook(String hookedMethod, final String hookedState, XC_LoadPackage.LoadPackageParam lpparam)
    {
        /*
        String hookedMethod example: org.teamsik.apps.hackingchallenge.a:boolean verifyPassword (android.content.Context, java.lang.String)
        elements[0] : org.teamsik.apps.hackingchallenge.a (Classname)
        elements[1] : boolean verifyPassword (android.content.Context, java.lang.String) (method name parsed earlier on by Util.HRMD
         */
        String[] elements = hookedMethod.split(":", 2);
        Log.v("nad", "Class: " + elements[0]);
        /*
        So here we will just grab the class, and loop through all methods and pass it to Util.HRMD and see if it is the same as elements[1]
        If it is the same, it is the class and method we are looking for
         */
        Class<?> foundClass = XposedHelpers.findClass(elements[0], lpparam.classLoader);
        Method method = null;
        if (foundClass != null && !foundClass.isInterface())
        {
            //Use java's method to get all declared methods of the class
            Method[] foundMethods = null;
            try {
                foundMethods = foundClass.getDeclaredMethods();
            } catch (Error e) {
                //Oops
            }
            if (foundMethods != null) {
                if (foundMethods.length > 0) {
                    for (Method tmpMethod : foundMethods) {
                        if(!Modifier.isAbstract(tmpMethod.getModifiers())){
                            if(Util.generateHRMD(tmpMethod).equals(elements[1]))
                            {
                                Log.v("nad", "HUAT AHHH: " + Util.generateHRMD(tmpMethod) + " : " + elements[1]);
                                method = tmpMethod;
                            }
                        }
                    }
                }
            }
        }

        /*
        If we find the method we are looking for , we will generate a hook for it
        We will generate the beforeHookedMethod and the afterHookedMethod hooks for
        the method that we are hooking. This is so as to allow the user to hook input
        parameters and also control the return value (as much as possible).
         */
        final Method methodToHook = method;
        XC_MethodHook xcMethodHook = new XC_MethodHook() {

            /*
            To allow the user to modify the input parameters, we create a MethodInfo class.
            The MethodInfo object will contain all the values and types of the parameters
            (as much as possible anyway).

            Then we  pause the application using dirtyWait() which is basically just
            to sleep() for 1 second indefinitely.

            We then send the MethodInfo object to the user where it is then rendered on the
            client and modifications are made. Once that is done, the new MethodInfo object
            is then sent back to the hook method, where we will get the values and reconstruct
            the parameters and replace the old ones with the user defined ones.

            We then unlock the boolean to break out of the sleep().
             */
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if((HookStates.valueOf(hookedState).equals(HookStates.HOOK_INPUT_ONLY)) || (HookStates.valueOf(hookedState).equals(HookStates.HOOK_ALL)))
                {

                    Log.v("nad", "BEFOREEE");
                    Log.v("nad", "sending broadcast to hook");

                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setAction("com.nad.GOT_INFO");

                    /*
                    Generate MethodInfo which will contain all we need to modify the params
                     */
                    MethodInfo mi = new MethodInfo(methodToHook, param);

                    intent.putExtra("action", "methodInfo");
                    intent.putExtra("methodInfo", mi);

                    /*
                    Send the MethodInfo object to the user client for modification
                     */
                    if (targetContext != null) {
                        Log.v("nad", "sending broadcast to hook");
                        targetContext.sendBroadcast(intent);
                    }

                    /*
                    This variable will determine if the app can wake, we set it to true
                    so it will sleep.
                     */
                    mPaused = true;

                    /*
                    sleep indefinitely
                     */
                    dirtyWait();

                    /*
                    if we get here, it means that we have received back a modified
                    MethodInfo object from the user client and the sleep() was broken.
                    We can then replace the old input parameters with the ones that the user modified.
                     */
                    for (int x = 0; x < globalMethodInfo.getMethodParameters().size(); x++) {
                        Log.v("nad", "received back methodinfo: " + globalMethodInfo.getMethodParameters().get(x).getParameterValue().toString());
                        ParameterInfo pi = globalMethodInfo.getMethodParameters().get(x);

                        Object ob = pi.getParameterValue();

                        String type = pi.getParameterClass();
                        Log.v("nad", "parameterClass: " + type);
                        if(!pi.getIsXStream())
                        {
                            param.args[pi.getParameterIndex()] = Util.getReconstructedObject(type, ob);
                        }
                        else if(pi.getIsXStream())
                        {
                            XStream xStream = new XStream();
                            Log.v("nad", "got a xstream object, attempting to build it");
                            param.args[x] = xStream.fromXML(pi.getParameterValue().toString());
                        }
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if((HookStates.valueOf(hookedState).equals(HookStates.HOOK_RETURN_ONLY)) || (HookStates.valueOf(hookedState).equals(HookStates.HOOK_ALL)))
                {
                    Log.v("nad", "AFTERRRRR");
                    MethodInfo mi = new MethodInfo(methodToHook, param, true);
                    Intent intent2 = new Intent();
                    intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent2.setAction("com.nad.GOT_INFO");
                    intent2.putExtra("action", "methodInfo");
                    intent2.putExtra("methodInfo", mi);

                    if (targetContext != null) {
                        Log.v("nad", "sending broadcast to hook");
                        targetContext.sendBroadcast(intent2);
                    }

                    mPaused = true;
                    dirtyWait();

                    Log.v("nad", "received back methodinfo: " + globalMethodInfo.getMethodParameters().get(0).getParameterValue().toString());

                    Object ob = globalMethodInfo.getMethodParameters().get(0).getParameterValue();

                    ParameterInfo pi = globalMethodInfo.getMethodParameters().get(0);
                    String type = pi.getParameterClass();

                    if (!pi.getIsXStream())
                    {
                        param.setResult(Util.getReconstructedObject(type, ob));
                    }
                    else if(pi.getIsXStream())
                    {
                        XStream xStream = new XStream();
                        Log.v("nad", "got a xstream object, attempting to build it");
                        Object x = xStream.fromXML(pi.getParameterValue().toString());
                        param.setResult(x);
                    }
                }
            }
        };


        /*
        Hook it!
         */
        XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(method, xcMethodHook);

        /*
        We maintain a list of hooked methods and the Unhook class, so that we can unhook it later if we want it.
         */
        hookedMethodList.put(hookedMethod, unhook);

        /*
        update the tracker for display purposes
         */
        if(hookedMethodsTracker.containsKey(elements[0]))
        {
            if(!hookedMethodsTracker.get(elements[0]).contains(elements[1]))
                hookedMethodsTracker.get(elements[0]).add(elements[1]);
        }
        else
        {
            ArrayList<String> methodsList = new ArrayList();
            methodsList.add(elements[1]);
            hookedMethodsTracker.put(elements[0], methodsList);
        }
        updateHookedMethodsFile();
    }

    public void removeHook(String hookedMethod)
    {
        if(hookedMethodList.containsKey(hookedMethod))
        {
            hookedMethodList.get(hookedMethod).unhook();
            hookedMethodList.remove(hookedMethod);


            /*
            update the tracker for display purposes
            */
            String[] elements = hookedMethod.split(":", 2);
            String className = elements[0];
            String methodName =  elements[1];
            if(hookedMethodsTracker.containsKey(className))
            {
                hookedMethodsTracker.get(className).remove(methodName);
            }
            else
            {
                Log.v("nad", "trying to remove a class that does not exist in tracker");
            }
        }
        else
        {
            Log.v("nad", ":(");
        }

        /*
        We shall write out to file the methods that we have hooked.
        Once we have done that, we will then send a notification to NAD
        to grab the latest list to update in the UI.
        A bit primitive I know..
         */
        updateHookedMethodsFile();
    }

    private void updateHookedMethodsFile()
    {
        FileOutputStream fileOutputStream = null;
        String nad_data_path = null;
        try {
            //fileOutputStream = new FileOutputStream("nad_hooked_methods");
            String nad_data = "nad_hooked_methods.dat";
            nad_data_path = targetContext.getFilesDir() + "/" + nad_data;
            fileOutputStream = targetContext.openFileOutput(nad_data, Context.MODE_WORLD_READABLE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectOutputStream objectOutputStream= null;
        try {
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            objectOutputStream.writeObject(hookedMethodsTracker);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nad.GOT_INFO");
        intent.putExtra("action", "notifyHookedMethodChanged");

        if (targetContext != null) {
            Log.v("nad", "sending notification for changed methods");
            targetContext.sendBroadcast(intent);
        }
    }

    private void dirtyWait()
    {

        /*
        Log.v("nad", "==============> " + getActivity().getClass().getSimpleName());
        final Activity activity = getActivity();

        ProgressDialog progress;
        progress = ProgressDialog.show(getActivity(), "NAD", "waiting for user response...", true);
        */

        synchronized (mPauseLock) {
            while (mPaused) {
                Log.v("nad", "waiting for return value...");
                try {
                    mPauseLock.wait(1000);

                } catch (Exception e) {
                    Log.v("nad", "Something went wrong while waiting..");
                }
            }
        }
    }

    /*
    public static Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object activityThread = null;
        try {
            activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Field activitiesField = null;
        try {
            activitiesField = activityThreadClass.getDeclaredField("mActivities");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = null;
        try {
            activities = (Map<Object, Object>) activitiesField.get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = null;
            try {
                pausedField = activityRecordClass.getDeclaredField("paused");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            pausedField.setAccessible(true);
            try {
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = null;
                    try {
                        activityField = activityRecordClass.getDeclaredField("activity");
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    activityField.setAccessible(true);
                    Activity activity = null;
                    try {
                        activity = (Activity) activityField.get(activityRecord);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return activity;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    */

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

        Class ActivityManagerServiceClazz = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", null);

        XposedBridge.hookAllMethods(ActivityManagerServiceClazz, "appNotResponding", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if(disableANR)
                    return null;
                else
                    return XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
            }
        });

    }
}
