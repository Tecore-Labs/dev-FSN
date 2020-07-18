package com.cpms.enumeration;

public enum CommenterType {
    USER(1),
    COMPANY(2);

    private final int value;

    CommenterType(int value){
        this.value=value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CommenterType{" +
                "value=" + value +
                '}';
    }
}

