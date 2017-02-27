/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.cnbleu.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <b>Create Date:</b> 02/27/17<br>
 * <b>Author:</b> Gordon<br>
 * <b>Description:</b> <br>
 */
@SuppressWarnings("unused")
public class SerialPort {
    private static final String TAG = "SerialPort";

    /** 串口波特率定义 */
    public enum BAUDRATE {
        B0(0),
        B50(50),
        B75(75),
        B110(110),
        B134(134),
        B150(150),
        B200(200),
        B300(300),
        B600(600),
        B1200(1200),
        B1800(1800),
        B2400(2400),
        B4800(4800),
        B9600(9600),
        B19200(19200),
        B38400(38400),
        B57600(57600),
        B115200(115200),
        B230400(230400),
        B460800(460800),
        B500000(500000),
        B576000(576000),
        B921600(921600),
        B1000000(1000000),
        B1152000(1152000),
        B1500000(1500000),
        B2000000(2000000),
        B2500000(2500000),
        B3000000(3000000),
        B3500000(3500000),
        B4000000(4000000);

        int baudrate;

        BAUDRATE(int baudrate) {
            this.baudrate = baudrate;
        }

        int getBaudrate() {
            return this.baudrate;
        }

    }

    /** 串口停止位定义 */
    public enum STOPB {
        /** 1位停止位 */
        B1(1),
        /** 2位停止位 */
        B2(2);

        int stopBit;

        STOPB(int stopBit) {
            this.stopBit = stopBit;
        }

        public int getStopBit() {
            return this.stopBit;
        }

    }

    /** 串口数据位定义 */
    public enum DATAB {
        /** 5位数据位 */
        CS5(5),
        /** 6位数据位 */
        CS6(6),
        /** 7位数据位 */
        CS7(7),
        /** 8位数据位 */
        CS8(8);

        int dataBit;

        DATAB(int dataBit) {
            this.dataBit = dataBit;
        }

        public int getDataBit() {
            return this.dataBit;
        }
    }

    /** 串口校验位定义 */
    public enum PARITY {
        /** 无奇偶校验 */
        NONE(0),
        /** 奇校验 */
        ODD(1),
        /** 偶校验 */
        EVEN(2);

        int parity;

        PARITY(int parity) {
            this.parity = parity;
        }

        public int getParity() {
            return this.parity;
        }
    }

    /** 串口流控定义 */
    public enum FLOWCON {
        /** 不使用流控 */
        NONE(0),
        /** 硬件流控 */
        HARD(1),
        /** 软件流控 */
        SOFT(2);

        int flowCon;

        FLOWCON(int flowCon) {
            this.flowCon = flowCon;
        }

        public int getFlowCon() {
            return this.flowCon;
        }
    }


    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort() {
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * 打开串口设备
     *
     * @param device   设备文件
     * @param baudrate {@link BAUDRATE} 串口波特率
     * @param stopbit  {@link STOPB} 串口停止位
     * @param databit  {@link DATAB} 串口数据位
     * @param parity   {@link PARITY} 串口奇偶检验
     * @param flowCon  {@link FLOWCON} 串口流控
     *
     * @return true, 串口设备打开成功。false，打开失败
     */
    public boolean open(File device,
                        BAUDRATE baudrate,
                        STOPB stopbit,
                        DATAB databit,
                        PARITY parity,
                        FLOWCON flowCon) {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                             + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                    || !device.canWrite()) {
                    Log.e(TAG, "native open device error!");
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "native open device cause exception: " + e.getLocalizedMessage());
                return false;
            }
        }

        mFd = open(device.getAbsolutePath(),
                   baudrate.getBaudrate(),
                   stopbit.getStopBit(),
                   databit.getDataBit(),
                   parity.getParity(),
                   flowCon.getFlowCon());
        if (mFd == null) {
            Log.e(TAG, "native open returns null fd");
            return false;
        }

        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        return true;
    }

    // JNI
    private native static FileDescriptor open(String path,
                                              int baudrate,
                                              int stopbit,
                                              int databit,
                                              int parity,
                                              int flowCon);

    public native void close();

    static {
        System.loadLibrary("serialport");
    }
}