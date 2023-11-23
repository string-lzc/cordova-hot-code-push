package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when some error happened during the update download.
 */
public class ProgressEvent {

    public static final String EVENT_NAME = "chcp_fileDownloadProgress";
}
