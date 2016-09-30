package shared;

import android.util.Log;

import com.nad.Util;
import com.thoughtworks.xstream.XStream;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by IEUser on 8/29/2015.
 */
public class MethodInfo implements Serializable {

    private static final long serialVersionUID = 1300307475607848077L;
    public String humanReadableDescription;
    String methodName;
    String className;
    /*
    ArrayList methodParameterTypes;
    ArrayList methodParameterValues;
    ArrayList hookableParamsIndex;
    */
    //Class<?> methodReturnType;
    String methodReturnType;
    ArrayList<ParameterInfo> methodParameters;
    //transient Method mMethod;

    public MethodInfo(Method method)
    {
        //mMethod = method;
        //methodReturnType = method.getReturnType();
        methodReturnType = method.getReturnType().getName();
        methodName = method.getName();
        humanReadableDescription = createHumanReadableDescription(method);
    }

    public MethodInfo(Method method, XC_MethodHook.MethodHookParam param)
    {
        methodReturnType = method.getReturnType().getName();
        methodName = method.getName();
        humanReadableDescription = createHumanReadableDescription(method);

        ArrayList<ParameterInfo> methodParameters = new ArrayList<>();

        for (int i = 0; i < param.args.length; i++) {
            Log.v("diorand", "class type " + param.args[i].getClass().getSimpleName());
            Log.v("diorand", "paramv: " + param.args[i].toString());

            ParameterInfo pi = new ParameterInfo();

            if (Util.getSerializableTypes().contains(param.args[i].getClass().getSimpleName())) {
                pi.setIsXStream(false);
                //pi.setParameterClass(param.args[i].getClass());
                pi.setParameterClass(param.args[i].getClass().getSimpleName());
                pi.setParameterIndex(i);
                pi.setParameterValue(param.args[i]);
                pi.setIsReturnValue(false);
                methodParameters.add(pi);
            }
            else
            {
                try {
                    XStream xstream = new XStream();
                    String xml = xstream.toXML(param.args[i]);
                    Log.v("nad", "xstream-test:" + xml);
                    pi.setIsXStream(true);
                    pi.setParameterClass(param.args[i].getClass().getSimpleName());
                    pi.setParameterIndex(i);
                    pi.setParameterValue(xml);
                    pi.setIsReturnValue(false);
                    methodParameters.add(pi);
                } catch (StackOverflowError t)
                {
                    Log.e("nad", "failed at xstream, oen of the objects is way too big");
                }
            }

        }

        this.setMethodParameters(methodParameters);
        this.setClassName(method.getDeclaringClass().getCanonicalName());
    }

    public MethodInfo(Method method, XC_MethodHook.MethodHookParam param, Boolean isReturnValue)
    {
        ParameterInfo pi = new ParameterInfo();
        ArrayList<ParameterInfo> methodParameter = new ArrayList<>();
        pi.setIsReturnValue(true);
        pi.setParameterIndex(0);

        if(Util.getSerializableTypes().contains(param.getResult().getClass().getSimpleName())) {

            pi.setParameterValue(param.getResult());
            pi.setParameterClass(param.getResult().getClass().getSimpleName());
            pi.setIsXStream(false);
            methodParameter.add(pi);
        }
        else
        {
            try {
                XStream xstream = new XStream();
                String xml = xstream.toXML(param.getResult());
                Log.v("diorand", "xstream-test:" + xml);
                pi.setIsXStream(true);
                pi.setParameterClass(param.getResult().getClass().getSimpleName());
                pi.setParameterValue(xml);
                methodParameter.add(pi);
            } catch (Exception ex)
            {
                Log.e("diorand", "failed at xstream");
            }
        }

        this.setMethodParameters(methodParameter);
        this.setClassName(method.getDeclaringClass().getCanonicalName());
    }

    private String createHumanReadableDescription(Method method)
    {
        String description = methodReturnType + " " + methodName + " (";
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

    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /*
    public Class<?> getMethodReturnType() {
        return methodReturnType;
    }

    public void setMethodReturnType(Class<?> methodReturnType) {
        this.methodReturnType = methodReturnType;
    }
    */

    /*
    public Method getmMethod() {
        return mMethod;
    }

    public void setmMethod(Method mMethod) {
        this.mMethod = mMethod;
    }
    */

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
/*
    public ArrayList getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public void setMethodParameterTypes(ArrayList methodParameterTypes) {
        this.methodParameterTypes = methodParameterTypes;
    }


    public ArrayList getMethodParameterValues() {
        return methodParameterValues;
    }

    public void setMethodParameterValues(ArrayList methodParameterValues) {
        this.methodParameterValues = methodParameterValues;
    }

    public ArrayList getHookableParamsIndex() {
        return hookableParamsIndex;
    }

    public void setHookableParamsIndex(ArrayList hookableParamsIndex) {
        this.hookableParamsIndex = hookableParamsIndex;
    }
    */

    public ArrayList<ParameterInfo> getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(ArrayList<ParameterInfo> methodParameters) {
        this.methodParameters = methodParameters;
    }
}
