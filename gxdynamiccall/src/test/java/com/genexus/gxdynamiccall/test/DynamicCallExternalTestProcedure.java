package com.genexus.gxdynamiccall.test;

public class DynamicCallExternalTestProcedure {
    private int multiplier = 1;
    public DynamicCallExternalTestProcedure(int multiplier){
        this.multiplier=multiplier;
    }

    public String calculateAsString(int a, int b) {
        return String.valueOf((a + b )* multiplier);
    }

    public static String sumAsString(int a, int b){
        return String.valueOf((a + b ));
    }
    
}
