package com.fastjien.sunny.net;

public interface ResponseListener<T> {
    void onResponse(T response);
}
