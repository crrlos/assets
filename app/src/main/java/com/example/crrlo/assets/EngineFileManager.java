package com.example.crrlo.assets;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;

public class EngineFileManager {
    final String TAG;
    String currentFilePath;
    String dataEnginesPath;
    private BufferedReader reader;
    private BufferedWriter writer;

    class AlphabeticComparator implements Comparator<Object> {
        AlphabeticComparator() {
        }

        @SuppressLint({"DefaultLocale"})
        public int compare(Object o1, Object o2) {
            return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
        }
    }

    public EngineFileManager() {
        this.TAG = "EngineFileManager";
        this.dataEnginesPath = "/data/data/com.example.crrlo.assets/engine/";
        this.currentFilePath = "/data/data/com.example.crrlo.assets/";
        this.reader = null;
        this.writer = null;

    }

    public String getExternalDirectory() {
        return Environment.getExternalStorageDirectory().toString();
    }

    public String getParentFile(String path) {
        String newPath = BuildConfig.FLAVOR;
        try {
            return new File(path).getParentFile().toString();
        } catch (NullPointerException e) {
            return path;
        }
    }

    public String[] getFileArrayFromPath(String path) {
        String[] sortedFiles = null;
        try {
            sortedFiles = new File(path).list();
            Arrays.sort(sortedFiles, new AlphabeticComparator());
            this.currentFilePath = path;
            return sortedFiles;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return sortedFiles;
        }
    }

    public boolean fileIsDirectory(String fileName) {
        return new File(this.currentFilePath, fileName).isDirectory();
    }

    public String[] getFileArrayFromData() {
        String[] sortedFiles = new File(this.dataEnginesPath).list();
        Arrays.sort(sortedFiles, new AlphabeticComparator());
        return sortedFiles;
    }

    public boolean writeEngineToData(String filePath, String fileName, InputStream is) {
        File file;
        File file2 = new File(this.dataEnginesPath);
        if (!file2.exists() && !file2.mkdir()) {
            return false;
        }
        if (new File(this.dataEnginesPath, fileName).exists()) {
            try {
                try {
                    Runtime.getRuntime().exec(new String[]{"chmod", "744", new File(this.dataEnginesPath, fileName).getAbsolutePath()}).waitFor();
                    return true;
                } catch (InterruptedException e) {
                    deleteFileFromData(fileName);
                    return false;
                }
            } catch (IOException e2) {
                deleteFileFromData(fileName);
                return false;
            }
        }
        InputStream istream;
        if (is != null) {
            istream = is;
        } else {
            File f = new File(filePath, fileName);
            try {
                istream = new FileInputStream(f);
                file = f;
            } catch (IOException e3) {
                file = f;
                deleteFileFromData(fileName);
                return false;
            }
        }
        try {
            FileOutputStream fout = new FileOutputStream(this.dataEnginesPath + fileName);
            byte[] b = new byte[1024];
            while (true) {
                int noOfBytes = istream.read(b);
                if (noOfBytes == -1) {
                    break;
                }
                fout.write(b, 0, noOfBytes);
            }
            istream.close();
            fout.close();
            try {
                String[] cmd = new String[3];
                cmd[0] = "chmod";
                cmd[1] = "744";
                cmd[2] = this.dataEnginesPath + fileName;
                try {
                    Runtime.getRuntime().exec(cmd).waitFor();
                    if (isEngineProcess(fileName)) {
                        return true;
                    }
                    deleteFileFromData(fileName);
                    return false;
                } catch (InterruptedException e4) {
                    deleteFileFromData(fileName);
                    return false;
                }
            } catch (IOException e5) {
                deleteFileFromData(fileName);
                return false;
            }
        } catch (IOException e6) {
            deleteFileFromData(fileName);
            return false;
        }
    }

    public  boolean isEngineProcess(String file) {
        boolean isProcess = false;
        try {
            Process process = new ProcessBuilder(new String[]{"/data/data/com.example.crrlo.assets/engine/critter"}).start();
            OutputStream stdout = process.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(stdout));
            try {
                writeToProcess("isready\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line;
            int cnt = 0;
            while (cnt < 100) {
                try {
                    line = readFromProcess();
                    if (!(line == null || (line.equals("readyok") || line.endsWith("readyok")) == false)) {
                        isProcess = true;
                        cnt = 100;
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e3) {
                }
                cnt++;
            }
            process.destroy();
            return isProcess;
        } catch (IOException e4) {
            return false;
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

    public boolean dataFileExist(String file) {
        return new File(this.dataEnginesPath, file).exists();
    }

    public boolean deleteFileFromData(String file) {
        return new File(this.dataEnginesPath, file).delete();
    }
}
