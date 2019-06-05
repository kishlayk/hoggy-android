/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.my.kiki.service;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.assistant.embedded.v1alpha1.AudioInConfig;
import com.google.assistant.embedded.v1alpha1.AudioOutConfig;
import com.google.assistant.embedded.v1alpha1.ConverseConfig;
import com.google.assistant.embedded.v1alpha1.ConverseRequest;
import com.google.assistant.embedded.v1alpha1.ConverseResponse;
import com.google.assistant.embedded.v1alpha1.ConverseState;
import com.google.assistant.embedded.v1alpha1.EmbeddedAssistantGrpc;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.protobuf.ByteString;
import com.my.kiki.BuildConfig;
import com.my.kiki.R;
import com.my.kiki.db.MyDatabase;
import com.my.kiki.main.MainApplication;
import com.my.kiki.model.PairedDevices;
import com.my.kiki.notification.SpeechServiceNotification;
import com.my.kiki.receiver.ACLReceiver;
import com.my.kiki.utils.LogUtils;
import com.my.kiki.utils.Utils;
import com.my.kiki.voiceassistant.Credentials;

import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.auth.MoreCallCredentials;
import io.grpc.stub.StreamObserver;


public class SpeechService extends Service {

    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param text    The text.
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String text, boolean isFinal);

        void onSpeechResponsed(String text, boolean isFinal);

        void onVoiceRecordStart();

        void onVoiceRecordStop();

        void onRequestStart();

        void onCredentioalSuccess();

        void onError();

        void onConnecting();

        void onConnected();

        void restartSpeechService();

    }

    private static final String TAG = "SpeechService";

    public static final List<String> SCOPE =
            Collections.singletonList("https://www.googleapis.com/auth/assistant-sdk-prototype");
    private static final String HOSTNAME = "embeddedassistant.googleapis.com";
    private static final int PORT = 443;

    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private EmbeddedAssistantGrpc.EmbeddedAssistantStub mApi;
    private static Handler mHandler;
    private Utils.BluetoothState bluetoothState = Utils.BluetoothState.UNAVAILABLE;

    private int DEFAULT_VOLUME = 1000;
    //private static int mVolumePercentage = 100; // for volume command
    AudioManager.OnAudioFocusChangeListener afChangeListener;
    AudioTrack mAudioTrack;//audiotracker
    AudioManager audioManager;

    ACLReceiver receiver;

    private final int validSampleRates[] = new int[]{8000, 11025, 16000, 22050,
            32000, 37800, 44056, 44100, 47250, 48000, 50000, 50400, 88200,
            96000, 176400, 192000, 352800, 2822400, 5644800};

    private BluetoothDevice device;
    SpeechServiceNotification notification;


    private final StreamObserver<ConverseResponse> mResponseObserver
            = new StreamObserver<ConverseResponse>() {
        @Override
        public void onNext(ConverseResponse value) {

            if (!Utils.isBuildTypeNoBluetooth()) {
                if (bluetoothState != Utils.BluetoothState.AVAILABLE) {
                    return;
                }
            }
            switch (value.getConverseResponseCase()) {
                case EVENT_TYPE:
                    // Log.d(TAG, "converse response event: " + value.getEventType());
                    //playAudioSong=false;
                    break;
                case RESULT:
                    final String spokenRequestText = value.getResult().getSpokenRequestText();
                    final String spokenResponseText = value.getResult().getSpokenResponseText();

                    vConversationState = value.getResult().getConversationState();

                    if (!spokenRequestText.isEmpty()) {
                        Log.i(TAG, "assistant request text: " + spokenRequestText);

                        for (Listener listener : mListeners) {
                            listener.onSpeechRecognized(spokenRequestText, true);
                        }
                    }

                    if (value.getResult().getVolumePercentage() != 0) {
                        int mVolumePercentage = value.getResult().getVolumePercentage();
                        Log.i(TAG, "assistant volume changed: " + mVolumePercentage);
                        float newVolume = AudioTrack.getMaxVolume() * mVolumePercentage / 100.0f;
                        Log.d(TAG,"new vol "+newVolume);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mAudioTrack.setVolume(newVolume);
                        }
                    }

//                    float volume = (float) audioManager.
//                            getStreamVolume(AudioManager.STREAM_MUSIC);
//                    Log.e("xyz123",volume+" RESULT "+mAudioTrack.getMaxVolume()+" "+value.getResult().getVolumePercentage());

                    if (!spokenResponseText.isEmpty()) {
                        Log.i(TAG, "assistant response text: " + spokenResponseText);

                    }
                    break;
                case AUDIO_OUT:
                    byte[] data = value.getAudioOut().getAudioData().toByteArray();
                    final ByteBuffer audioData = ByteBuffer.wrap(data);
                    // Log.d(TAG, "converse audio size: " + audioData.remaining());

                    final byte[] finaldata = data;

//                        new Thread() {
//                            public void run() {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                    mAudioTrack.write(audioData, audioData.remaining(), AudioTrack.WRITE_BLOCKING);
//                                } else {
//                                    mAudioTrack.write(finaldata, 0, finaldata.length);
//                                }
//
//                                Log.i(TAG, "assistant response finished: " + mAudioTrack.getPlayState());
//
//                                for (Listener listener : mListeners) {
//                                    listener.onSpeechResponsed("", false);
//                                }
//
//                                if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
//                                    mAudioTrack.play();
//                            }
//                        }.start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAudioTrack.write(audioData, audioData.remaining(), AudioTrack.WRITE_BLOCKING);
                    } else {
                        mAudioTrack.write(data, 0, data.length);
                    }


//                    Log.i(TAG, "MOBILE DATA SPEED: " + Utils.getInstance(MainApplication.getGlobalContext()).getMobileDataSpeed() + " , CPU USAGE : " + Utils.getInstance(MainApplication.getGlobalContext()).readUsage() + " , HEAP SIZE : " + Utils.getInstance(MainApplication.getGlobalContext()).getHeapSize() + "mb");


                    break;
                case ERROR:
                    Log.e(TAG, "converse response error: " + value.getError());
	                Log.e("xyz123","converse response error:");
                    break;
                case CONVERSERESPONSE_NOT_SET:
                   // Log.d(TAG, "CONVERSERESPONSE_NOT_SET"+value.getEventType());
                    break;
            }

        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, t.toString());
            try{
                for (Listener listener : mListeners) {
                    listener.onSpeechResponsed("", true);
                    listener.restartSpeechService();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onCompleted() {

            Log.i(TAG, "assistant response finished: "+mAudioTrack.getPlayState());

            for (Listener listener : mListeners) {
                listener.onSpeechResponsed("", false);
            }

            if (mAudioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING) mAudioTrack.play();



        }
    };

    private StreamObserver<ConverseRequest> mRequestObserver;

    public static SpeechService from(IBinder binder) {
        return ((SpeechBinder) binder).getService();
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.i("Speech Service ","onCreate");

        receiver = new ACLReceiver(MainApplication.getGlobalContext(), new ACLReceiver.Listener() {
            @Override
            public void onConnected() {
                for (Listener listener : mListeners) {
                    listener.onVoiceRecordStart();
                }
            }

            @Override
            public void onDisconnected() {
                for (Listener listener : mListeners) {
                    listener.onVoiceRecordStop();
                }
            }
        });

        //bluetooth sco receiver
        this.registerReceiver(scoReceiver, new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

        //bluetooth a2dp filter
/*        IntentFilter a2dpfilter = new IntentFilter();
        a2dpfilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        a2dpfilter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        a2dpfilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        a2dpfilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        this.registerReceiver(a2dpHspReceiver,a2dpfilter);*/


        mHandler = new Handler();
        fetchAccessToken();

        int outputBufferSize = AudioTrack.getMinBufferSize(32000,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioTrack = new AudioTrack(audioManager.MODE_NORMAL, 16000,AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, outputBufferSize, AudioTrack.MODE_STREAM);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAudioTrack.setVolume(DEFAULT_VOLUME);
                audioManager.setStreamVolume(3, audioManager.getStreamMaxVolume(3), 0);
            }

            mAudioTrack.play();


            afChangeListener =  new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    Log.v("is_one afChangeListener",focusChange+"");
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {



                        // Permanent loss of audio focus
                        // Pause playback immediately
                   /*     mediaController.getTransportControls().pause();
                        // Wait 30 seconds before stopping playback
                        handler.postDelayed(delayedStopRunnable,
                                TimeUnit.SECONDS.toMillis(30))*/;
                        Log.v("is_one","0");

                        int result = audioManager.requestAudioFocus(afChangeListener,
                                AudioManager.STREAM_RING | AudioManager.STREAM_MUSIC | AudioManager.STREAM_ALARM| AudioManager.STREAM_VOICE_CALL,
                                // Request permanent focus.
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            // Start playback
                            Log.v("is_test_am2","playing...inresult");
                        }
                    }
                    else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        Log.v("is_one","1");
                        // Pause playback
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        Log.v("is_one","2");
                        // Lower the volume, keep playing
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        Log.v("is_one","3");
                        // Your app has been granted audio focus again
                        // Raise volume to normal, restart playback if necessary
                    }
                }
            };
           int result = audioManager.requestAudioFocus(afChangeListener,
                    AudioManager.STREAM_RING | AudioManager.STREAM_MUSIC | AudioManager.STREAM_ALARM| AudioManager.STREAM_VOICE_CALL,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);

           if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start playback
                Log.v("is_test_am","playing...inresult");
            }

           notification = new SpeechServiceNotification(this);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecognising();
        try {
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(scoReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private String getDefaultLanguageCode() {
        final Locale locale = Locale.getDefault();
        final StringBuilder language = new StringBuilder(locale.getLanguage());
        final String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            language.append("-");
            language.append(country);
        }
        return language.toString();
    }

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    public void stopAudio() {
        mAudioTrack.stop();
    }

    private void activateBluetoothSco() {

        if (!audioManager.isBluetoothScoAvailableOffCall()) {
            Log.e(TAG, "SCO ist not available, recording is not possible");
        //    Toast.makeText(this, "SCO ist not available, recording is not possible!", Toast.LENGTH_SHORT);
            return;
        }

        if (!audioManager.isBluetoothScoOn()) {
            Log.i(TAG, "Turning on Bluetooth again");
            audioManager.setSpeakerphoneOn(false);
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();
        }
    }


    /**
     * Starts recognizing speech audio.
     *
     * @param sampleRate The sample rate of the audio.
     */


    private ByteString vConversationState = null;

    public void startRecognizing(int sampleRate) {
        if (Utils.isBuildTypeNoBluetooth()) {
            audioManager.setSpeakerphoneOn(true);
        } else{
            activateBluetoothSco();
        }

        if (mApi == null) {
            Log.w(TAG, "API not ready. Ignoring the request.");
            return;
        }
        for (Listener listener : mListeners) {
            listener.onRequestStart();
        }

        mRequestObserver = mApi.converse(mResponseObserver);
        ConverseConfig.Builder converseConfigBuilder =ConverseConfig.newBuilder()
                .setAudioInConfig(AudioInConfig.newBuilder()
                        .setEncoding(AudioInConfig.Encoding.LINEAR16)
                        .setSampleRateHertz(sampleRate)
                        .build())
                .setAudioOutConfig(AudioOutConfig.newBuilder()
                        .setEncoding(AudioOutConfig.Encoding.LINEAR16)
                        .setSampleRateHertz(sampleRate)
//                        .setVolumePercentage(100)
                        .build());
        if (vConversationState != null) {
            converseConfigBuilder.setConverseState(
                    ConverseState.newBuilder()
                            .setConversationState(vConversationState)
                            .build());
        }
        mRequestObserver.onNext(ConverseRequest.newBuilder()
                .setConfig(converseConfigBuilder.build())
                .build());
    }


    /**
     * Recognizes the speech audio. This method should be called every time a chunk of byte buffer
     * is ready.
     *
     * @param data The audio data.
     * @param size The number of elements that are actually relevant in the {@code data}.
     */
    public void recognize(byte[] data, int size) {
        if (mRequestObserver == null) {

            return;
        }
        // Call the streaming recognition API
        mRequestObserver.onNext(ConverseRequest.newBuilder()
                .setAudioIn(ByteString.copyFrom(data, 0, size))
                .build());
    }

    /**
     * Finishes recognizing speech audio.
     */
    public void finishRecognizing() {
        if (mRequestObserver == null) {
            return;
        }
        mRequestObserver.onCompleted();
        mRequestObserver = null;
    }

    public void stopRecognising(){
        mHandler.removeCallbacks(mFetchAccessTokenRunnable);
        mHandler = null;
        Log.i("Speech Service ","Destroy");
        // Release the gRPC channel.
        if (mApi != null) {
            final ManagedChannel channel = (ManagedChannel) mApi.getChannel();
            if (channel != null && !channel.isShutdown()) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error shutting down the gRPC channel.", e);
                }
            }
            mApi = null;
        }
    }



    private class SpeechBinder extends Binder {
        SpeechService getService() {
            return SpeechService.this;
        }
    }

    private final Runnable mFetchAccessTokenRunnable = new Runnable() {
        @Override
        public void run() {
            fetchAccessToken();
        }
    };

    private void fetchAccessToken() {


        ManagedChannel channel = ManagedChannelBuilder.forTarget(HOSTNAME).build();
        try {
            mApi = EmbeddedAssistantGrpc.newStub(channel)
                    .withCallCredentials(MoreCallCredentials.from(
                            Credentials.fromResource(getApplicationContext(), R.raw.credentials)
                    ));
        } catch (IOException|JSONException e) {
            Log.e(TAG, "error creating assistant service:", e);
        }

        for (Listener listener : mListeners) {
            listener. onCredentioalSuccess();

        }

    }


    /**
     * Authenticates the gRPC channel using the specified {@link GoogleCredentials}.
     */
    private static class GoogleCredentialsInterceptor implements ClientInterceptor {

        private final com.google.auth.Credentials mCredentials;

        private Metadata mCached;

        private Map<String, List<String>> mLastMetadata;

        GoogleCredentialsInterceptor(com.google.auth.Credentials credentials) {
            mCredentials = credentials;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
		        final MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
		        final Channel next) {
            return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {
                @Override
                protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                        throws StatusException {
                    Metadata cachedSaved;
                    URI uri = serviceUri(next, method);
                    synchronized (this) {
                        Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata;
                            mCached = toHeaders(mLastMetadata);
                        }
                        cachedSaved = mCached;
                    }
                    headers.merge(cachedSaved);
                    delegate().start(responseListener, headers);
                }
            };
        }

        /**
         * Generate a JWT-specific service URI. The URI is simply an identifier with enough
         * information for a service to know that the JWT was intended for it. The URI will
         * commonly be verified with a simple string equality check.
         */
        private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
                throws StatusException {
            String authority = channel.authority();
            if (authority == null) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Channel has no authority")
                        .asException();
            }
            // Always use HTTPS, by definition.
            final String scheme = "https";
            final int defaultPort = 443;
            String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            URI uri;
            try {
                uri = new URI(scheme, authority, path, null, null);
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI for auth")
                        .withCause(e).asException();
            }
            // The default port must not be present. Alternative ports should be present.
            if (uri.getPort() == defaultPort) {
                uri = removePort(uri);
            }
            return uri;
        }

        private URI removePort(URI uri) throws StatusException {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                        uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI after removing port")
                        .withCause(e).asException();
            }
        }

        private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
            try {
                return mCredentials.getRequestMetadata(uri);
            } catch (IOException e) {
                throw Status.UNAUTHENTICATED.withCause(e).asException();
            }
        }

        private static Metadata toHeaders(Map<String, List<String>> metadata) {
            Metadata headers = new Metadata();
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    Metadata.Key<String> headerKey = Metadata.Key.of(
                            key, Metadata.ASCII_STRING_MARSHALLER);
                    for (String value : metadata.get(key)) {
                        headers.put(headerKey, value);
                    }
                }
            }
            return headers;
        }

    }


    private final BroadcastReceiver scoReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {

            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            switch (state) {
                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is connected");
                    handleBluetoothStateChange(Utils.BluetoothState.AVAILABLE);
                    bluetoothState= Utils.BluetoothState.AVAILABLE;
                    try{
                        notification.buildNotification("Online");
                        for (Listener listener : mListeners) {
                            listener.onConnected();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                    Log.i(TAG, "Bluetooth HFP Headset is connecting");
                    try{
                        for (Listener listener : mListeners) {
                            listener.onConnecting();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    handleBluetoothStateChange(Utils.BluetoothState.UNAVAILABLE);
                    break;
                case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                    Log.i(TAG, "Bluetooth HFP Headset is disconnected");
                    notification.removeNotification(SpeechServiceNotification.SPEECH_NOTIFICATION_ID);
                    handleBluetoothStateChange(Utils.BluetoothState.UNAVAILABLE);
                    try{
                        for (Listener listener : mListeners) {
                            listener.onVoiceRecordStop();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                case AudioManager.SCO_AUDIO_STATE_ERROR:
                    Log.i(TAG, "Bluetooth HFP Headset is in error state");
                    handleBluetoothStateChange(Utils.BluetoothState.UNAVAILABLE);
                    break;
            }
        }

        private void handleBluetoothStateChange(Utils.BluetoothState state) {

            if (bluetoothState == state) {
                return;
            }
            bluetoothState = state;
            Log.i(TAG, "Bluetooth state changed to:" + state);

        }
    };

    private final BroadcastReceiver a2dpHspReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "A2DP status : " + intent.getAction());
        }
    };
}
