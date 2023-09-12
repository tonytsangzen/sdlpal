/* -*- mode: c; tab-width: 4; c-basic-offset: 4; c-file-style: "linux" -*- */
//
// Copyright (c) 2009-2011, Wei Mingzhi <whistler_wmz@users.sf.net>.
// Copyright (c) 2011-2022, SDLPAL development team.
// All rights reserved.
//
// This file is part of SDLPAL.
//
// SDLPAL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License version 3
// as published by the Free Software Foundation.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package com.sdlpal.sdlpal;

import static com.sdlpal.sdlpal.AssetsZipUtils.UnZipAssetsFolder;

import org.libsdl.app.SDLActivity;

import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.*;
import android.media.*;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.*;

public class PalActivity extends SDLActivity {

    static {
        System.loadLibrary("SDL2");
        System.loadLibrary("main");
    }

    private static final String TAG = "sdlpal-debug";
    private static MediaPlayer mediaPlayer;

    public static native void setScreenSize(int width, int height);

    public static boolean crashed = false;
    static String audioFile;
    private static MediaPlayer JNI_mediaplayer_load(String filename){
        Log.v(TAG, "loading midi:" + filename);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(mSingleton.getApplicationContext(), Uri.fromFile(new File(filename)));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch(IOException e) {
            Log.e(TAG, filename + " not available for playing, check");
        }
        return mediaPlayer;
    }

    public boolean copyAssetsFile(Context m, String name, String path) {
        Log.d("", "copy [" + name + "] to [" + path + "]");
        try {
            InputStream fis = m.getAssets().open(name);
            byte fw[] = new byte[(int) fis.available()];
            fis.read(fw);
            fis.close();

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(fw);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            Log.d("", "Copy file error:" + e.toString());
            e.printStackTrace();
        }
        return false;
    }
    private void PrepareFile(){
        String cachePath = getApplicationContext().getExternalCacheDir().getPath();
        Log.d("SDL", cachePath.toString());
        String dataPath = cachePath;
        String sdlpalPath = cachePath;

        File extFolder = new File(sdlpalPath);
        if( !extFolder.exists()) {
            extFolder.mkdirs();
        }

        setAppPath(sdlpalPath, dataPath, cachePath);

        File runningFile = new File(cachePath + "/running");
        if(runningFile.exists()){
            runningFile.delete();
        }

        copyAssetsFile(this, "sdlpal.cfg", dataPath + "/sdlpal.cfg");
        File dataFile = new File(dataPath + "/data.zip");
        if(!dataFile.exists()) {
            copyAssetsFile(this, "data.zip", dataPath + "/data.zip");
            try {
                UnZipAssetsFolder(this, Uri.fromFile(dataFile), dataPath);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrepareFile();
        loadConfigFile();
        setConfigBoolean("LaunchSetting", false);
        saveConfigFile();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        setScreenSize(metrics.widthPixels, metrics.heightPixels);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (!this.isFinishing() && mediaPlayer != null) {
            mediaPlayer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        super.onResume();
    }
    public static native boolean setConfigBoolean(String item, boolean value);
    public static native boolean loadConfigFile();
    public static native boolean saveConfigFile();
    public static native void setAppPath(String basepath, String datapath, String cachepath);
}
