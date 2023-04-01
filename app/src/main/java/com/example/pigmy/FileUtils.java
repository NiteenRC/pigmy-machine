package com.example.pigmy;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    protected static String createFile(String fileName) {
        File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadPath, fileName);
        return file.getAbsolutePath();
    }

    protected boolean writeToFile(List<String> stringList) {
        boolean writeResult;
        try {
            File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadPath, "receive.txt");
            FileOutputStream fOut = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOut));
            for (int i = 0; i < stringList.size(); i++) {
                bw.write(stringList.get(i));
                bw.newLine();
            }
            bw.close();
            fOut.close();
            writeResult = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writeResult;
    }

    protected List<String> readDataExternal() {
        List<String> dataList = new ArrayList<>();
        try {
            File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            FileInputStream fis = new FileInputStream(downloadPath + "/send.txt");
            Scanner sc = new Scanner(fis);
            while (sc.hasNextLine()) {
                dataList.add(sc.nextLine());
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

}
