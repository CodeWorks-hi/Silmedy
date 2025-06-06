package com.example.silmedy.videocall;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class WebRTCManager implements FirebaseSignalingClient.Callback {
    private static final String TAG = "WebRTCManager";

    private final Activity activity;
    private final EglBase eglBase;
    private final SurfaceViewRenderer remoteView;
    private final SurfaceViewRenderer localView;
    private VideoCapturer videoCapturer;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private FirebaseSignalingClient signalingClient;
    private String roomId;
    private DataChannel localDataChannel;
    private boolean isCaller = false;
    private boolean disposed = false;


    private TextView subtitleTextView;  // 액티비티에서 뷰를 전달받아 필드로 선언   // 남호가 추가

    public void setSubtitleTextView(TextView subtitleTextView) {// 남호가 추가
        this.subtitleTextView = subtitleTextView;               // 남호가 추가
    }                                                            // 남호가 추가

    public WebRTCManager(Activity activity, EglBase eglBase,
                         SurfaceViewRenderer remoteView,
                         SurfaceViewRenderer localView) {
        this.activity = activity;
        this.eglBase = eglBase;
        this.remoteView = remoteView;
        this.localView = localView;
        Log.d(TAG, "Constructor called");
        initFactory();
        initPeerConnection();
        initLocalMedia();
    }

    public void setRoomId(String roomId, boolean isCaller) {
        this.roomId = roomId;
        this.isCaller = isCaller;
        Log.d(TAG, "setRoomId() called. roomId=" + roomId);
        if (signalingClient == null) {
            signalingClient = new FirebaseSignalingClient(roomId, this);
            Log.d(TAG, "FirebaseSignalingClient initialized");
        }
    }

    private void initFactory() {
        Log.d(TAG, "initFactory() start");
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(activity.getApplicationContext())
                        .createInitializationOptions());
        AudioDeviceModule adm = JavaAudioDeviceModule.builder(activity.getApplicationContext())
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule();
        factory = PeerConnectionFactory.builder()
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(
                        eglBase.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(
                        eglBase.getEglBaseContext()))
                .createPeerConnectionFactory();
        Log.d(TAG, "PeerConnectionFactory created");
    }

    private void initPeerConnection() {
        Log.d(TAG, "initPeerConnection() start");
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer());
// TURN over UDP/TCP
        iceServers.add(PeerConnection.IceServer
                .builder("turn:13.209.17.4:3478?transport=udp")
                .setUsername("testuser").setPassword("testpass")
                .createIceServer());
        iceServers.add(PeerConnection.IceServer
                .builder("turn:13.209.17.4:3478?transport=tcp")
                .setUsername("testuser").setPassword("testpass")
                .createIceServer());
// TLS (5349), HTTPS (443) failover
        iceServers.add(PeerConnection.IceServer
                .builder("turn:13.209.17.4:5349?transport=tcp")
                .setUsername("testuser").setPassword("testpass")
                .createIceServer());
        iceServers.add(PeerConnection.IceServer
                .builder("turn:13.209.17.4:443?transport=tcp")
                .setUsername("testuser").setPassword("testpass")
                .createIceServer());
        PeerConnection.RTCConfiguration cfg = new PeerConnection.RTCConfiguration(iceServers);
        cfg.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        
        peerConnection = factory.createPeerConnection(cfg,
                new PeerConnectionAdapter() {
                    @Override
                    public void onIceCandidate(IceCandidate candidate) {
                        Log.d(TAG, "onIceCandidate() candidate=" + candidate.sdp);
                        signalingClient.sendIceCandidate(new SignalingData(candidate));
                    }

                    @Override
                    public void onTrack(RtpTransceiver transceiver) {
                        MediaStreamTrack track = transceiver.getReceiver().track();
                        if (track instanceof VideoTrack) {
                            VideoTrack vt = (VideoTrack) track;
                            remoteView.post(() -> vt.addSink(remoteView));
                        }
                    }

                    @Override
                    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
                        Log.d(TAG, "onIceConnectionChange() state=" + newState);
                        switch (newState) {
                            case CONNECTED:
                            case COMPLETED:
                                // Connection established; you can update UI if needed
                                break;
                            case DISCONNECTED:
                            case FAILED:
                                Log.d(TAG, "onIceConnectionChange DISCONNECTED/FAILED – 대기 상태 유지");
                                break;
                            default:
                                // Other states: NEW, CHECKING, CLOSED
                                break;
                        }
                    }

                    @Override
                    public void onDataChannel(DataChannel channel) {
                        Log.d(TAG, "onDataChannel() label=" + channel.label() + " state=" + channel.state());
                        if (!"subtitles".equals(channel.label())) return;
                        channel.registerObserver(new DataChannel.Observer() {
                            @Override
                            public void onMessage(DataChannel.Buffer buffer) {
                                Log.d(TAG, "▶ onMessage() fired");
                                ByteBuffer data = buffer.data;
                                byte[] bytes = new byte[data.capacity()];
                                data.get(bytes);
                                final String subtitle = new String(bytes, Charset.forName("UTF-8"));
                                Log.d(TAG, "Received subtitle: " + subtitle);
                                // 뷰가 세팅되어 있으면 화면에 띄우고, 아니면 경고만 로깅
                                if (subtitleTextView != null) {
                                    activity.runOnUiThread(() ->
                                            subtitleTextView.setText(subtitle)
                                    );
                                } else {
                                    Log.w(TAG, "subtitleTextView is null — cannot show subtitle");
                                }
                            }

                            @Override
                            public void onStateChange() {
                                Log.d(TAG, "▶ subtitles DC state → " + channel.state());
                            }

                            @Override
                            public void onBufferedAmountChange(long previousAmount) {
                            }
                        });
                    }
                }
        );

        if (peerConnection == null) {
            Log.e(TAG, "Failed to create PeerConnection");
        } else {
            Log.d(TAG, "PeerConnection created");
        }
    }

    public void closeDataChannel() {
        if (localDataChannel != null) {
            localDataChannel.close();
            localDataChannel = null;
        }
    }


    private void initLocalMedia() {
        Log.d(TAG, "initLocalMedia() start");
        videoCapturer = createCameraCapturer();
        SurfaceTextureHelper helper = SurfaceTextureHelper.create(
                "CaptureThread", eglBase.getEglBaseContext());
        VideoSource vs = factory.createVideoSource(false);
        videoCapturer.initialize(helper, activity.getApplicationContext(), vs.getCapturerObserver());
        try {
            videoCapturer.startCapture(640, 480, 30);
        } catch (Exception e) {
            Log.e(TAG, "startCapture() failed", e);
        }
        VideoTrack localVideo = factory.createVideoTrack("ARDAMSv0", vs);
        localVideo.addSink(localView);

        AudioSource as = factory.createAudioSource(new MediaConstraints());
        AudioTrack localAudio = factory.createAudioTrack("ARDAMSa0", as);
        peerConnection.addTrack(localVideo);
        peerConnection.addTrack(localAudio);
        Log.d(TAG, "Local tracks added");
    }

    private VideoCapturer createCameraCapturer() {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);
        for (String name : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(name)) {
                VideoCapturer cap = enumerator.createCapturer(name, null);
                if (cap != null) return cap;
            }
        }
        for (String name : enumerator.getDeviceNames()) {
            VideoCapturer cap = enumerator.createCapturer(name, null);
            if (cap != null) return cap;
        }
        throw new RuntimeException("No camera available");
    }

    /**
     * Caller 역할: Offer 생성 후 전송
     */
    public void createOfferAndSend(String roomId) {
        this.roomId = roomId;
        Log.d(TAG, "▶ createOfferAndSend() — caller? " + isCaller);

        // 신규 시그널링 클라이언트 초기화 (이미 init 되었다면 중복 무시)
        if (signalingClient == null) {
            signalingClient = new FirebaseSignalingClient(roomId, this);
        }
        // ▶ caller 역할로 Offer 만들기 직전에만 채널 생성
        DataChannel.Init init = new DataChannel.Init();
        localDataChannel = peerConnection.createDataChannel("subtitles", init);
        Log.d(TAG, "▶ Caller created subtitles DC, state=" + localDataChannel.state());

        peerConnection.createOffer(new SdpAdapter("createOffer") {
            @Override
            public void onCreateSuccess(SessionDescription offer) {
                peerConnection.setLocalDescription(
                        new SdpAdapter("setLocalOffer"), offer);
                signalingClient.sendOffer(offer.description);
            }
        }, new MediaConstraints());
    }

    /**
     * Callee 역할: Answer 생성 후 전송
     */
    public void createAnswerAndSend(String roomId) {
        this.roomId = roomId;
        if (signalingClient == null) {
            signalingClient = new FirebaseSignalingClient(roomId, this);
        }
        // ▶ callee 역할로 Answer 만들기 직전에만 채널 생성
        DataChannel.Init init = new DataChannel.Init();
        localDataChannel = peerConnection.createDataChannel("subtitles", init);
        Log.d(TAG, "▶ Callee created subtitles DC, state=" + localDataChannel.state());

        peerConnection.createAnswer(new SdpAdapter("createAnswer") {
            @Override
            public void onCreateSuccess(SessionDescription answer) {
                peerConnection.setLocalDescription(
                        new SdpAdapter("setLocalAnswer"), answer);
                signalingClient.sendAnswer(answer.description);
            }
        }, new MediaConstraints());
    }

    // ────────────────────────────────────────────────────────────────────────────
    // FirebaseSignalingClient.Callback 구현
    // ────────────────────────────────────────────────────────────────────────────
    @Override
    public void onOfferReceived(String sdp) {
        SessionDescription offerDesc =
                new SessionDescription(SessionDescription.Type.OFFER, sdp);
        peerConnection.setRemoteDescription(new SdpAdapter("setRemoteOffer") {
            @Override
            public void onSetSuccess() {
                Log.d(TAG, "Remote offer set successfully, creating answer");
                // Offer 받은 후 Answer 생성 및 전송
                createAnswerAndSend(roomId);
            }
        }, offerDesc);
    }

    @Override
    public void onAnswerReceived(String sdp) {
        SessionDescription answerDesc =
                new SessionDescription(SessionDescription.Type.ANSWER, sdp);
        peerConnection.setRemoteDescription(
                new SdpAdapter("setRemoteAnswer"), answerDesc);
    }

    @Override
    public void onIceCandidateReceived(IceCandidate candidate) {
        peerConnection.addIceCandidate(candidate);
    }

    /**
     * 연결 종료 시 리소스 해제
     */
    public void dispose() {
        if (disposed) return;
        disposed = true;
        Log.d(TAG, "dispose() called");

        // 1) 카메라 캡처러 정리
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Log.w(TAG, "stopCapture 실패", e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }
        if (signalingClient != null) signalingClient.stop();
        if (peerConnection != null) peerConnection.dispose();
        if (factory != null) factory.dispose();
    }
}