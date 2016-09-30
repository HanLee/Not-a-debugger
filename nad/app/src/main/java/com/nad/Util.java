package com.nad;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Administrator on 4/19/2016.
 */
public final class Util {
    // private constructor to avoid unnecessary instantiation of the class
    private Util() {
    }

    public static final String generateHRMD(Method method) {
        String methodName = method.getName();
        String className = method.getDeclaringClass().getCanonicalName();
        Class<?> methodReturnType = method.getReturnType();

        String description = methodReturnType.getName() + " " + methodName + " (";
        for(Class<?> parameter:method.getParameterTypes())
        {
            description+=parameter.getName();
            description+=", ";
        }
        if(method.getParameterTypes().length > 0) {
            description = description.substring(0, description.length() - 2);
        }
        description += ")";

        return description;
    }

    public static final String generateHRMD(Member method) {
        String methodName = method.getName();
        String className = method.getDeclaringClass().getCanonicalName();
        Class<?> methodReturnType = ((Method) method).getReturnType();

        String description = methodReturnType.getName() + " " + methodName + " (";
        for(Class<?> parameter:((Method) method).getParameterTypes())
        {
            description+=parameter.getName();
            description+=", ";
        }
        if(((Method) method).getParameterTypes().length > 0) {
            description = description.substring(0, description.length() - 2);
        }
        description += ")";

        return description;
    }

    public static final ArrayList<String> getSerializableTypes()
    {
        /*
       Currently supported types that can be serialized and modified between android and normal Java
        */

        ArrayList<String> serializableTypes = new ArrayList<>();
        serializableTypes.add("String");
        serializableTypes.add("Integer");
        serializableTypes.add("Boolean");
        serializableTypes.add("Int");
        serializableTypes.add("Long");
        serializableTypes.add("Float");
        serializableTypes.add("Double");
        serializableTypes.add("Short");

        return serializableTypes;
    }

    public static final Object getReconstructedObject (String type, Object ob)
    {
        Object object = new Object();
        switch (type) {
            case "String":
                object = ob.toString();
                break;
            case "Character":
                object = ob.toString().charAt(0);
                break;
            case "Byte":
                object = Byte.parseByte(ob.toString());
                break;
            case "Integer":
                object = Integer.parseInt(ob.toString());
                break;
            case "Long":
                object = Long.parseLong(ob.toString());
                break;
            case "Float":
                object = Float.valueOf(ob.toString());
                break;
            case "Double":
                object = Double.valueOf(ob.toString());
                break;
            case "Boolean":
                object = Boolean.valueOf(ob.toString());
                break;
            default:
                Log.v("diorand", "unknown type, strange this should not happen");
                break;
        }

        return object;
    }

    public static String byteArrayToHexString(byte[] byteArray)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<byteArray.length; i++)
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        return hexString.toString();
    }

}
