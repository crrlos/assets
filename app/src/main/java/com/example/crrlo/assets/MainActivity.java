package com.example.crrlo.assets;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Path;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            CopyRawToFile(R.raw.stockfidh,"stock");
            MakeExecutable("stock");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Stockfish s = new Stockfish(new File(getFilesDir(),"stock").getAbsolutePath());
       s.startEngine();
        s.sendCommand("go movetime 500");
        Toast.makeText(MainActivity.this, s.getOutput(1000), Toast.LENGTH_SHORT).show();

    }

    private String MakeExecutable(String filename) {
        //First get the absolute path to the file
        File folder = getFilesDir();

        String filefolder = null;
        try {
            filefolder = folder.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!filefolder.endsWith("/"))
            filefolder += "/";

        String fullpath = filefolder + filename;

        try {
            Runtime.getRuntime().exec("chmod 770 " + fullpath).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return fullpath;
    }


    private void CopyRawToFile(int resourceId, String filename) throws IOException {
        InputStream input = getResources().openRawResource(resourceId);
        OutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);

        byte[] buffer = new byte[1024 * 4];
        int a;
        while((a = input.read(buffer)) > 0)
            output.write(buffer, 0, a);

        input.close();
        output.close();
    }














    private void copyFile(InputStream stream, String localPath, Context context) {
        try {
            InputStream in = stream;
            FileOutputStream out = new FileOutputStream(localPath);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();

        } catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private final synchronized void writeToProcess(String data) throws IOException {
        if (this.writer != null) {
            this.writer.write(data);
            this.writer.flush();
        }
    }

    private final String readFromProcess() throws IOException {
        if (this.reader != null) {
            return this.reader.readLine();
        }
        return null;
    }


}