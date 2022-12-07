package com.genexus.gxdynamiccall;

public class GXDynCallMethodConf {
    private boolean isStatic;
    private String methodName;
    
    public GXDynCallMethodConf(){
        isStatic=false;
        methodName="execute";
    }

    public void setIsStatic(boolean is){
        isStatic=is;
    }
    public boolean getIsStatic(){
        return isStatic;
    }

    public void setMethodName(String mn){
        methodName=mn;
    }

    public String getMethodName(){
        return methodName;
    }
  
}
