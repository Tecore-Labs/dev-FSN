package com.cpms.enumeration;

public enum Status {
    ACTIVE(1),
    DELETED(2);

    private final int value;

    Status(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Status{" +
                "value=" + value +
                '}';
    }
}
