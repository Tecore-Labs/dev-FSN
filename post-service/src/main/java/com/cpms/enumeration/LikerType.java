package com.cpms.enumeration;

public enum LikerType {
    USER(1),
    COMPANY(2);

    private final int value;

    LikerType(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "LikerType{" +
                "value=" + value +
                '}';
    }
}
