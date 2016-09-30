package shared;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by IEUser on 8/29/2015.
 */
public class MethodInfo implements Serializable {

    private static final long serialVersionUID = 1300307475607848077L;
    public String  humanReadableDescription;
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
