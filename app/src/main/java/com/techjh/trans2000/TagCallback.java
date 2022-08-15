package com.techjh.trans2000;


public interface TagCallback {
    void tagCallback(ReadTag var1);

    int tagCallbackFailed(int var1);

    void ReadOver();
}
