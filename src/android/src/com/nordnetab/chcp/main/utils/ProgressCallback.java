package com.nordnetab.chcp.main.utils;
import java.util.Map;

public interface ProgressCallback {
    void onProgressUpdate(Map<String, Object> progress);
}