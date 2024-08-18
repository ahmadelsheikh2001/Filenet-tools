package com.example.filenet;

import com.filenet.api.core.*;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.*;
import com.filenet.api.util.*;

import FileNet.FileNetConnections;

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

public class UplodeFolder {

    public static void UploadingFolder(String folderPath, String testname) throws IOException {
        System.out.println("Starting the folder upload process...");

        // Connect to FileNet Object Store
        System.out.println("Connecting to FileNet Object Store...");
        ObjectStore os = FileNetConnections.Connect();
        if (os == null) {
            System.out.println("Failed to connect to FileNet Object Store.");
            return;
        }
        System.out.println("Connected to FileNet Object Store.");

        // Define folder path and document class
        String FolderName = "/Correspondence/صادر";
        System.out.println("Fetching the folder: " + FolderName);
        Folder folder = Factory.Folder.fetchInstance(os, FolderName, null);
        System.out.println("Folder fetched successfully.");
        
        String DocClass = "Outgoing";       
        // Get the files from the folder
        File directory = new File(folderPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("The provided path is not a valid directory.");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the directory.");
            return;
        }

        // Loop through each file and upload it
        for (File Ufile : files) {
            if (Ufile.isFile()) {
                System.out.println("Processing file: " + Ufile.getName());

                // Create a new document instance
                System.out.println("Creating a new document with class: " + DocClass);
                Document document = Factory.Document.createInstance(os, DocClass);
                System.out.println("Document instance created.");

                // Determine MIME type
                System.out.println("Determining MIME type...");
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mimeType = fileNameMap.getContentTypeFor(Ufile.getName());
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    Tika typeFinder = new Tika();
                    mimeType = typeFinder.detect(Ufile);
                }
                System.out.println("MIME type determined: " + mimeType);

                // Create content element
                System.out.println("Creating content element...");
                ContentElementList cel = Factory.ContentElement.createList();
                ContentTransfer ct = Factory.ContentTransfer.createInstance();
                FileInputStream fileInputStream = new FileInputStream(Ufile);
                ct.setCaptureSource(fileInputStream);
                ct.set_ContentType(mimeType);
                ct.set_RetrievalName(Ufile.getName());
                cel.add(ct);
                System.out.println("Content element created.");

                // Set content elements and MIME type to document
                System.out.println("Setting content and MIME type to document...");
                document.set_ContentElements(cel);
                document.set_MimeType(mimeType);
                document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
                document.save(RefreshMode.REFRESH);
                System.out.println("Document content and MIME type set successfully.");

                // Set the document name and custom property
                System.out.println("Setting the document name and custom property...");
                document.getProperties().putValue("DocumentTitle", Ufile.getName());
                document.getProperties().putValue("testname", testname); // Adding the custom property
                document.save(RefreshMode.REFRESH);
                System.out.println("Document name and custom property set.");

                // Link the document to the folder
                System.out.println("Linking the document to the folder...");
                ReferentialContainmentRelationship rcr = folder.file(document, AutoUniqueName.AUTO_UNIQUE, document.get_Name(), DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
                rcr.save(RefreshMode.REFRESH);
                System.out.println("Document linked to folder successfully.");

                fileInputStream.close();
                System.out.println("File uploaded successfully.");
            }
        }

        System.out.println("All files in the folder have been processed.");
    }
}
