package com.horchat.horchat.listener;

import android.os.Bundle;

public interface SessionListener {
    void onStatusCheck();
    void onStatusMessage(Bundle args);
}
