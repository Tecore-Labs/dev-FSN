package com.cpms.enumeration;

public enum PostPrivacy {
    PUBLIC(1),
    NETWORK(2),
    ONLY_ME(0);

    private final int value;

    PostPrivacy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PostPrivacy{" +
                "vlaue=" + value +
                '}';
    }
}
