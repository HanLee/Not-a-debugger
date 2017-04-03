package com.nad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.HookStates;
import shared.HookedMethodInfo;
import shared.MethodInfo;

public class MainActivity extends AppCompatActivity {

    WSServer wsServer;
    BroadcastReceiver myActivityReceiver;
    ArrayList<String> appNameList = new ArrayList<String>();
    HashMap<String, String> nameMap = new HashMap<String,String>();
    ArrayList<String> hookedAppMethodsList = new ArrayList<String>();
    HashMap<String, ArrayList<String>> hookedAppClassMethodsHM = new HashMap<String, ArrayList<String>>();
    String targetAppDataPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();

    }

    public void setUp(){
        TextView ipTextView = (TextView) findViewById(R.id.ipTextView);
        ipTextView.setText(getWifiIPAddress());
        Switch serverSwitch = (Switch) findViewById(R.id.serverSwitch);
        serverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    wsServer.start();
                else if(!isChecked)
                    try {
                        wsServer.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        });

        try {
            wsServer = new WSServer(8888, this);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Declare our receiver. Overriding the onReceive() method.
        myActivityReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent data) {

                String action = data.getStringExtra("action");
                if(action.equals("broadcastMethods"))
                {
                   Log.v("nad", "Got a broadcast, sending methods");

                    String diorand_data_path =  data.getStringExtra("info");
                    targetAppDataPath = data.getStringExtra("info2");

                   Log.v("nad", "main received : " + diorand_data_path);
                    try {
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(diorand_data_path));
                        HashMap<String, ArrayList<String>> classAndMethodsList = (HashMap<String, ArrayList<String>>) ois.readObject();
                        hookedAppClassMethodsHM = classAndMethodsList;
                       Log.v("nad", "printing class and methods");
                        JSONObject jsonObject1 = new JSONObject();
                        JSONArray jsonArray1 = new JSONArray();
                        JSONObject cmi = null;

                        for (Map.Entry<String, ArrayList<String>> entry : hookedAppClassMethodsHM.entrySet())
                        {
                            cmi = new JSONObject();
                            try {
                                //cmi.put(entry.getKey(), entry.getValue());
                                cmi.put("class", entry.getKey());
                                JSONArray ja = new JSONArray(entry.getValue());
                                cmi.put("methods", ja);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            jsonArray1.put(cmi);
                        }


                        try {
                            jsonObject1.put("type", "hookableFunctions");
                            jsonObject1.put("data", jsonArray1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                       Log.v("nad", "jsonobject1" + jsonArray1.toString());
                        wsServer.sendToAll(jsonObject1.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (action.equals("hook"))
                {
                    data.getStringExtra("hook").equals("x");
                    setLock();
                }
                else if (action.equals("methodInfo"))
                {
                    MethodInfo x = (MethodInfo) data.getSerializableExtra("methodInfo");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = null;
                    byte[] methodInfoBytes = null;
                    try {
                        out = new ObjectOutputStream(bos);
                        out.writeObject(x);
                        methodInfoBytes = bos.toByteArray();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException ex) {
                            // ignore close exception
                        }
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            // ignore close exception
                        }
                    }
                    String encodedMethod = Base64.encodeToString(methodInfoBytes, Base64.NO_WRAP);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", "methodInfo");
                        jsonObject.put("data", encodedMethod);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   Log.v("nad", "sending:" + encodedMethod);
                    wsServer.sendToAll(jsonObject.toString());

                }
                else if (action.equals("notifyHookedMethodChanged"))
                {
                    MainActivity.this.notifyHookedMethodChanged();
                }
                else if (action.equals("broadcastSystemFunctions"))
                {
                    Log.v("nad", "got broadcastsystemFunctions");
                    String filePath = data.getStringExtra("info");
                    Log.v("nad", "received broadcastSystemFunctions path " + filePath);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", "hookedMethods");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    HashMap<String, HashMap<Integer, String>> hm = new HashMap<>();

                    FileInputStream fileInputStream  = null;
                    try {
                        fileInputStream = new FileInputStream(filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    ObjectInputStream objectInputStream = null;
                    try {
                        objectInputStream = new ObjectInputStream(fileInputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        hm = (HashMap) objectInputStream.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    JSONArray availableSystemFunctionsJSONArray = new JSONArray();
                    for (Map.Entry<String, HashMap<Integer, String>> entry : hm.entrySet()) {
                        JSONObject systemFunctionDescriptionJSONObject = new JSONObject();
                        String key = entry.getKey();
                        Log.v("nad", ">> system key: " + key);
                        HashMap<Integer, String> value = entry.getValue();
                        JSONArray systemFunctionMethodsArray = new JSONArray();
                        for (Map.Entry<Integer, String> entry1 : value.entrySet()) {
                            JSONObject jsonObject1 = new JSONObject();
                            Integer key1 = entry1.getKey();
                            String value1 = entry1.getValue();
                            Log.v("nad", ">>>> index: " + String.valueOf(key1) + " : " + value1);
                            try {
                                jsonObject1.put(String.valueOf(key1), value1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            systemFunctionMethodsArray.put(jsonObject1);
                        }
                        try {
                            systemFunctionDescriptionJSONObject.put(key, systemFunctionMethodsArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        availableSystemFunctionsJSONArray.put(systemFunctionDescriptionJSONObject);
                        //Log.v("nad", "JSONARRAY : " + jsonArray.toString());
                    }

                    //hashmap <classname <id, descriptino>>
                    //ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

                    //JSONObject jsonObject1 = new JSONObject(hm);
                    JSONObject packagedJSONObject = new JSONObject();
                    try {
                        packagedJSONObject.put("type", "availableSystemMethods");
                        packagedJSONObject.put("data", availableSystemFunctionsJSONArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Log.v("nad", "SYSTEMFUNCTIONS" + packagedJSONObject.toString());
                    //wsServer.sendToAll(packagedJSONObject.toString());

                    /*
                    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

                    for (Object ob : hm.entrySet())
                    {
                        Map.Entry entry = (Map.Entry) ob;
                        JSONObject tmp = new JSONObject();
                        JSONArray tmpArray = null;
                        try {
                            tmp.put("class", entry.getKey());
                            tmpArray = new JSONArray((ArrayList<String>) entry.getValue());
                            tmp.put("methods", tmpArray);
                        } catch (JSONException e) {
                            Log.e("diorand", e.toString());
                        }
                        jsonObjectArrayList.add(tmp);
                    }

                    JSONArray data = new JSONArray(jsonObjectArrayList);
                    try {
                        jsonObject.put("data", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("diorand", "HELLOTHERE");
                    Log.v("diorand", "hookedmethods" + data.toString());
                    wsServer.sendToAll(jsonObject.toString());
                    */
                }
            }
        };

        //Create an IntentFilter to listen for our ACTION string.
        IntentFilter changeColorFilter = new IntentFilter("com.nad.GOT_INFO");

        //Finally, register the receiver.
        registerReceiver(myActivityReceiver, changeColorFilter);

        refreshPackagesList();
    }

    public String getWifiIPAddress()
    {
        /*
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
                */
        return "127.0.0.1";
    }

    public void refreshPackagesList()
    {
        final PackageManager pm = getPackageManager();
        appNameList.clear();
        nameMap.clear();
        //get a list of installed apps.
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < packages.size(); ++i) {
            appNameList.add(packages.get(i).packageName);
            nameMap.put(packages.get(i).packageName, packages.get(i).sourceDir);
        }
    }

    public ArrayList getPackagesList()
    {
        return appNameList;
    }

    public boolean setHookedPackage(String packageName)
    {
        SharedPreferences.Editor editor = getSharedPreferences("NAD_HOOKED_APP", MODE_WORLD_READABLE).edit();
        editor.clear();
        editor.commit();
        editor.putString("app", packageName);
        editor.commit();

       Log.v("nad", "new hooked: " + getHookedPackage());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "currentHookedApplication");
            jsonObject.put("data", getHookedPackage());
            wsServer.sendToAll(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getHookedPackage()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("NAD_HOOKED_APP", MODE_WORLD_READABLE);
        String packageSourceDir = sharedPreferences.getString("app", "");

        return packageSourceDir;
    }


    public void getHookedMethods()
    {
        Log.v("nad", "hai");
        SharedPreferences sharedPreferences = getSharedPreferences("NAD_HOOKED_METHODS", MODE_WORLD_READABLE);
        /*
        Set<String> methods = sharedPreferences.getStringSet("methods", new HashSet<String>());

        Log.v("diorand", "hai");
        for (String str : methods) {
            Log.v("diorand", str);
        }
        */
        /*
        Map x = sharedPreferences.getAll();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "hookedMethods");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
        for (Object thing : x.entrySet()) {
            Map.Entry foo = (Map.Entry) thing;
            Log.v("diorand", "getHookedMethod key : " + foo.getKey());
            Log.v("diorand", "getHookedMethod value : " + foo.getValue());
            String entry = (String) foo.getKey();
            String className = entry.substring(0, entry.indexOf(":"));
            String methodName = entry.substring(entry.indexOf(":")+1, entry.length());
            if(!hm.containsKey(className))
            {
                ArrayList<String> methods = new ArrayList<String>();
                methods.add(methodName);
                hm.put(className, methods);
            }
            else if (hm.containsKey(className))
            {
                hm.get(className).add(methodName);
            }
        }

        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        for (Object ob : hm.entrySet())
        {
            Map.Entry entry = (Map.Entry) ob;
            JSONObject tmp = new JSONObject();
            JSONArray tmpArray = null;
            try {
                tmp.put("class", entry.getKey());
                tmpArray = new JSONArray((ArrayList<String>) entry.getValue());
                tmp.put("methods", tmpArray);
            } catch (JSONException e) {
                Log.e("diorand", e.toString());
            }
            jsonObjectArrayList.add(tmp);
        }

        JSONArray data = new JSONArray(jsonObjectArrayList);
        try {
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("diorand", "HELLOTHERE");
        Log.v("diorand", "hookedmethods" + data.toString());
        wsServer.sendToAll(jsonObject.toString());
        */
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "hookedMethods");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();

        FileInputStream fileInputStream  = null;
        try {
            fileInputStream = new FileInputStream(targetAppDataPath+"/"+"nad_hooked_methods.dat");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            hm = (HashMap) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        for (Object ob : hm.entrySet())
        {
            Map.Entry entry = (Map.Entry) ob;
            JSONObject tmp = new JSONObject();
            JSONArray tmpArray = null;
            try {
                tmp.put("class", entry.getKey());
                ArrayList<String> arrayListOfMethods = new ArrayList<>();
                //entry.getValue will return an ArrayList of HookedMethodInfo
                for(HookedMethodInfo hmi : (ArrayList<HookedMethodInfo>)entry.getValue())
                {
                    arrayListOfMethods.add(hmi.getMethodName());
                }
                tmpArray = new JSONArray(arrayListOfMethods);
                tmp.put("methods", tmpArray);
            } catch (JSONException e) {
                Log.e("diorand", e.toString());
            }
            jsonObjectArrayList.add(tmp);
        }

        JSONArray data = new JSONArray(jsonObjectArrayList);
        try {
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("diorand", "HELLOTHERE");
        Log.v("diorand", "hookedmethods" + data.toString());
        wsServer.sendToAll(jsonObject.toString());
    }

    public void setHookedMethods(String hookedMethod, String hookState)
    {
        Log.v("nad", hookedMethod);

        //send intents
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nad.CHANGED");
        intent.putExtra("action", "HOOKED_METHODS_CHANGED");
        intent.putExtra("hooked_method", hookedMethod);
        intent.putExtra("hooked_state", hookState);
        sendBroadcast(intent);

    }

    public void notifyHookedMethodChanged()
    {
        this.getHookedMethods();
    }

    /*
    public void releaseLock()
    {
       Log.v("nad", "in mainactivity release lock");
        //send intents
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.diorand.EXTRA");
        intent.putExtra("action", "RELEASE_LOCK");
        sendBroadcast(intent);

        SharedPreferences.Editor editor = getSharedPreferences("DIORAND_HOOK_STATUS", MODE_WORLD_READABLE).edit();
        editor.putString("hooked", "false");
        editor.clear();
        editor.commit();

    }
    */
    public void setLock()
    {
        SharedPreferences.Editor editor = getSharedPreferences("DIORAND_HOOK_STATUS", MODE_WORLD_READABLE).edit();
        editor.putString("hooked", "true");
        editor.clear();
        editor.commit();
    }

    public void clearAllHookedMethods(){
        SharedPreferences.Editor editor = getSharedPreferences("DIORAND_HOOKED_METHODS", MODE_WORLD_READABLE).edit();
        editor.clear();
        editor.commit();

        //send intents
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.diorand.CHANGED");
        intent.putExtra("action", "HOOKED_METHODS_CHANGED");
        sendBroadcast(intent);
    }

    public void sendEditedMethodInfoToXposed(String encodedMethodInfo){
        byte[] ba = Base64.decode(encodedMethodInfo, Base64.NO_WRAP);
        ByteArrayInputStream in = new ByteArrayInputStream(ba);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MethodInfo mi = null;

        try {
            mi = (MethodInfo) is.readObject();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Log.v("diorand", "test edited methodInfo: " + mi.getMethodParameterValues().get(0).toString());
       Log.v("nad", "test edited methodInfo: " + mi.getMethodParameters().get(0).getParameterValue().toString());

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(/*"com.diorand.HAHA"*/"com.nad.CHANGED");
        intent.putExtra("action", "methodInfo");
        intent.putExtra("methodInfo", mi);
        sendBroadcast(intent);
    }

    //true for on, false for off
    public void toggleTraceMode(boolean traceModeStatus){
        Log.v("nad", "toggleTraceMode: " + String.valueOf(traceModeStatus));
        SharedPreferences.Editor editor = getSharedPreferences("NAD_TRACE_MODE", MODE_WORLD_READABLE).edit();
        editor.putBoolean("traceMode", traceModeStatus);
        editor.apply();
        Log.v("nad", String.valueOf(getSharedPreferences("NAD_TRACE_MODE", MODE_WORLD_READABLE).getBoolean("traceMode", false)));
    }

    public void getTraceModeStatus(){
        String status = String.valueOf(getSharedPreferences("NAD_TRACE_MODE", MODE_WORLD_READABLE).getBoolean("traceMode", false));
        JSONObject tmp = new JSONObject();
        try {
            tmp.put("type", "traceModeStatus");
            tmp.put("data", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsServer.sendToAll(tmp.toString());
    }

    //true for on, false for off
    public void toggleCanaryMode(boolean traceModeStatus){
        Log.v("nad", "toggleCanaryMode: " + String.valueOf(traceModeStatus));
        SharedPreferences.Editor editor = getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).edit();
        editor.putBoolean("canaryMode", traceModeStatus);
        editor.apply();
        Log.v("nad", String.valueOf(getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).getBoolean("canaryMode", false)));
    }

    public void getCanaryModeStatus(){
        String status = String.valueOf(getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).getBoolean("canaryMode", false));
        JSONObject tmp = new JSONObject();
        try {
            tmp.put("type", "canaryModeStatus");
            tmp.put("data", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsServer.sendToAll(tmp.toString());
    }

    public void getCanaryToken(){
        String canaryToken = String.valueOf(getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).getString("canaryToken", ""));
        JSONObject tmp = new JSONObject();
        try {
            tmp.put("type", "canaryToken");
            tmp.put("data", canaryToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsServer.sendToAll(tmp.toString());
    }

    public void setCanaryToken(String canaryToken){
        Log.v("nad", "setCanaryToken: " + String.valueOf(canaryToken));
        SharedPreferences.Editor editor = getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).edit();
        editor.putString("canaryToken", canaryToken);
        editor.apply();
        Log.v("nad", String.valueOf(getSharedPreferences("NAD_CANARY_MODE", MODE_WORLD_READABLE).getString("canaryToken", "")));
    }

    /*
    public void toggleANRStatus(boolean anrStatus){
        Log.v("nad", "in mainactivity toggleANRStatus");
        //send intents

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nad.CHANGED");
        intent.putExtra("action", "toggleANRStatus");
        intent.putExtra("data", anrStatus);
        sendBroadcast(intent);
    }

    public void getANRStatus(){
        String status = String.valueOf(getSharedPreferences("NAD_ANR_MODE", MODE_WORLD_READABLE).getBoolean("anrDisable", false));
        JSONObject tmp = new JSONObject();
        try {
            tmp.put("type", "anrDisableStatus");
            tmp.put("data", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsServer.sendToAll(tmp.toString());
        //wsServer.sendToAll();
    }
    */

    /*
    public void ping(){
       Log.v("nad", "in mainactivity release lock");
        //send intents

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nad.CHANGED");
        intent.putExtra("action", "TEST");
        sendBroadcast(intent);


    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
