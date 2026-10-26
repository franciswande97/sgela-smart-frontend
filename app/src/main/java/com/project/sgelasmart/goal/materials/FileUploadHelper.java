package com.project.sgelasmart.goal.materials;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FileUploadHelper {

    public static MultipartBody.Part prepareFilePart(Context context, String partName, Uri fileUri) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        String fileName = getFileName(resolver, fileUri);
        String mimeType = resolver.getType(fileUri);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        InputStream inputStream = resolver.openInputStream(fileUri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        byte[] bytes = byteBuffer.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
        return MultipartBody.Part.createFormData(partName, fileName, requestFile);
    }

    private static String getFileName(ContentResolver resolver, Uri uri) {
        String name = "uploaded_material";
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        }
        return name;
    }
}
