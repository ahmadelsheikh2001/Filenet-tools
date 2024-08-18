package com.example.filenet;

import com.filenet.api.core.*;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.*;
import com.filenet.api.util.*;

import FileNet.FileNetConnections;

import org.apache.tika.Tika;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;

public class FileNetUpId {

    // Method to check-out, update, and check-in a document
    public static void updateDocumentVersion(ObjectStore os, String documentId, File newFile, String newTitle, String topic) {
        try {
            System.out.println("Fetching document with ID: " + documentId);
            Document document = Factory.Document.fetchInstance(os, documentId, null);
            if (document != null) {
                System.out.println("Document fetched successfully.");

                // Check-out the document
                System.out.println("Checking out the document...");
                document.checkout(ReservationType.EXCLUSIVE, null, document.getClassName(), document.getProperties());
                document.save(RefreshMode.REFRESH);
                Document reservation = (Document) document.get_Reservation();
                System.out.println("Document checked out successfully.");

                // Determine MIME type
                System.out.println("Determining MIME type...");
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mimeType = fileNameMap.getContentTypeFor(newFile.getName());
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    Tika typeFinder = new Tika();
                    mimeType = typeFinder.detect(newFile);
                }
                System.out.println("MIME type determined: " + mimeType);

                // Create content element
                System.out.println("Creating content element...");
                ContentElementList cel = Factory.ContentElement.createList();
                ContentTransfer ct = Factory.ContentTransfer.createInstance();
                try (FileInputStream fileInputStream = new FileInputStream(newFile)) {
                    ct.setCaptureSource(fileInputStream);
                    ct.set_ContentType(mimeType);
                    ct.set_RetrievalName(newFile.getName());
                    cel.add(ct);
                    System.out.println("Content element created.");

                    // Set the new content, title, and topic property
                    System.out.println("Setting new content, title, and testname property...");
                    reservation.set_ContentElements(cel);
                    reservation.set_MimeType(mimeType);
                    reservation.getProperties().putValue("DocumentTitle", newTitle);
                    reservation.getProperties().putValue("topic", topic);  // Set the topic property

                    // Check-in the document as a new major version
                    System.out.println("Checking in the document as a new major version...");
                    reservation.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
                    reservation.save(RefreshMode.REFRESH);
                    System.out.println("Document checked in successfully. Major version updated.");
                }
            } else {
                System.out.println("Failed to fetch document.");
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        } catch (EngineRuntimeException e) {
            System.err.println("Engine runtime exception: " + e.getMessage());
        }
    }

    
   public static void updateDocumentVersionByTestName(ObjectStore os, String topic, File newFile, String newTitle,
        String DateSent,
        String description, String comment, 
        int severity, int dissemination) {

    try {
        String query = "SELECT * FROM Document WHERE Topic = '" + topic + "'";
        SearchSQL sqlObject = new SearchSQL(query);
        SearchScope searchScope = new SearchScope(os);
        RepositoryRowSet rowSet = searchScope.fetchRows(sqlObject, null, null, null);

        if (rowSet.isEmpty()) {
            System.out.println("No document found with the Ot topic: " + topic);
            return;
        }

        RepositoryRow row = (RepositoryRow) rowSet.iterator().next();
        Document document = Factory.Document.fetchInstance(os, row.getProperties().getIdValue(PropertyNames.ID), null);

        // Check-out and update the document
        if (document != null) {
            FileNetUpId.updateDocumentVersion(os, document.get_Id().toString(), newFile, newTitle, topic);

            // Update additional properties
            document.getProperties().putValue("DateSent", DateSent);

            document.getProperties().putValue("Description", description);
            document.getProperties().putValue("Comment", comment);

            document.getProperties().putValue("Severity", severity);
            document.getProperties().putValue("Dissemination", dissemination);


            // Save the changes
            document.save(RefreshMode.REFRESH);
        } else {
            System.out.println("Failed to fetch document.");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

//    public static void main(String[] args) {
//        try {
//            // Connect to FileNet Object Store
//            ObjectStore os = FileNetConnections.Connect();
//            if (os != null) {
//                // Define the 'testname', file path, and new title
//                String testName = "SampleTestName"; // Update this value as needed
//                File newFile = new File("D:\\test\\test.txt"); // Update this path as needed
//                String newTitle = "Elsheikh";
//
//                // Update the document's version by 'testname'
//                updateDocumentVersionByTestName(os, testName, newFile, newTitle);
//            } else {
//                System.out.println("Failed to connect to the ObjectStore");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
