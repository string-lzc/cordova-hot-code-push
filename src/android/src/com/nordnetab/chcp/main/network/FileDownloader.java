package com.nordnetab.chcp.main.network;

import android.util.Log;

import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.utils.FilesUtility;
import com.nordnetab.chcp.main.utils.MD5;
import com.nordnetab.chcp.main.utils.ProgressCallback;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.utils.URLConnectionHelper;
import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Helper class to download files.
 */
public class FileDownloader {


    /**
     * Download list of files.
     * Full url to the file is constructed from the contentFolderUrl and ManifestFile#hash (relative path).
     * For each downloaded file we perform check of his hash. If it is different from the one, that provided
     * if ManifestFile#hash - exception will be thrown.
     * Download stops on any error.
     *
     * @param downloadFolder   absolute path to the folder, where downloaded files should be placed
     * @param contentFolderUrl root url on the server, where all files are located
     * @param files            list of files to download
     * @throws Exception
     * @see ManifestFile
     */
    public static void downloadFiles(final String downloadFolder,
                                     final String contentFolderUrl,
                                     final List<ManifestFile> files,
                                     final Map<String, String> requestHeaders,
                                     final ProgressCallback progressCallback) throws Exception {
        for (ManifestFile file : files) {
            String fileUrl = URLUtility.construct(contentFolderUrl, file.name);
            String filePath = Paths.get(downloadFolder, file.name);
            download(fileUrl, filePath, file.hash, requestHeaders, progressCallback);
        }
    }

    /**
     * Download file from server, save it on the disk and check his hash.
     *
     * @param urlFrom  url to download from
     * @param filePath where to save file
     * @param checkSum checksum of the file
     * @throws IOException
     */
    public static void download(final String urlFrom,
                                final String filePath,
                                final String checkSum,
                                final Map<String, String> requestHeaders,
                                final ProgressCallback progressCallback) throws Exception {
        Log.d("CHCP", "Loading file: " + urlFrom);
        final MD5 md5 = new MD5();

        final File downloadFile = new File(filePath);
        FilesUtility.delete(downloadFile);
        FilesUtility.ensureDirectoryExists(downloadFile.getParentFile());

        // download file
        final URLConnection connection = URLConnectionHelper.createConnectionToURL(urlFrom, requestHeaders);
        final InputStream input = new BufferedInputStream(connection.getInputStream());
        final OutputStream output = new BufferedOutputStream(new FileOutputStream(filePath, false));

        final byte data[] = new byte[1024];
        int count;
        long downloadedBytes = 0;
        long totalBytes = connection.getContentLength(); // 获取总字节数

        // final int progressThreshold = 1024 * 1024; // 阈值为1MB
        final int progressThreshold = 100; // 阈值为100KB
        int progressCount = progressThreshold;
        Log.e("CHCP", "totalBytes: " + totalBytes);
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
            md5.write(data, count);
            downloadedBytes += count;
            
            if (totalBytes > 0) {
                if (downloadedBytes >= progressCount) {
                    float progress = (float) downloadedBytes / totalBytes * 100; // 计算下载进度
                    Map<String, Object> progressData = new HashMap<String, Object>();
                    progressData.put("MSG_DOWNLOAD_PROGRESS", progress);
                    progressCallback.onProgressUpdate(progressData);
                    progressCount += progressThreshold;
                }
            }
        }

        output.flush();
        output.close();
        input.close();

        final String downloadedFileHash = md5.calculateHash();
        if (!downloadedFileHash.equals(checkSum)) {
            throw new IOException("File is corrupted: checksum " + checkSum + " doesn't match hash " + downloadedFileHash + " of the downloaded file");
        }
    }
}
