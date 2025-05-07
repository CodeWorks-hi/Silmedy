// SdpObserverAdapter.java
package com.example.silmedy.videocall;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * SdpObserver 전부 빈 구현
 */
public class SdpAdapter implements SdpObserver {
    private final String tag;

    // ← 생성자 추가
    public SdpAdapter(String tag) {
        this.tag = tag;
    }

    @Override public void onCreateSuccess(SessionDescription sd) {
        Log.d(tag, "[" + tag + "] setRemoteDescription success");
    }
    @Override public void onSetSuccess() {}
    @Override public void onCreateFailure(String e) {
        Log.e(tag, "[" + tag + "] setRemoteDescription failure: " + e);
    }
    @Override public void onSetFailure(String e) {}
}