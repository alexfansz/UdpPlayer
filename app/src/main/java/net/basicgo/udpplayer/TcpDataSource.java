package net.basicgo.udpplayer;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public final class TcpDataSource extends BaseDataSource {

    /**
     * Thrown when an error is encountered when trying to read from a {@link TcpDataSource}.
     */
    public static final class AirMirroUdpDataSourceException extends IOException {

        public AirMirroUdpDataSourceException(IOException cause) {
            super(cause);
        }

    }

    /**
     * The default maximum datagram packet size, in bytes.
     */
    public static final int DEFAULT_MAX_PACKET_SIZE = 2*1024*1024;

    /** The default socket timeout, in milliseconds. */
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 8 * 1000;

    private final int socketTimeoutMillis;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private @Nullable
    Socket socket;
    private @Nullable
    Uri uri;

    private boolean opened;



    public TcpDataSource() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     */
    public TcpDataSource(int maxPacketSize) {
        this(maxPacketSize, DEFAULT_SOCKET_TIMEOUT_MILLIS);
    }

    /**
     * Constructs a new instance.
     *
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     * @param socketTimeoutMillis The socket timeout in milliseconds. A timeout of zero is interpreted
     *     as an infinite timeout.
     */
    public TcpDataSource(int maxPacketSize, int socketTimeoutMillis) {
        super(/* isNetwork= */ true);
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    /**
     * Constructs a new instance.
     *
     * @param listener An optional listener.
     * @deprecated Use {@link #TcpDataSource()} and {@link #addTransferListener(TransferListener)}.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public TcpDataSource(@Nullable TransferListener listener) {
        this(listener, DEFAULT_MAX_PACKET_SIZE);
    }

    /**
     * Constructs a new instance.
     *
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     * @deprecated Use {@link #TcpDataSource(int)} and {@link #addTransferListener(TransferListener)}.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public TcpDataSource(@Nullable TransferListener listener, int maxPacketSize) {
        this(listener, maxPacketSize, DEFAULT_SOCKET_TIMEOUT_MILLIS);
    }

    /**
     * Constructs a new instance.
     *
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     * @param socketTimeoutMillis The socket timeout in milliseconds. A timeout of zero is interpreted
     *     as an infinite timeout.
     * @deprecated Use {@link #TcpDataSource(int, int)} and {@link
     *     #addTransferListener(TransferListener)}.
     */
    @Deprecated
    public TcpDataSource(
            @Nullable TransferListener listener, int maxPacketSize, int socketTimeoutMillis) {
        this(maxPacketSize, socketTimeoutMillis);
        if (listener != null) {
            addTransferListener(listener);
        }
    }

    @Override
    public long open(DataSpec dataSpec) throws TcpDataSource.AirMirroUdpDataSourceException {
        uri = dataSpec.uri;
        String host = uri.getHost();
        int port = uri.getPort();
        transferInitializing(dataSpec);

        Log.v("AirMirroUdpDataSource", "open:"+"uri->"+uri.toString()+","+host+":"+port);
        try {
            socket = new Socket(host,port);
            //socket = new Socket("192.168.2.108",55556);
            if(socket != null){
                //获取输出流、输入流
                mOutStream = socket.getOutputStream();
                mInStream = socket.getInputStream();
            }
        } catch (IOException e) {
            throw new TcpDataSource.AirMirroUdpDataSourceException(e);
        }

        try {
            socket.setSoTimeout(socketTimeoutMillis);
        } catch (SocketException e) {
            throw new TcpDataSource.AirMirroUdpDataSourceException(e);
        }

        opened = true;
        transferStarted(dataSpec);

        Log.v("AirMirroUdpDataSource", "open ed");
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws TcpDataSource.AirMirroUdpDataSourceException {
        if (readLength == 0) {
            return 0;
        }

        int bytesToRead = 0;

        try {
            //bytesToRead = mInStream.read(packetBuffer);
            bytesToRead = mInStream.read(buffer,offset,readLength);
            bytesTransferred(bytesToRead);
        } catch (IOException e) {
            throw new TcpDataSource.AirMirroUdpDataSourceException(e);
        }

       //Log.v("AirMirroUdpDataSource", "readLength:" +readLength+"bytesToRead:"+bytesToRead);
        //Log.v("AirMirroUdpDataSource", "readLength:" +readLength+"bytesToRead:"+bytesToRead+"["+new String(buffer)+"]");
        return bytesToRead;
    }

    @Override
    public @Nullable
    Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws TcpDataSource.AirMirroUdpDataSourceException{
        Log.v("AirMirroUdpDataSource", "close");
        uri = null;



        if (socket != null) {
            try {

                mOutStream.write("EOS".getBytes());
                mOutStream.flush();
                mOutStream.close();
                mInStream.close();
                socket.close();
            } catch (IOException e) {
                throw new TcpDataSource.AirMirroUdpDataSourceException(e);
            }
            socket = null;

        }


        if (opened) {
            opened = false;
            transferEnded();
        }
    }
}
