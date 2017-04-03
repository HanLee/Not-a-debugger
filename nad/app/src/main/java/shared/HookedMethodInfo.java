package shared;

import java.io.Serializable;

/**
 * Created by Administrator on 2/19/2017.
 */
public class HookedMethodInfo implements Serializable{

    private String methodName;
    private HookStates hookType;

    public HookedMethodInfo(String methodName, HookStates hookType) {
        this.methodName = methodName;
        this.hookType = hookType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public HookStates getHookType() {
        return hookType;
    }

    public void setHookType(HookStates hookType) {
        this.hookType = hookType;
    }
}
