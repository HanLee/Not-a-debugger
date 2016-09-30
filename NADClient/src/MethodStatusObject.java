
public class MethodStatusObject {
	private String className;
	private String methodName;
	private Boolean hooked;
	private Boolean isClassName;
	
	
	public MethodStatusObject()
	{
		this.className = "";
		this.methodName = "";
		this.hooked = false;
		this.isClassName = false;
	}
	
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Boolean getHooked() {
		return hooked;
	}
	public void setHooked(Boolean hooked) {
		this.hooked = hooked;
	}

	public Boolean getIsClassName() {
		return isClassName;
	}

	public void setIsClassName(Boolean isClassName) {
		this.isClassName = isClassName;
	}
	
	
	@Override
    public String toString(){
    	if(isClassName)
    		return this.className;
    	else if(!this.methodName.equals(""))
    		return this.methodName;
    	else
    		return "?";
    }
    
}
