package org.example;

public interface OnNewDataHandler<T> {
    void onNewData(Object sender, T data);
}
