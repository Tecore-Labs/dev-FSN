package com.cpms.enumeration;

public enum MediaFileType {

    ImageType(1),
    VideoType(2);

    private final int value;

    MediaFileType(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }

}
