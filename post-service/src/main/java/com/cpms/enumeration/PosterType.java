package com.cpms.enumeration;

public enum PosterType {
    USER(1),
    COMPANY(2);

    private final int value;

    PosterType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PosterType{" +
                "value=" + value +
                '}';
    }
}
