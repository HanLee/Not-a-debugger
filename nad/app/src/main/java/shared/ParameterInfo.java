package shared;

import java.io.Serializable;

/**
 * Created by Administrator on 3/16/2016.
 */
public class ParameterInfo implements Serializable {
    private static final long serialVersionUID = 6026158903532059192L;
    //Class<?> parameterClass;
    String parameterClass;
    Object parameterValue;
    Integer parameterIndex;
    Boolean isXStream;
    Boolean isReturnValue;

    /*
    public Class<?> getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(Class<?> parameterClass) {
        this.parameterClass = parameterClass;
    }
    */

    public String getParameterClass() {
        return parameterClass;
    }

    public void setParameterClass(String parameterClass) {
        this.parameterClass = parameterClass;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(Object parameterValue) {
        this.parameterValue = parameterValue;
    }

    public Integer getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(Integer parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    public Boolean getIsXStream() {
        return isXStream;
    }

    public void setIsXStream(Boolean isXStream) {
        this.isXStream = isXStream;
    }

    public Boolean getIsReturnValue() {
        return isReturnValue;
    }

    public void setIsReturnValue(Boolean isReturnValue) {
        this.isReturnValue = isReturnValue;
    }
}
