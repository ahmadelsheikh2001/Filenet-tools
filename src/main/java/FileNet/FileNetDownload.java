package FileNet;

import com.filenet.api.core.*;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.*;
import com.filenet.api.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileNetDownload {

    // Method to download a document and save it locally
    public static void downloadDocument(ObjectStore os, String documentId, String localFilePath) {
        System.out.println("Fetching document with ID: " + documentId);
        Document document = Factory.Document.fetchInstance(os, documentId, null);
        if (document != null) {
            System.out.println("Document fetched successfully.");

            try {
                // Get the content elements
                ContentElementList contentElements = document.get_ContentElements();
                if (contentElements != null && !contentElements.isEmpty()) {
                    // Assume there is only one content element
                    ContentTransfer contentTransfer = (ContentTransfer) contentElements.get(0);
                    try (InputStream inputStream = contentTransfer.accessContentStream();
                         FileOutputStream outputStream = new FileOutputStream(new File(localFilePath))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Document downloaded and saved successfully to: " + localFilePath);
                    }
                } else {
                    System.out.println("No content elements found for the document.");
                }
            } catch (IOException e) {
                System.out.println("Error occurred while saving the document: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to fetch document.");
        }
    }

    public static void main(String[] args) {
        try {
            // Connect to FileNet Object Store
            ObjectStore os = FileNetConnections.Connect();
            if (os != null) {
                // Define document ID and local file path
                String documentId = "{50763191-0000-CE14-8F7B-CE8A26D363D3}";
                String localFilePath = "D:\\DownloadedDocument.pdf"; // Update this path as needed
                
                // Download the document
                downloadDocument(os, documentId, localFilePath);
            } else {
                System.out.println("Failed to connect to the ObjectStore");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
