package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.filenet.FileNetUpId;
import com.example.filenet.FileNetUpload;
import com.example.filenet.UplodeFolder;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.Document;
import com.filenet.api.core.EngineObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import FileNet.FileNetConnections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/file-upload")
public class FileNetController {

    @PostMapping
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("testname") String testname) {
        try {
        	
            // تحويل MultipartFile إلى File
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            // رفع الملف إلى FileNet
            FileNetUpload.UploadingFile(convFile, title, testname);

            return "File uploaded successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload file: " + e.getMessage();
        }
    }

    @PostMapping("/updateDocument")
    public String updateDocumentVersion(
            @RequestParam String documentId,
            @RequestParam String filePath,
            @RequestParam String newTitle,
            @RequestParam String topic) {
        File newFile = new File(filePath);
        if (!newFile.exists()) {
            return "File does not exist: " + filePath;
        }
        
        try {
            // Connect to FileNet Object Store
            ObjectStore os = FileNetConnections.Connect();
            if (os != null) {
                // Update the document's version
                FileNetUpId.updateDocumentVersion(os, documentId, newFile, newTitle, topic);

                // Optionally delete the file if no longer needed
                // newFile.delete();

                return "Document updated successfully!";
            } else {
                return "Failed to connect to the ObjectStore";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }
   
    
    
    
  @PostMapping("/updateDocumentByTestName")
public String updateDocumentVersionByTestName(
        @RequestParam String topic,
        @RequestParam String filePath,
        @RequestParam String newTitle,
        @RequestParam String DateSent,
        @RequestParam String description,
        @RequestParam String comment,
        @RequestParam int severity,
        @RequestParam int dissemination
     ) {

    File newFile = new File(filePath);
    if (!newFile.exists()) {
        return "File does not exist: " + filePath;
    }

    try {
        // Connect to FileNet Object Store
        ObjectStore os = FileNetConnections.Connect();
        if (os != null) {
            // Update the document's version using topic and additional properties
            FileNetUpId.updateDocumentVersionByTestName(os, topic, newFile, newTitle, DateSent,
                    description, comment, severity, dissemination);

            return "Document updated successfully!";
        } else {
            return "Failed to connect to the ObjectStore";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "An error occurred: " + e.getMessage();
    }
}

    @GetMapping("/count")
    public int getFileCount() {
        int fileCount = 0;

        try {
            // الاتصال بـ ObjectStore
            ObjectStore os = FileNetConnections.Connect();
            if (os != null) {
                // إنشاء استعلام لجلب جميع الوثائق
                String query = "SELECT This FROM Document";
                SearchSQL sqlObject = new SearchSQL(query);
                SearchScope searchScope = new SearchScope(os);
                RepositoryRowSet rowSet = searchScope.fetchRows(sqlObject, null, null, null);

                // حساب عدد الصفوف
                if (rowSet != null) {
                	while (rowSet.iterator().hasNext()) {
                        rowSet.iterator().next();
                        fileCount++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileCount; // إعادة عدد الملفات
    }

    @PostMapping("/uploadFolder")
    public String uploadFolder(
            @RequestParam String folderPath,
            @RequestParam String testname) {
        
        File directory = new File(folderPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return "The provided path is not a valid directory: " + folderPath;
        }

        try {
            UplodeFolder.UploadingFolder(folderPath, testname);
            return "All files in the folder have been uploaded successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }
    }




}
