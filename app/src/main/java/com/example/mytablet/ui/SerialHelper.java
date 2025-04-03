package com.example.mytablet.ui;


import java.io.File;

import android.serialport.SerialPort;
import android.util.Log;

import com.example.mytablet.ui.model.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialHelper {
    private static final String TAG = "SerialHelper";
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isOpen = false;

    /**
     * 打开串口
     * @param devicePath 串口设备路径（如 "/dev/ttyS4"）
     * @param baudRate 波特率（如 9600）
     * @return 是否打开成功
     */
    public boolean openSerialPort(String devicePath, int baudRate) {
        try {
            File device = new File(devicePath);
            serialPort = new SerialPort(device, baudRate);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            isOpen = true;
            Log.d(TAG, "✅ 串口打开成功: " + devicePath);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "❌ 串口打开失败", e);
            Utils.showToast("串口打开失败,请联系开发者");
            return false;
        }
    }

    /**
     * 发送数据
     * @param data 需要发送的字节数据
     */
    public void sendData(byte[] data) {
        if (!isOpen || outputStream == null) {
            Log.e(TAG, "❌ 串口未打开，无法发送数据！");
            return;
        }
        try {
            outputStream.write(data);
            outputStream.flush();
            Log.d(TAG, "✅ 发送数据: " + bytesToHex(data));
        } catch (IOException e) {
            Log.e(TAG, "❌ 发送数据失败", e);
        }
    }

    /**
     * 读取数据
     * @return 读取到的数据
     */
    public byte[] readData() {
        if (!isOpen || inputStream == null) {
            Log.e(TAG, "❌ 串口未打开，无法读取数据！");
            return null;
        }
        try {
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                byte[] receivedData = new byte[bytesRead];
                System.arraycopy(buffer, 0, receivedData, 0, bytesRead);
                Log.d(TAG, "📥 读取到数据: " + bytesToHex(receivedData));
                return receivedData;
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ 读取数据失败", e);
        }
        return null;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (serialPort != null) serialPort.close();
            isOpen = false;
            Log.d(TAG, "✅ 串口已关闭");
        } catch (IOException e) {
            Log.e(TAG, "❌ 关闭串口失败", e);
        }
    }

    /**
     * 获取串口状态
     * @return 串口是否打开
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 获取输入流
     * @return 输入流
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 获取输出流
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }


    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 输入字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X ", b));
        }
        return hexString.toString().trim();
    }
}





