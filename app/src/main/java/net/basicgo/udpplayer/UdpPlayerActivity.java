package net.basicgo.udpplayer;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;


import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.UdpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.google.android.exoplayer2.util.Util;



import android.app.Activity;
import android.os.ParcelFileDescriptor;

public final class UdpPlayerActivity extends Activity {
    private PlayerView localPlayerView;

    SimpleExoPlayer mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /*WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock wifiLock  = manager.createMulticastLock("localWifi");
        wifiLock.acquire();*/

        localPlayerView = new PlayerView(this);
        setContentView(localPlayerView);
        localPlayerView.requestFocus();

        initExoPlayer();
    }

    private void initExoPlayer() {
        //Uri udpUri = Uri.parse("udp://255.255.255.255:12345");
        //Uri udpUri = Uri.parse("udp://127.0.0.1:12345");
        Uri udpUri = Uri.parse("tcp://192.168.43.1:12345");

        /*DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(0,DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,0,0)
                .createDefaultLoadControl();*/

        AirMirrorLoadControl loadControl = new AirMirrorLoadControl.Builder()
                //.setBufferDurationsMs(0,DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,0,0)
                .createDefaultLoadControl();

        mPlayer= new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();
        //mPlayer= new SimpleExoPlayer.Builder(this,new DefaultRenderersFactory(this).setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)).build();
        mPlayer.addAnalyticsListener(new EventLogger(null));
        localPlayerView.setPlayer(mPlayer);

        //java 1.8 lamda code, pretty
        DataSource.Factory factory = () -> new TcpDataSource(188*1024*4, 5000);
        ExtractorsFactory tsExtractorFactory = () -> new TsExtractor[]{new TsExtractor(TsExtractor.MODE_MULTI_PMT,
                new TimestampAdjuster(0), new DefaultTsPayloadReaderFactory())};

        //ExtractorsFactory mp4ExtractorFactory = () -> new Mp4Extractor[]{new Mp4Extractor()};

        /*  alex code, ugly
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "net.basicgo.udpplayer"));
        ExtractorsFactory extractorsFactory = new UdpPlayerTsExtractorsFactory();
// This is the MediaSource representing the media to be played.
         */

        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(factory,tsExtractorFactory)
                        .createMediaSource(udpUri);

// Prepare the player with the source.
        mPlayer.prepare(videoSource);
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (localPlayerView != null) {
                localPlayerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void releasePlayer() {
        if (mPlayer != null) {

            mPlayer.release();
            mPlayer = null;
        }
    }
}
