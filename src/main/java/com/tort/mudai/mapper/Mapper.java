package com.tort.mudai.mapper;

import com.tort.mudai.AdapterEventListener;

public interface Mapper extends AdapterEventListener {
    String getPathTo(String location);
}
