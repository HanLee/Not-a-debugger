package com.nad;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IEUser on 8/27/2015.
 */
public class WSServer extends WebSocketServer {

    MainActivity mainActivity;

    public WSServer(int port, MainActivity ma) throws UnknownHostException {
        super (new InetSocketAddress(port));
        this.mainActivity = ma;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.v("nad", "connection from " + conn.getRemoteSocketAddress());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "log");
            jsonObject.put("data", "////////////////////////////\n///////// Not A Debugger //////////\n////////////////////////////");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendToAll(jsonObject.toString());

        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "currentHookedApplication");
            jsonObject.put("data", mainActivity.getHookedPackage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendToAll(jsonObject.toString());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String[] pMessage = message.split(" ");
        String command = pMessage[0];
        JSONObject jsonObject = new JSONObject();

        switch(command){
            case "help":
                sendToAll("Available commands: showPackages, hookPackage, showHookedPackage");
                break;
            case "showPackages":
                mainActivity.refreshPackagesList();
                ArrayList<String> packages = mainActivity.getPackagesList();
                String sPackages = "";
                JSONObject jsonObject1 = new JSONObject();
                for(String name: packages) {
                    sPackages += name;
                    sPackages += "\n";
                }
                try {
                    jsonObject1.put("type", "hookableApplications");
                    JSONArray jsonArray = new JSONArray(packages);
                    jsonObject1.put("data", jsonArray);
                    jsonObject.put("type", "log");
                    jsonObject.put("data", sPackages);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //sendToAll(jsonObject.toString());
                sendToAll((jsonObject1.toString()));

                break;
            case "hookPackage":
                String packageName = pMessage[1].trim();
                try {
                    jsonObject.put("type", "log");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(packageName.equals("") || packageName==null)
                    try {
                        jsonObject.put("data", "missing package name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                else{
                    if(mainActivity.setHookedPackage(packageName))
                        try {
                            jsonObject.put("data", "Successfully hooked "+packageName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    else
                        try {
                            jsonObject.put("data", "Error hooking " + packageName + ", did you enter correctly?");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
                sendToAll(jsonObject.toString());
                break;
            case "showHookedPackage":
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "log");
                    jsonObject.put("data", mainActivity.getHookedPackage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case "hookMethod":
                Log.v("nad", message);
                int indexOfSpace = message.indexOf(" ");
                /*changing delimiter from ; to - cause arrays need ;*/
                int indexOfSC = message.indexOf("-");
                String methodToHook = message.substring(indexOfSpace + 1, indexOfSC/*message.length()*/);
                String hookState = message.substring(indexOfSC+1,message.length());
                if(methodToHook.equals("") || methodToHook==null) {
                    try {
                        jsonObject.put("type", "log");
                        jsonObject.put("data", "missing a method name to hook");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.v("nad", "AAAAAA" + methodToHook);
                    Log.v("nad", "BBBBBB" + hookState);
                    mainActivity.setHookedMethods(methodToHook, hookState);
                }
                break;
            /*
            case "clearAllHookedMethods":
                mainActivity.clearAllHookedMethods();
                break;
                */
            case "showHookedMethods":
                mainActivity.getHookedMethods();
                break;
            /*
            case "releaseLock":
                Log.v("nad", "got release lock from web page");
                //mainActivity.releaseLock();
                break;
            case "setLock":
                Log.v("nad", "got set lock from web page");
                mainActivity.setLock();
                break;
                */
            /*
            case "ping":
                Log.v("nad", "executing ping");
                mainActivity.ping();
                break;
                */
            case "methodInfo":
                String encodedMethodInfo = pMessage[1].trim();
                Log.v("nad", "received edited methodInfo: " + encodedMethodInfo);
                mainActivity.sendEditedMethodInfoToXposed(encodedMethodInfo);
                break;
            case "toggleTraceMode":
                Log.v("nad", "tracemode new status: " + pMessage[1].trim());
                mainActivity.toggleTraceMode(pMessage[1].equals("true") ? true : false);
                break;
            case "getTraceModeStatus":
                Log.v("nad", "getTraceModeStatus");
                mainActivity.getTraceModeStatus();
                break;
            case "toggleCanaryMode":
                Log.v("nad", "canarymode new status: " + pMessage[1].trim());
                mainActivity.toggleCanaryMode(pMessage[1].equals("true") ? true : false);
                break;
            case "getCanaryModeStatus":
                Log.v("nad", "getCanaryModeStatus");
                mainActivity.getCanaryModeStatus();
                break;
            case "getCanaryToken":
                Log.v("nad", "getCanaryToken");
                mainActivity.getCanaryToken();
                break;
            case "setCanaryToken":
                Log.v("nad", "setCanaryToken");
                mainActivity.setCanaryToken(pMessage[1].trim());
                break;
            /*
            case "toggleANRStatus":
                Log.v("nad", "toggleANRStatus");
                mainActivity.toggleANRStatus(pMessage[1].equals("true") ? true : false);
                break;
            case "getANRStatus":
                Log.v("nad", "getANRStatus");
                mainActivity.getANRStatus();
                break;
            */
            /*
            case "showSystemFunctions":
                break;
                */
            default:
                sendToAll("unknown command, please try again");
                break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    public void sendToAll(String text){
        Collection<WebSocket> con = connections();
        synchronized (con){
            for(WebSocket c : con){
                c.send(text);
            }
        }
    }

}
