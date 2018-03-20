package com.android.mikechenmj.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.File;
import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer mMediaPlayer;
    private IBinder mCallback;

    public static final int TRANSACTION_START_MUSIC = IBinder.FIRST_CALL_TRANSACTION + 1;
    public static final int TRANSACTION_PAUSE_MUSIC = IBinder.FIRST_CALL_TRANSACTION + 2;
    public static final int TRANSACTION_SWITCH_MUSIC = IBinder.FIRST_CALL_TRANSACTION + 3;
    public static final int TRANSACTION_SET_CALLBACK = IBinder.FIRST_CALL_TRANSACTION + 4;

    public static final String DESCRIPTOR = "CustomMusicService";

    private Binder mMusicBinder = new Binder() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_START_MUSIC:
                    data.enforceInterface(DESCRIPTOR);
                    startMusic();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_PAUSE_MUSIC:
                    data.enforceInterface(DESCRIPTOR);
                    pauseMusic();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_SWITCH_MUSIC:
                    data.enforceInterface(DESCRIPTOR);
                    switchMusic();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_SET_CALLBACK:
                    data.enforceInterface(DESCRIPTOR);

                    setCallback(data.readStrongBinder());
                    return true;
                default:
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        initMediaPlayer(intent.getStringExtra("data"));
        return mMusicBinder;
    }

    private void initMediaPlayer(final String path) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            File file = new File(path);
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.setDataSource(file.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    transactUpdate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMusic() {
        mMediaPlayer.start();
    }

    private void pauseMusic() {
        mMediaPlayer.pause();
    }

    private void switchMusic() {
        String path = transactGetMusicPath();
        initMediaPlayer(path);
        mMediaPlayer.start();
    }

    private void setCallback(IBinder callback) {
        mCallback = callback;
    }

    private String transactGetMusicPath() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = "";
        try {
            data.writeInterfaceToken(MusicListActivity.DESCRIPTOR);
            mCallback.transact(MusicListActivity.TRANSACTION_GET_MUSIC_PATH, data, reply, 0);
            reply.readException();
            result = reply.readString();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            reply.recycle();
            data.recycle();
        }
        return result;
    }

    private void transactUpdate() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(MusicListActivity.DESCRIPTOR);
            mCallback.transact(MusicListActivity.TRANSACTION_UPDATE, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
