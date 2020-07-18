package com.cpms.enumeration;

public enum MediaFileSize {

    ImageSize(1000000),
    VideoSize(5000000);

    private final int value;

    MediaFileSize(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }
}
