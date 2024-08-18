/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;


/**
 *
 * @author administrator
 */
public class IOUtils {
    public static byte[] readDocContentFromFile(File f)
    {
        FileInputStream is;
        byte[] b = null;
        int fileLength = (int)f.length();
        if(fileLength != 0)
        {
        	try
        	{
        		is = new FileInputStream(f);
                System.out.println(f.getName()+"               "+fileLength);
        		b = new byte[fileLength];
        		is.read(b);
        		is.close();
        	}
        	catch (Exception e)
        	{
                
        		e.printStackTrace();
        	}
        }
        return b;
    }

    public static Double writeContent(InputStream s, String filename) throws IOException
    {
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream("C:\\dir\\"+filename) );
        Double size = new Double(0);
        int bufferSize;
        byte[] buffer = new byte[1024];
        while( ( bufferSize = s.read(buffer) ) != -1 )
        {
            size += bufferSize;
            writer.write(buffer, 0, bufferSize);
        }
        writer.close();
        s.close();
        return size;
    }

    public static File downloadContentTransfer(ContentTransfer element) throws Exception{
        String filename;
        try{
            //System.out.println(41);
            filename = element.get_RetrievalName();
            //System.out.println(42);
            InputStream stream = element.accessContentStream();
            //System.out.println(43);
            writeContent(stream,filename);
            //System.out.println(44);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
        return new File("docs\\"+filename);
    }

    public void downloadDoc(Document d) throws Exception{
        ContentElementList elements = d.get_ContentElements();
        ContentTransfer element = (ContentTransfer)elements.get(0);
        String filename = element.get_RetrievalName();
        InputStream stream = element.accessContentStream();
        Double size = writeContent(stream,filename);
        System.out.println("Filename = "+filename);
        Double expected = element.get_ContentSize();
        if( size != expected ){
            System.out.println(size);
            System.out.println(expected);
            System.err.println("Invalid content size retrieved");
        }
        /*d = Factory.Document.fetchInstance("/document2",null);
        elements = d.get_ContentElements();
        for( Iterator i = elements.iterator(); i.hasNext(); )
        {
            element = (ContentTransfer)i.next();
            writeContent(element.accessContentStream(),"element" + element.get_ElementSequenceNumber());
        }*/
    }

    public static String createLogFile(String className){
        String fileName="";
        try{
            Calendar cal=Calendar.getInstance();
            fileName="MigrationLog."+cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"_"+className;
            File f=new File("c:\\MigrationLogs\\"+fileName+".xls");
            if(f.exists()){
                int count=1;
                File f2=new File("c:\\MigrationLogs\\"+fileName+"_"+count+".xls");
                while(f2.exists()){
                    count++;
                    f2=new File("c:\\MigrationLogs\\"+fileName+"_"+count+".xls");
                }
                fileName=fileName+"_"+count;
            }
            File fol=new File("c:\\MigrationLogs");
            fol.mkdirs();
            File fol2=new File("docs");
            fol2.mkdirs();
//            FileOutputStream out = new FileOutputStream("c:\\MigrationLogs\\"+fileName+".xls");
//            Workbook wb = new HSSFWorkbook();
//            Sheet s = wb.createSheet();
//            Row r = null;
//            Cell c = null;
//            wb.setSheetName(0, "HSSF Test");
//            r = s.createRow(0);
//            c = r.createCell(0);
//            c.setCellValue("Status");
//            c = r.createCell(1);
//            c.setCellValue("Old Document Id");
//            c = r.createCell(2);
//            c.setCellValue("New Document Id");
//            c = r.createCell(3);
//            c.setCellValue("Error Type");
//            wb.setSheetName(0, "Migration Log");
//            wb.write(out);
//            out.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return fileName;
    }

    public static String createFailedLogFile(String className){
        String fileName="";
        try{
            Calendar cal=Calendar.getInstance();
            fileName="EmptyDocumentsLog."+cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"_"+className;
            File f=new File("c:\\MigrationLogs\\"+fileName+".xls");
            if(f.exists()){
                int count=1;
                File f2=new File("c:\\MigrationLogs\\"+fileName+"_"+count+".xls");
                while(f2.exists()){
                    count++;
                    f2=new File("c:\\MigrationLogs\\"+fileName+"_"+count+".xls");
                }
                fileName=fileName+"_"+count;
            }
//            FileOutputStream out = new FileOutputStream("c:\\MigrationLogs\\"+fileName+".xls");
//            Workbook wb = new HSSFWorkbook();
//            Sheet s = wb.createSheet();
//            Row r = null;
//            Cell c = null;
//            r = s.createRow(0);
//            c = r.createCell(0);
//            c.setCellValue("Document Id");
//            wb.setSheetName(0, "Failed Documents Log");
//            wb.write(out);
//            out.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return fileName;
    }

    public static void addToLogFile(String fileName,List<String> values){
        try {
//            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File(fileName)));
//            FileOutputStream out = new FileOutputStream(fileName);
//            HSSFWorkbook wb = new HSSFWorkbook(fs);
//            HSSFSheet sheet = wb.getSheetAt(0);
//            int rows; // No of rows
//            rows = sheet.getPhysicalNumberOfRows();
//            Row r=sheet.createRow(rows);
//            for(int i=0;i<values.size();i++){
//                Cell c=r.createCell(i);
//                c.setCellValue(values.get(i));
//            }
//            wb.write(out);
//            out.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }

    }
    public static boolean isIdExistInLog(String id,String className){
        try {
//            Calendar cal=Calendar.getInstance();
//            String fileName="MigrationLog."+cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"_"+className;
//            File f=new File("c:\\MigrationLogs\\"+fileName+".xls");
//            int count=1;
//            while(f.exists()){
//                POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(f));
//                HSSFWorkbook wb = new HSSFWorkbook(fs);
//                HSSFSheet sheet = wb.getSheetAt(0);
//                HSSFRow row;
//                HSSFCell cell;
//                HSSFCell cell2;
//                int rows; // No of rows
//                rows = sheet.getPhysicalNumberOfRows();
//                for(int i = 0; i < rows; i++) {
//                    row = sheet.getRow(i);
//                    if(row != null) {
//                        cell = row.getCell(0);
//                        cell2 = row.getCell(1);
//                        if(cell != null) {
//                            if(cell.getStringCellValue().equals("Success") && cell2.getStringCellValue().equals(id)){
//                                return true;
//                            }
//                        }
//                    }
//                }
//                
//                f=new File("c:\\MigrationLogs\\"+fileName+"_"+count+".xls");
//                count++;
//            }

        } catch(Exception ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    public static String getRecordMapping(String id){
        try {
//            File f=new File("MappingSheet.xls");
//            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(f));
//            HSSFWorkbook wb = new HSSFWorkbook(fs);
//            HSSFSheet sheet = wb.getSheetAt(0);
//            HSSFRow row;
//            HSSFCell cell;
//            int rows; // No of rows
//            rows = sheet.getPhysicalNumberOfRows();
//            for(int i = 0; i < rows; i++) {
//                row = sheet.getRow(i);
//                if(row != null) {
//                    cell = row.getCell(0);
//                    if(cell != null) {
//                        if(cell.getStringCellValue().trim().equals(id.trim())){
//                            return row.getCell(1).getStringCellValue().trim();
//                        }
//                    }
//                }
//            }


        } catch(Exception ioe) {
            ioe.printStackTrace();
        }
        return id.trim();
    }

    static public void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        //return (path.delete());
    }
}
