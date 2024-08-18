/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.filenet.api.collection.BinaryList;
import com.filenet.api.collection.BooleanList;
import com.filenet.api.collection.ClassDescriptionSet;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DateTimeList;
import com.filenet.api.collection.DependentObjectList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.Float64List;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.collection.IdList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.Integer32List;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.collection.StringList;
import com.filenet.api.collection.VersionableSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.FilteredPropertyType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.EngineObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.meta.ClassDescription;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.filenet.apiimpl.core.DocumentClassDefinitionImpl;

/**
 *
 * @author administrator
 */
public class CEUtils {

    
	public static Vector getOSNames(CEConnection ce)
    {
    	Vector osnames=new Vector();
        Iterator it = ce.getOSSet().iterator();
        while(it.hasNext())
        {
            ObjectStore os = (ObjectStore) it.next();
            osnames.add(os.get_DisplayName());
        }
        return osnames;
    }

    public static List<Folder> getObjectStoreFolders(CEConnection ce,String osName){
        List<Folder> result=new ArrayList<Folder>();
        ObjectStore os=ce.fetchOS(osName);
        FolderSet folders=os.get_TopFolders();
        Iterator it = folders.iterator();
        while(it.hasNext())
        {
            Folder currFolder = (Folder) it.next();
            if(!currFolder.getProperties().getBooleanValue("isHiddenContainer"))
                result.add(currFolder);
        }
        return result;
    }

    public static List<ClassDescription> getObjectStoreClasses(CEConnection ce,String osName){
        List<ClassDescription> result=new ArrayList<ClassDescription>();
        //System.out.println(ce.getDomainName());
        ObjectStore os=ce.fetchOS(osName);
        //System.out.println("1");
        ClassDescriptionSet classes=os.get_ClassDescriptions();
        HashSet x = (HashSet)classes;
        List<ClassDescription> test = new ArrayList<ClassDescription>(x);
        //System.out.println("2");
        Iterator it = classes.iterator();
        System.out.println(test.size());
        System.out.println(classes.isEmpty());
       do
        {
            //System.out.println("4");
            ClassDescription currClass = (ClassDescription) it.next();
            //System.out.println("5");
            if(!currClass.get_IsHidden() && currClass.get_SuperclassDescription().get_Name().equals("Document"))
                result.add(currClass);
        } while(it.hasNext());
        //System.out.println("6");
        return result;
    }

    public static List<KeyValue> getObjectStoreClasses2(CEConnection ce,String osName,int classType){
        List<KeyValue> filenetDocuments = new ArrayList<KeyValue>();
        SearchSQL sqlObject = new SearchSQL("SELECT SymbolicName, Id, IsHidden, IsSystemOwned, This,SuperClassDefinition FROM DocumentClassDefinition WHERE AllowsSubclasses=TRUE AND IsSystemOwned=FALSE AND IsHidden=FALSE");
        SearchScope scope = new SearchScope(ce.fetchOS(osName));
        RepositoryRowSet rowSet;
        rowSet = scope.fetchRows(sqlObject, null, null, false);
        for( Iterator i = rowSet.iterator(); i.hasNext(); )
        {
            RepositoryRow obj = (RepositoryRow)i.next();
            if(classType == 1 && "TechDoc".equals(((DocumentClassDefinitionImpl)obj.getProperties().get("SuperClassDefinition").getObjectValue()).get_SymbolicName().toString()))
                filenetDocuments.add(new KeyValue(obj.getProperties().get("SymbolicName").getStringValue(),obj.getProperties().get("Id").getIdValue().toString()));
            else if(classType == 2 && !"TechDoc".equals(obj.getProperties().get("SymbolicName").getStringValue()) && "RandWaterBaseDocumentClass".equals(((DocumentClassDefinitionImpl)obj.getProperties().get("SuperClassDefinition").getObjectValue()).get_SymbolicName().toString()))
                filenetDocuments.add(new KeyValue(obj.getProperties().get("SymbolicName").getStringValue(),obj.getProperties().get("Id").getIdValue().toString()));
        }
        Collections.sort(filenetDocuments,COMPARATOR);
        return filenetDocuments;

    }
    private static Comparator<KeyValue> COMPARATOR = new Comparator<KeyValue>()
    {
       public int compare(KeyValue o1, KeyValue o2)
       {
         return o1.getKey().compareTo(o2.getKey());
       }
    };

    public static List<String> getDocumentsList(String docClass,ObjectStore os){

        /*String adhocSearchXML = "<request>" +
                "<objectstores mergeoption=\"union\"><objectstore id=\"" + osId + "\"/></objectstores>" +
                "<querystatement>SELECT DateLastModified FROM " + docClass + " Order By DocumentTitle</querystatement>"+
                "<options maxrecords=\"500\"/>" +
                "</request>";
        Session myCESession= ObjectFactory.getSession("MyApp", Session.DEFAULT,"fntadmin","Password123");
        myCESession.setConfiguration(new FileInputStream ("WcmApiConfig.properties"));
        Search searchObject = ObjectFactory.getSearch(myCESession);
        BaseObjects colBaseObjects = searchObject.singleObjectTypeExecute(adhocSearchXML, BaseObject.TYPE_DOCUMENT);

        Iterator it = colBaseObjects.iterator();
        List<Document> filenetDocuments = new ArrayList<Document>();
        int counter = 1;

        while (it.hasNext()) {
            Document document = (Document) it.next();
            filenetDocuments.add(document);

        }*/
        List<String> filenetDocuments = new ArrayList<String>();
        SearchSQL sqlObject = new SearchSQL("SELECT DocumentTitle, Id FROM [" + docClass+"] where VersionStatus <> 4" );
        SearchScope scope = new SearchScope(os);
        IndependentObjectSet set = scope.fetchObjects(sqlObject, null, null, false);
        // Loop through the returned results
        for( Iterator i = set.iterator(); i.hasNext(); )
        {
            Document obj = (Document)i.next();
            filenetDocuments.add(obj.get_Id().toString());
        }
        return filenetDocuments;
    }

    public static int getDocumentsCount(String docClass,ObjectStore os){
        SearchSQL sqlObject = new SearchSQL("SELECT Id FROM [" + docClass+"] where VersionStatus<>4" );
        SearchScope scope = new SearchScope(os);
        RepositoryRowSet rowSet;
        rowSet = scope.fetchRows(sqlObject, null, null, false);
        Iterator i=rowSet.iterator();
        int count =0;
        while(i.hasNext()){
            i.next();
            count++;
        }
        
        return count;
    }

    public static int getDocumentsCount2(String docClass,ObjectStore os) throws Exception{
        int iCount=0;
        long startTime = System.currentTimeMillis();
          SearchScope searchScope = new SearchScope(os);
          String query = "SELECT Id from "+docClass+" where VersionStatus<>4";

          SearchSQL sql = new SearchSQL();
          sql.setQueryString(query);

          Integer myPageSize = new Integer(100);
          PropertyFilter propertyFilter = new PropertyFilter();
          propertyFilter.setMaxRecursion(1);
          propertyFilter.addIncludeType(1, null, null, FilteredPropertyType.ANY,null);
          Boolean continuable = new Boolean(true);
          RepositoryRowSet rowSet = searchScope.fetchRows(sql, myPageSize,propertyFilter, continuable);

          if (rowSet.isEmpty()) {
             return 0;
              //throw new Exception();

          } else {
             Iterator iterator = rowSet.iterator();


             while (iterator.hasNext()) {
                RepositoryRow row = (RepositoryRow) iterator.next();
    //            String docId = row.getProperties().get("Id").getIdValue().toString();
    //                System.out.println(" Document ID : "+docId);
                    iCount++;

             }
             System.out.println("Record count : "+iCount);
             long endTime = System.currentTimeMillis();
             System.out.println(" time taken : "+(endTime - startTime)/1000);

          }
          return iCount;
    }

    public static  List<String> getDocumentsList2(String docClass,ObjectStore os) throws Exception{
        List<String> result = new ArrayList<String>();
          SearchScope searchScope = new SearchScope(os);
          String query = "SELECT Id from "+docClass+" where VersionStatus<>4";

          SearchSQL sql = new SearchSQL();
          sql.setQueryString(query);

          Integer myPageSize = new Integer(100);
          PropertyFilter propertyFilter = new PropertyFilter();
          propertyFilter.setMaxRecursion(1);
          propertyFilter.addIncludeType(1, null, null, FilteredPropertyType.ANY,null);
          Boolean continuable = new Boolean(true);
          RepositoryRowSet rowSet = searchScope.fetchRows(sql, myPageSize,propertyFilter, continuable);

          if (rowSet.isEmpty()) {
             throw new Exception();

          } else {
             Iterator iterator = rowSet.iterator();


             while (iterator.hasNext()) {
                RepositoryRow row = (RepositoryRow) iterator.next();
                String docId = row.getProperties().get("Id").getIdValue().toString();
                result.add(docId);
    //                System.out.println(" Document ID : "+docId);


             }


          }
          return result;
    }

    private static List<Folder> getSubFolders(Folder f){
        FolderSet fs=f.get_SubFolders();
        Iterator it = fs.iterator();
        List<Folder> result=new ArrayList<Folder>();
        while(it.hasNext())
        {
            Folder currFolder = (Folder) it.next();
            result.add(currFolder);
        }
        return result;
    }

    private static List<String> getFolderContents(Folder f){
        DocumentSet ds=f.get_ContainedDocuments();
        Iterator it = ds.iterator();
        List<String> result=new ArrayList<String>();
        while(it.hasNext())
        {
            Document currDoc = (Document) it.next();
            result.add(currDoc.get_Id().toString());
        }
        return result;
    }

    public static List<String> getAllFolderContents(Folder f){
        List<String> result=new ArrayList<String>();
        List<Folder> childFolders=getSubFolders(f);
        result.addAll(getFolderContents(f));
        if(childFolders.size()==0){
            return result;
        }
        for(int i=0;i<childFolders.size();i++){
            result.addAll(getAllFolderContents(childFolders.get(i)));
        }
        return result;
    }

    public static String addDocument(Document d,ObjectStore destOS) throws Exception{
    	
    	
    	
        Document doc=Factory.Document.createInstance(destOS, d.getClassName());
        doc.set_OwnerDocument(d);
        doc.getProperties().putValue("DocumentTitle", "Test_"+d.getProperties().getStringValue("DocumentTitle"));
        doc.set_MimeType(d.get_MimeType());
        Iterator it=d.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        while(it.hasNext()){
            ContentTransfer elem=(ContentTransfer)it.next();
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //if(elem.get_ContentSize() > 0)
            {
                File f=IOUtils.downloadContentTransfer(elem);
                ByteArrayInputStream is;
                FileInputStream fis=null;
                if(elem.get_ContentSize() > 0){
                    //is = new ByteArrayInputStream(IOUtils.readDocContentFromFile(f));
                    fis=new FileInputStream(f);
                }
                else
                    is = new ByteArrayInputStream(new byte[1]);
                ctNew.setCaptureSource(fis);
                ctNew.set_RetrievalName(f.getName());
                fis.close();
                f.delete();
            }
            cel.add(ctNew);
            
        }
        doc.set_ContentElements(cel);
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        Iterator propIterator=d.getProperties().iterator();

        while(propIterator.hasNext()){
            //try{
                Property p=(Property)propIterator.next();
                /*if(p.getPropertyName().equals("ReleasedVersion") || p.getPropertyName().equals("VersionSeries")
                || p.getPropertyName().equals("DateLastModified") || p.getPropertyName().equals("IsCurrentVersion")
                || p.getPropertyName().equals("Name") || p.getPropertyName().equals("StorageArea")
                || p.getPropertyName().equals("Annotations") )
                    continue;*/
                if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                	
                	System.out.println("-----------------------Properies-----------------------------");
                	System.out.println(p.getPropertyName());
                	System.out.println("-----------------------Properies-----------------------------");
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }
            /*}
            catch(EngineRuntimeException erx){
                continue;
            }*/
        }

        doc.save(RefreshMode.REFRESH);
        
        String folderPath=((Folder)d.get_FoldersFiledIn().iterator().next()).get_PathName();
        Folder destFolder=Factory.Folder.fetchInstance(destOS, folderPath, null);
        if(destFolder == null){
            Factory.Folder.createInstance(destOS, ClassNames.FOLDER);
        }
        return doc.get_Id().toString();
    }

    public static String addRWDocument(Document d,ObjectStore destOS,CEConnection sourceCon,CEConnection destCon) throws Exception{
        Document doc;
        System.out.println("------------------Add Doc2--------->>>>>>>>>>>");
        boolean ignoreDiscipline=false;
        boolean ignoreSubType=false;
        boolean ignoreReportType=false;
        if(d.getClassName().equals("SurveyDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Survey Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ArchDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Architecture");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("CivilDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Civil");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ElecDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Electrical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("MechanicalDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Mechanical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("NonDisciplineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Non Discipline Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("PipelineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("DocumentSubType", "Section of Pipelines Longitudinal");
            ignoreSubType=true;
        }
        else if(d.getClassName().equals("glentest")){
            doc=Factory.Document.createInstance(destOS, "Reports");

            if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
                doc.getProperties().putValue("ReportType", "Pump Test Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
                doc.getProperties().putValue("ReportType", "Geotechnical Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Reports")){
                doc.getProperties().putValue("ReportType", "Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Other Reports")){
                doc.getProperties().putValue("ReportType", "Other Reports");
                ignoreReportType=true;
            }
            //Document Title Field
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Certificate")){
            doc=Factory.Document.createInstance(destOS, "Certificates");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Drawing")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Engineering Calculations")){
            doc=Factory.Document.createInstance(destOS, "EngineeringCalculations");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Engineering Request/Proposal")){
            doc=Factory.Document.createInstance(destOS, "ListSchedules");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Operating and Maintenance Manual")){
            doc=Factory.Document.createInstance(destOS, "Manuals");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Permit")){
            doc=Factory.Document.createInstance(destOS, "Permits");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Pictorial Image")){
            doc=Factory.Document.createInstance(destOS, "PictorialImages");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Procedure")){
            doc=Factory.Document.createInstance(destOS, "Procedures");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Reports")){
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Other Report")){
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Specification")){
            doc=Factory.Document.createInstance(destOS, "Specifications");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType").equals("Transmittal Note")){
            doc=Factory.Document.createInstance(destOS, "TransmittalNotes");
        }
        else{
            doc=Factory.Document.createInstance(destOS, d.getClassName());

        }
        //doc.set_OwnerDocument(d);
        //if(doc.getProperties().isPropertyPresent("FileNumber"))
        {

            String fileNumber=d.getProperties().getStringValue("DocumentTitle");
            if(fileNumber != null){
                if(fileNumber.contains("."))
                    fileNumber=fileNumber.substring(0, fileNumber.lastIndexOf("."));
                doc.getProperties().putValue("FileNumber", fileNumber);
            }
        }
        doc.set_MimeType(d.get_MimeType());

        Iterator propIterator=d.getProperties().iterator();
        sourceCon.startAuth();
        //System.out.println(d.getProperties().getStringValue("DocumentTitle")+" **************************** "+d.getProperties().getStringValue("RandWaterSites"));
        while(propIterator.hasNext()){
            
                Property p=(Property)propIterator.next();
                //System.out.println(p.getPropertyName());
                if(ignoreDiscipline && p.getPropertyName().equals("Discipline"))
                    continue;
                if(ignoreSubType && p.getPropertyName().equals("DocumentSubType"))
                    continue;
                if(ignoreReportType && p.getPropertyName().equals("ReportType"))
                    continue;
                if(p.getPropertyName().equals("DocumentType"))
                    continue;
                if(p.getPropertyName().equals("SecurityPolicy"))
                    continue;
                if(p.getPropertyName().equals("StoragePolicy"))
                    continue;
                if(p.getPropertyName().equals("Owner"))
                    continue;
                if(p.getPropertyName().equals("SecurityParent"))
                    continue;
                if(p.getPropertyName().equals("EntryTemplateObjectStoreName"))
                    continue;
                if(p.getPropertyName().equals("SecurityFolder"))
                    continue;
                if(p.getPropertyName().equals("RMEntityDescription"))
                    continue;
                if(p.getPropertyName().equals("EmailSubject"))
                    doc.getProperties().putValue("SubjectMatter", p.getStringValue());
                else if(p.getPropertyName().equals("RandWaterSites"))
                    doc.getProperties().putValue("Sites", p.getStringListValue());
                else if(p.getPropertyName().equals("PIKNumber"))
                    doc.getProperties().putValue("PIKNumberlegacy", p.getStringValue());
                else if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }

        }

        sourceCon.startAuth();
        Iterator it=d.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        if(!it.hasNext())
            return null;
        boolean hasContent=false;
        while(it.hasNext()){
            ContentTransfer elem=(ContentTransfer)it.next();
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //if(elem.get_ContentSize() > 0)
            {
                File f=IOUtils.downloadContentTransfer(elem);
                if(f==null)
                    continue;
                ByteArrayInputStream is;
                if(elem.get_ContentSize() > 0){
                    is = new ByteArrayInputStream(IOUtils.readDocContentFromFile(f));
                    hasContent=true;
                }
                else
                    is = new ByteArrayInputStream(new byte[1]);
                ctNew.setCaptureSource(is);
                ctNew.set_RetrievalName(f.getName());
                //fis.close();
                f.delete();
            }
            cel.add(ctNew);

        }
        if(!hasContent)
            return null;
        sourceCon.endAuth();
        destCon.startAuth();
        doc.set_ContentElements(cel);
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        /*Iterator propIterator=d.getProperties().iterator();
        sourceCon.startAuth();
        while(propIterator.hasNext()){
                Property p=(Property)propIterator.next();
                if(ignoreDiscipline && p.getPropertyName().equals("Discipline"))
                    continue;
                if(ignoreSubType && p.getPropertyName().equals("Document Subtype"))
                    continue;
                if(ignoreReportType && p.getPropertyName().equals("Report type"))
                    continue;

                if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }
            
        }
        destCon.startAuth();
        doc.save(RefreshMode.REFRESH);
        destCon.endAuth();
*/
        return doc.get_Id().toString();
    }

    public static String addRWDocument2(Document d,ObjectStore destOS,CEConnection sourceCon,CEConnection destCon) throws Exception{
        Document doc;
        System.out.println("------------------Add Doc3--------->>>>>>>>>>>");
        boolean ignoreDiscipline=false;
        boolean ignoreSubType=false;
        boolean ignoreReportType=false;
        //System.out.println(14);

        if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Certificate")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Certificates");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Drawing")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Engineering Calculations")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "EngineeringCalculations");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Engineering Request/Proposal")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "ListSchedules");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Operating and Maintenance Manual")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Manuals");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Permit")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Permits");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pictorial Image")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "PictorialImages");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Procedure")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Procedures");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Reports")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Other Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Specification")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Specifications");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Transmittal Note")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "TransmittalNotes");
        }
        else if(d.getClassName().equals("SurveyDoc")){
           // System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Survey Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ArchDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Architecture");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("CivilDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Civil");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ElecDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Electrical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("MechanicalDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Mechanical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("NonDisciplineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Non Discipline Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("PipelineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("DocumentSubType", "Section of Pipelines Longitudinal");
            ignoreSubType=true;
        }
        else if(d.getClassName().equals("glentest")){
            doc=Factory.Document.createInstance(destOS, "Reports");

            if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
                doc.getProperties().putValue("ReportType", "Pump Test Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
                doc.getProperties().putValue("ReportType", "Geotechnical Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Reports")){
                doc.getProperties().putValue("ReportType", "Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Other Reports")){
                doc.getProperties().putValue("ReportType", "Other Reports");
                ignoreReportType=true;
            }
            //Document Title Field
        }

        else{
            doc=Factory.Document.createInstance(destOS, d.getClassName());

        }
        //System.out.println(15);
        //doc.set_OwnerDocument(d);
        //if(doc.getProperties().isPropertyPresent("FileNumber"))
        {

            String fileNumber=d.getProperties().getStringValue("DocumentTitle");
            if(fileNumber != null){
                if(fileNumber.contains("."))
                    fileNumber=fileNumber.substring(0, fileNumber.lastIndexOf("."));
                doc.getProperties().putValue("FileNumber", fileNumber);
            }
        }
        //System.out.println(16);
        doc.set_MimeType(d.get_MimeType());

        Iterator propIterator=d.getProperties().iterator();
        sourceCon.startAuth();
        //System.out.println(d.getProperties().getStringValue("DocumentTitle")+" **************************** "+d.getProperties().getStringValue("RandWaterSites"));
        //System.out.println(17);
        String propError="";
        while(propIterator.hasNext()){
            try{
                Property p=(Property)propIterator.next();
                //System.out.println(p.getPropertyName());
                if(ignoreDiscipline && p.getPropertyName().equals("Discipline"))
                    continue;
                if(ignoreSubType && p.getPropertyName().equals("DocumentSubType"))
                    continue;
                if(ignoreReportType && p.getPropertyName().equals("ReportType"))
                    continue;
                if(p.getPropertyName().equals("DocumentType"))
                    continue;
                if(p.getPropertyName().equals("SecurityPolicy"))
                    continue;
                if(p.getPropertyName().equals("StoragePolicy"))
                    continue;
                if(p.getPropertyName().equals("Owner"))
                    continue;
                if(p.getPropertyName().equals("SecurityParent"))
                    continue;
                if(p.getPropertyName().equals("EntryTemplateObjectStoreName"))
                    continue;
                if(p.getPropertyName().equals("SecurityFolder"))
                    continue;
                if(p.getPropertyName().equals("RMEntityDescription"))
                    continue;
                if(p.getPropertyName().equals("RecordInformation"))
                    continue;
                if(p.getPropertyName().equals("EmailSubject"))
                    doc.getProperties().putValue("SubjectMatter", p.getStringValue());
                else if(p.getPropertyName().equals("RandWaterSites"))
                    doc.getProperties().putValue("Sites", p.getStringListValue());
                else if(p.getPropertyName().equals("PIKNumber"))
                    doc.getProperties().putValue("PIKNumberlegacy", p.getStringValue());
                else if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }
                p=null;
            }
            catch(Exception ex){
                if(!propError.equals(""))
                    propError+=", ";
                propError+=ex.getMessage();
            }
        }
        propIterator=null;
        //System.out.println(18);
        sourceCon.startAuth();
        Iterator it=d.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        if(!it.hasNext())
            return "-1";
        boolean hasContent=false;
        //System.out.println(19);
        while(it.hasNext()){
          //  System.out.println(30);
            ContentTransfer elem=(ContentTransfer)it.next();
            //System.out.println(31);
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //System.out.println(32);
            //if(elem.get_ContentSize() > 0)
            
                File f=IOUtils.downloadContentTransfer(elem);
              //  System.out.println(33);
                if(f==null)
                    continue;
                //System.out.println(34);
                ByteArrayInputStream is;
                FileInputStream fis=null;
                byte[] content;
                if(elem.get_ContentSize() > 0){
                    //content=IOUtils.readDocContentFromFile(f);
                    //is = new ByteArrayInputStream(content);
                    fis=new FileInputStream(f);
                    hasContent=true;
                }
                else
                    return "-1";
                //System.out.println(35);
                try{
                    ctNew.setCaptureSource(fis);
                }
                catch(Exception ex){
                    hasContent=false;
                }
                //System.out.println(36);
                ctNew.set_RetrievalName(f.getName());
                //System.out.println(37);
                //fis.close();
                //f.delete();
                //System.out.println(38);
            
            cel.add(ctNew);
            content=null;
            elem=null;

        }
        //System.out.println(20);
        if(!hasContent)
            return "-2";
        sourceCon.endAuth();
        destCon.startAuth();
        doc.set_ContentElements(cel);
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        IOUtils.deleteDirectory(new File("docs"));
        //System.out.println(21);
        String docId=doc.get_Id().toString();
        doc = null;
        cel=null;
        System.gc();
        if(propError.equals(""))
            return docId;
        else
            return docId+"_"+propError;
    }

    public static String addRWDocument3(Document d,ObjectStore destOS,CEConnection sourceCon,CEConnection destCon) throws Exception{
        Document doc;
        System.out.println("------------------Add Doc4--------->>>>>>>>>>>");
        Document currDoc=(Document)d.get_CurrentVersion();
        boolean ignoreDiscipline=false;
        boolean ignoreSubType=false;
        boolean ignoreReportType=false;
        //System.out.println(14);

        if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Certificate")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Certificates");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Drawing")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Engineering Calculations")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "EngineeringCalculations");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Engineering Request/Proposal")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "ListSchedules");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Operating and Maintenance Manual")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Manuals");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Permit")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Permits");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pictorial Image")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "PictorialImages");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Procedure")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Procedures");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Reports")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Other Report")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Reports");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Specification")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Specifications");
        }
        else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Transmittal Note")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "TransmittalNotes");
        }
        else if(d.getClassName().equals("SurveyDoc")){
           // System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Survey Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ArchDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Architecture");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("CivilDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Civil");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("ElecDoc")){
            //System.out.println(15);
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Electrical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("MechanicalDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Mechanical Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("NonDisciplineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("Discipline", "Non Discipline Drawings");
            ignoreDiscipline=true;
        }
        else if(d.getClassName().equals("PipelineDoc")){
            doc=Factory.Document.createInstance(destOS, "Drawings");
            doc.getProperties().putValue("DocumentSubType", "Section of Pipelines Longitudinal");
            ignoreSubType=true;
        }
        else if(d.getClassName().equals("glentest")){
            doc=Factory.Document.createInstance(destOS, "Reports");

            if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Pump Test Report")){
                doc.getProperties().putValue("ReportType", "Pump Test Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Geotechnical Report")){
                doc.getProperties().putValue("ReportType", "Geotechnical Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Reports")){
                doc.getProperties().putValue("ReportType", "Reports");
                ignoreReportType=true;
            }
            else if(d.getProperties().isPropertyPresent("DocumentType") && d.getProperties().getStringValue("DocumentType") != null && d.getProperties().getStringValue("DocumentType").equals("Other Reports")){
                doc.getProperties().putValue("ReportType", "Other Reports");
                ignoreReportType=true;
            }
            //Document Title Field
        }

        else{
            doc=Factory.Document.createInstance(destOS, d.getClassName());

        }
        //System.out.println(15);
        //doc.set_OwnerDocument(d);
        //if(doc.getProperties().isPropertyPresent("FileNumber"))
        {

            String fileNumber=d.getProperties().getStringValue("DocumentTitle");
            if(fileNumber != null){
                if(fileNumber.contains("."))
                    fileNumber=fileNumber.substring(0, fileNumber.lastIndexOf("."));
                doc.getProperties().putValue("FileNumber", fileNumber);
            }
        }
        //System.out.println(16);
        doc.set_MimeType(d.get_MimeType());

        Iterator propIterator=d.getProperties().iterator();
        sourceCon.startAuth();
        //System.out.println(d.getProperties().getStringValue("DocumentTitle")+" **************************** "+d.getProperties().getStringValue("RandWaterSites"));
        //System.out.println(17);
        String propError="";
        while(propIterator.hasNext()){
            try{
                Property p=(Property)propIterator.next();
                //System.out.println(p.getPropertyName());
                if(ignoreDiscipline && p.getPropertyName().equals("Discipline"))
                    continue;
                if(ignoreSubType && p.getPropertyName().equals("DocumentSubType"))
                    continue;
                if(ignoreReportType && p.getPropertyName().equals("ReportType"))
                    continue;
                if(p.getPropertyName().equals("DocumentType"))
                    continue;
                if(p.getPropertyName().equals("SecurityPolicy"))
                    continue;
                if(p.getPropertyName().equals("StoragePolicy"))
                    continue;
                if(p.getPropertyName().equals("Owner"))
                    continue;
                if(p.getPropertyName().equals("SecurityParent"))
                    continue;
                if(p.getPropertyName().equals("EntryTemplateObjectStoreName"))
                    continue;
                if(p.getPropertyName().equals("SecurityFolder"))
                    continue;
                if(p.getPropertyName().equals("RMEntityDescription"))
                    continue;
                if(p.getPropertyName().equals("RecordInformation"))
                    continue;
                if(p.getPropertyName().equals("EmailSubject"))
                    doc.getProperties().putValue("SubjectMatter", p.getStringValue());
                else if(p.getPropertyName().equals("RandWaterSites"))
                    doc.getProperties().putValue("Sites", p.getStringListValue());
                else if(p.getPropertyName().equals("PIKNumber"))
                    doc.getProperties().putValue("PIKNumberlegacy", p.getStringValue());
                else if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }
                p=null;
            }
            catch(Exception ex){
                if(!propError.equals(""))
                    propError+=", ";
                propError+=ex.getMessage();
            }
        }
        propIterator=null;
        //System.out.println(18);
        sourceCon.startAuth();
        VersionableSet docs=d.get_Versions();
        Iterator docsIt=docs.iterator();
        Document curr=(Document)docsIt.next();
        Iterator it=curr.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        if(!it.hasNext())
            return null;
        boolean hasContent=false;
        //System.out.println(19);
        while(it.hasNext()){
          //  System.out.println(30);
            ContentTransfer elem=(ContentTransfer)it.next();
            //System.out.println(31);
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //System.out.println(32);
            //if(elem.get_ContentSize() > 0)

                File f=IOUtils.downloadContentTransfer(elem);
              //  System.out.println(33);
                if(f==null)
                    continue;
                //System.out.println(34);
                ByteArrayInputStream is;
                FileInputStream fis=null;
                byte[] content;
                if(elem.get_ContentSize() > 0){
                    //content=IOUtils.readDocContentFromFile(f);
                    //is = new ByteArrayInputStream(content);
                    fis=new FileInputStream(f);
                    hasContent=true;
                }
                else
                    is = new ByteArrayInputStream(new byte[1]);
                //System.out.println(35);
                try{
                ctNew.setCaptureSource(fis);
                }
                catch(Exception ex){
                    hasContent=false;
                }
                //System.out.println(36);
                ctNew.set_RetrievalName(f.getName());
                //System.out.println(37);
                fis.close();
                f.delete();
                //System.out.println(38);

            cel.add(ctNew);
            content=null;
            elem=null;

        }
        sourceCon.endAuth();
        destCon.startAuth();
        doc.set_ContentElements(cel);
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        destCon.endAuth();
        sourceCon.startAuth();

        boolean hasMore=false;
        while(docsIt.hasNext()){
            hasMore=true;
            curr=(Document)docsIt.next();
            if(!curr.get_IsCurrentVersion()){
                if (doc.get_IsCurrentVersion().booleanValue()== false){
                    doc = (Document)doc.get_CurrentVersion();
                }
                sourceCon.endAuth();
                destCon.startAuth();
                doc.checkout(ReservationType.EXCLUSIVE, null, null, null);
                doc.save(RefreshMode.REFRESH);
                Document res=(Document)doc.get_Reservation();
                destCon.endAuth();
                sourceCon.startAuth();


                it=curr.get_ContentElements().iterator();
                cel = Factory.ContentElement.createList();
                if(!it.hasNext())
                    return null;
                hasContent=false;
                //System.out.println(19);
                while(it.hasNext()){
                  //  System.out.println(30);
                    ContentTransfer elem=(ContentTransfer)it.next();
                    //System.out.println(31);
                    ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
                    //System.out.println(32);
                    //if(elem.get_ContentSize() > 0)

                        File f=IOUtils.downloadContentTransfer(elem);
                      //  System.out.println(33);
                        if(f==null)
                            continue;
                        //System.out.println(34);
                        ByteArrayInputStream is;
                        FileInputStream fis=null;
                        byte[] content;
                        if(elem.get_ContentSize() > 0){
                            //content=IOUtils.readDocContentFromFile(f);
                            //is = new ByteArrayInputStream(content);
                            fis=new FileInputStream(f);
                            hasContent=true;
                        }
                        else
                            is = new ByteArrayInputStream(new byte[1]);
                        //System.out.println(35);
                        try{
                        ctNew.setCaptureSource(fis);
                        }
                        catch(Exception ex){
                            hasContent=false;
                        }
                        //System.out.println(36);
                        ctNew.set_RetrievalName(f.getName());
                        //System.out.println(37);
                        fis.close();
                        f.delete();
                        //System.out.println(38);

                    cel.add(ctNew);
                    content=null;
                    elem=null;

                    
                }
                sourceCon.endAuth();
                    destCon.startAuth();
                    res.set_ContentElements(cel);
                    res.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY ,CheckinType.MAJOR_VERSION);
                    res.save(RefreshMode.REFRESH);
                    destCon.endAuth();
                    sourceCon.startAuth();
            }
        }

        /*if(hasMore){
            if (doc.get_IsCurrentVersion().booleanValue()== false){
                doc = (Document)doc.get_CurrentVersion();
            }
            sourceCon.endAuth();
            destCon.startAuth();
            doc.checkout(ReservationType.EXCLUSIVE, null, null, null);
            doc.save(RefreshMode.REFRESH);
            Document res=(Document)doc.get_Reservation();
            destCon.endAuth();
            sourceCon.startAuth();

            it=currDoc.get_ContentElements().iterator();
            cel = Factory.ContentElement.createList();
            if(!it.hasNext())
                return null;
            hasContent=false;
            //System.out.println(19);
            while(it.hasNext()){
              //  System.out.println(30);
                ContentTransfer elem=(ContentTransfer)it.next();
                //System.out.println(31);
                ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
                //System.out.println(32);
                //if(elem.get_ContentSize() > 0)

                    File f=IOUtils.downloadContentTransfer(elem);
                  //  System.out.println(33);
                    if(f==null)
                        continue;
                    //System.out.println(34);
                    ByteArrayInputStream is;
                    FileInputStream fis=null;
                    byte[] content;
                    if(elem.get_ContentSize() > 0){
                        //content=IOUtils.readDocContentFromFile(f);
                        //is = new ByteArrayInputStream(content);
                        fis=new FileInputStream(f);
                        hasContent=true;
                    }
                    else
                        is = new ByteArrayInputStream(new byte[1]);
                    //System.out.println(35);
                    try{
                    ctNew.setCaptureSource(fis);
                    }
                    catch(Exception ex){
                        hasContent=false;
                    }
                    //System.out.println(36);
                    ctNew.set_RetrievalName(f.getName());
                    //System.out.println(37);
         fis.close();
                    f.delete();
                    //System.out.println(38);

                cel.add(ctNew);
                content=null;
                elem=null;

                
            }
            sourceCon.endAuth();
                destCon.startAuth();
                res.set_ContentElements(cel);
                res.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY ,CheckinType.MAJOR_VERSION);
                res.save(RefreshMode.REFRESH);
                destCon.endAuth();
                sourceCon.startAuth();
        }*/
        
        //System.out.println(20);
        if(!hasContent)
            return null;
        String docId=doc.get_Id().toString();
        doc = null;
        cel=null;
        System.gc();
        if(propError.equals(""))
            return docId;
        else
            return docId+"_"+propError;
    }

    public static String addRWRecordDocument(Document d,ObjectStore destOS,CEConnection sourceCon,CEConnection destCon) throws Exception{
        Document doc;
        System.out.println("------------------Add Doc5--------->>>>>>>>>>>");
        //System.out.println(14);

        doc=Factory.Document.createInstance(destOS, "Legacy");
        //System.out.println(15);
        //doc.set_OwnerDocument(d);
        //if(doc.getProperties().isPropertyPresent("FileNumber"))
        {

            String fileNumber=d.getProperties().getStringValue("DocumentTitle");
            if(fileNumber != null){
                if(fileNumber.contains("."))
                    fileNumber=fileNumber.substring(0, fileNumber.lastIndexOf("."));
                doc.getProperties().putValue("FileNumber", fileNumber);
            }
        }
        //System.out.println(16);
        doc.set_MimeType(d.get_MimeType());

        Iterator propIterator=d.getProperties().iterator();
        sourceCon.startAuth();
        String propError="";
        
        //String recordFolderPath="/File Plan/RandWater/COMMUNICATION AND PUBLIC RELATIONS";//path1.substring(0, path1.lastIndexOf("/"));
        
        while(propIterator.hasNext()){
            try{
                Property p=(Property)propIterator.next();
                //System.out.println(p.getPropertyName());
                if(p.getPropertyName().equals("SecurityPolicy"))
                    continue;
                if(p.getPropertyName().equals("StoragePolicy"))
                    continue;
                if(p.getPropertyName().equals("Owner"))
                    continue;
                if(p.getPropertyName().equals("SecurityParent"))
                    continue;
                if(p.getPropertyName().equals("EntryTemplateObjectStoreName"))
                    continue;
                if(p.getPropertyName().equals("SecurityFolder"))
                    continue;
                if(p.getPropertyName().equals("RMEntityDescription"))
                    continue;
                if(p.getPropertyName().equals("RecordInformation"))
                    continue;
                else if(p.getPropertyName().equals("RandWaterSites"))
                    doc.getProperties().putValue("Sites", p.getStringListValue());
                if(p.getPropertyName().equals("EmailSubject"))
                    doc.getProperties().putValue("SubjectMatter", p.getStringValue());
                else if(p.isSettable() && !p.getPropertyName().equals("OwnerDocument")){
                    Object value=p.getObjectValue();
                    if(value instanceof BinaryList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryListValue());
                    else if(value instanceof byte[])
                        doc.getProperties().putValue(p.getPropertyName(), p.getBinaryValue());
                    else if(value instanceof BooleanList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanListValue());
                    else if(value instanceof Boolean)
                        doc.getProperties().putValue(p.getPropertyName(), p.getBooleanValue());
                    else if(value instanceof DateTimeList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeListValue());
                    else if(value instanceof Date)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDateTimeValue());
                    else if(value instanceof DependentObjectList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getDependentObjectListValue());
                    else if(value instanceof EngineObject){
                        doc.getProperties().putValue(p.getPropertyName(), p.getEngineObjectValue());
                    }
                    else if(value instanceof Float64List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64ListValue());
                    else if(value instanceof Double)
                        doc.getProperties().putValue(p.getPropertyName(), p.getFloat64Value());
                    else if(value instanceof IdList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdListValue());
                    else if(value instanceof Id)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIdValue());
                    else if(value instanceof IndependentObjectSet)
                        doc.getProperties().putValue(p.getPropertyName(), p.getIndependentObjectSetValue());
                    else if(value instanceof InputStream)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInputStreamValue());
                    else if(value instanceof Integer32List)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32ListValue());
                    else if(value instanceof Integer)
                        doc.getProperties().putValue(p.getPropertyName(), p.getInteger32Value());
                    else if(value instanceof StringList)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringListValue());
                    else if(value instanceof String)
                        doc.getProperties().putValue(p.getPropertyName(), p.getStringValue());
                }
                p=null;
            }
            catch(Exception ex){
                if(!propError.equals(""))
                    propError+=", ";
                propError+=ex.getMessage();
            }
        }
        propIterator=null;
        //System.out.println(18);
        sourceCon.startAuth();
        Iterator it=d.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        if(!it.hasNext())
            return "-1";
        boolean hasContent=false;
        //System.out.println(19);
        while(it.hasNext()){
          //  System.out.println(30);
            ContentTransfer elem=(ContentTransfer)it.next();
            //System.out.println(31);
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //System.out.println(32);
            //if(elem.get_ContentSize() > 0)

                File f=IOUtils.downloadContentTransfer(elem);
              //  System.out.println(33);
                if(f==null)
                    continue;
                //System.out.println(34);
                ByteArrayInputStream is;
                FileInputStream fis=null;
                byte[] content;
                if(elem.get_ContentSize() > 0){
                    //content=IOUtils.readDocContentFromFile(f);
                    //is = new ByteArrayInputStream(content);
                    fis=new FileInputStream(f);
                    hasContent=true;
                }
                else
                    return "-1";
                //System.out.println(35);
                try{
                ctNew.setCaptureSource(fis);
                }
                catch(Exception ex){
                    hasContent=false;
                }
                //System.out.println(36);
                ctNew.set_RetrievalName(f.getName());
                //System.out.println(37);
                //fis.close();
                //f.delete();
                //System.out.println(38);

            cel.add(ctNew);
            content=null;
            elem=null;

        }
        //System.out.println(20);
        if(!hasContent)
            return "-2";
        boolean recordFailed=false;
        if(d.getProperties().getObjectValue("RecordInformation") != null){
            String path=((Folder)((Document)d.getProperties().getObjectValue("RecordInformation")).get_FoldersFiledIn().iterator().next()).get_PathName();

            String path1=path.substring(0, path.lastIndexOf("/"));
            String path2=path1.substring(0, path1.lastIndexOf("/"));
            String mappedPath=IOUtils.getRecordMapping(path2.substring(path2.indexOf("File Plan")+10, path2.length()));
            if(mappedPath.equals("SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities")){
                mappedPath="SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities";
            }
            String recordPath="/Records Management/File Plan/"+mappedPath;
            String folderPath="/File Plan/"+mappedPath;

            /*String recordPath="/Records Management/File Plan"+path2.substring(path2.indexOf("File Plan")+9, path2.length());
            String folderPath="/File Plan"+path2.substring(path1.indexOf("File Plan")+9, path2.length());
            if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Blackberry Implementation")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Cellular Phones/Blackberry Implementation";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Cellular Phones/Blackberry Implementation";
            }
            else if(folderPath.equals("/File Plan/RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships")){
                recordPath="/Records Management/File Plan/PROTECTION OF RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships";
                folderPath="/File Plan/PROTECTION OF RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships";
            }
            else if(folderPath.equals("/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS - COSATU")){
                recordPath="/Records Management/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS (COSATU)";
                folderPath="/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS (COSATU)";
            }
            else if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ELECTRICITY SUPPLY TO PUMPING STATIONS AND DEPOTS - ZUIKERBOSCH P.S")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ZUIKERBOSCH";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ZUIKERBOSCH";
            }
            else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Other Committees - Meetings and Minutes (copies of minutes)/Executive Committee")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROL/Other Committees Meetings and Minutes (copies of minutes)";
                folderPath="/File Plan/ORGANISATION AND CONTROL/Other Committees Meetings and Minutes (copies of minutes)";
            }
            else if(folderPath.equals("/File Plan/RETAIL WATER OPERATIONS/Retail Sanitation")){
                recordPath="/Records Management/File Plan/RETAIL WATER/Retail Sanitation";
                folderPath="/File Plan/RETAIL WATER/Retail Sanitation";
            }
            else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Other Committees - Meetings and Minutes (copies of minutes)/Tender Committee")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROLOther Committees Meetings and Minutes  (copies of minutes)";
                folderPath="/File Plan/ORGANISATION AND CONTROLOther Committees Meetings and Minutes  (copies of minutes)";
            }


            else if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ELECTRICITY SUPPLY TO PUMPING STATIONS AND DEPOTS - ZUIKERBOSCH P.S")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/Zuikerbosch";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/Zuikerbosch";

            }
             else if(folderPath.equals("/File Plan/RETAIL WATER OPERATIONS/Client")){
                recordPath="/Records Management/File Plan/RETAIL WATER/Client";
                folderPath="/File Plan/RETAIL WATER/Client";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/PRIVATE SECURITY INDUSTRY REGULATORY AUTHORITY")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/Private Security Industry Regulatory Authority (PSIRA)";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/Private Security Industry Regulatory Authority (PSIRA)";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/SOUTH AFRICAN ASSOCIATION OF WATER BOARDS")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/ Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/ Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT GOVERNANCE AND COMPLIANCE")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT Governance Compliance";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT Governance Compliance";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Information Services/IT VD OT (INFORMATION TECHNOLOGY VD OPERATIONS TECHNOLOGY)")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT vs OT (Information Technology vs Operations Technology)";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT vs OT (Information Technology vs Operations Technology)";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/POLICIES, RECORDS MANAGEMENT & CONTROL")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Policies";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Policies";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Records Management and Control/POST OFFICE - SERVICES,RECORDS MANAGEMENT & CONTROL")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Post Office";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Post Office";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/SERVICES AND ADMINISTRATION-RECORDS MANAGEMENT AND CONTROL - COURIER SERVICE")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Courier Service";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Courier Service";
            }
             else if(folderPath.equals("/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply")){
                recordPath="/Records Management/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply (also refer to Main Series 9)";
                folderPath="/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply (also refer to Main Series 9)";
            }
             else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder’s Compact")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder's Compact";
                folderPath="/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder's Compact";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Building Management/SERVICE AND ADMINISTRATION-BUILDING MANAGEMENT - FARM RIETVLEI 101 IR ( HEAD OFFICE BUILDING)")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Building Management/Farm Rietvlei 101 I. R.";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Building Management/Farm Rietvlei 101 I. R.";
            }*/
            Folder recordFolder=null;
            try{
                recordFolder=Factory.Folder.fetchInstance(destCon.fetchOS("FPOSRW"), recordPath, null);
            }
            catch(Exception ex){
                ex.printStackTrace();
                recordFailed=true;
                return ex.getMessage()+"_RECORDERROR";
            }
            //Folder recordFolder2=Factory.Folder.fetchInstance(destCon.fetchOS("FPOSRW"), "/Records Management/File Plan/RandWater/COMMUNICATION AND PUBLIC RELATIONS", null);
            //System.out.println(recordFolder);
            //System.out.println(recordFolder2);
            doc.getProperties().putValue("RecordFolderPath", folderPath);
        }

        sourceCon.endAuth();
        destCon.startAuth();
        doc.set_ContentElements(cel);
        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        IOUtils.deleteDirectory(new File("docs"));
        //System.out.println(21);
        String docId=doc.get_Id().toString();
        doc = null;
        cel=null;
        System.gc();
        if(propError.equals("")){
            if(recordFailed)
                return docId+"_RECORDERROR";
            else
                return docId;
        }
        else
            return docId+"_"+propError;
    }

    public static String addRWRecordDocument2(Document d,ObjectStore destOS,CEConnection sourceCon,CEConnection destCon) throws Exception{
        Document doc;
        System.out.println("------------------Add Doc6--------->>>>>>>>>>>");

        //System.out.println(14);

        doc=Factory.Document.createInstance(destOS, "Legacy");
        //System.out.println(15);
        //doc.set_OwnerDocument(d);
        //if(doc.getProperties().isPropertyPresent("FileNumber"))
        {

            String fileNumber=d.getProperties().getStringValue("DocumentTitle");
            if(fileNumber != null){
                if(fileNumber.contains("."))
                    fileNumber=fileNumber.substring(0, fileNumber.lastIndexOf("."));
                doc.getProperties().putValue("FileNumber", fileNumber);
            }
        }
        sourceCon.startAuth();
        Iterator it=d.get_ContentElements().iterator();
        ContentElementList cel = Factory.ContentElement.createList();
        if(!it.hasNext())
            return "-1";
        boolean hasContent=false;
        //System.out.println(19);
        while(it.hasNext()){
          //  System.out.println(30);
            ContentTransfer elem=(ContentTransfer)it.next();
            //System.out.println(31);
            ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
            //System.out.println(32);
            //if(elem.get_ContentSize() > 0)

                File f=IOUtils.downloadContentTransfer(elem);
              //  System.out.println(33);
                if(f==null)
                    continue;
                //System.out.println(34);
                ByteArrayInputStream is;
                FileInputStream fis=null;
                byte[] content;
                if(elem.get_ContentSize() > 0){
                    //content=IOUtils.readDocContentFromFile(f);
                    //is = new ByteArrayInputStream(content);
                    fis=new FileInputStream(f);
                    hasContent=true;
                }
                else
                    return "-1";
                //System.out.println(35);
                try{
                ctNew.setCaptureSource(fis);
                }
                catch(Exception ex){
                    hasContent=false;
                }
                //System.out.println(36);
                ctNew.set_RetrievalName(f.getName());
                //System.out.println(37);
                try{
                //f.delete();
                    //fis.close();
                   //f.delete();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                //System.out.println(38);

            cel.add(ctNew);
            content=null;
            elem=null;

        }
        //System.out.println(20);
        if(!hasContent)
            return "-2";
        boolean recordFailed=false;
        if(d.getProperties().getObjectValue("RecordInformation") != null){
            String path=((Folder)((Document)d.getProperties().getObjectValue("RecordInformation")).get_FoldersFiledIn().iterator().next()).get_PathName();

            String path1=path.substring(0, path.lastIndexOf("/"));
            String path2=path1.substring(0, path1.lastIndexOf("/"));

            String mappedPath=IOUtils.getRecordMapping(path2.substring(path2.indexOf("File Plan")+10, path2.length()));
            if(mappedPath.equals("SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities")){
                mappedPath="SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities";
            }

            String recordPath="/Records Management/File Plan/"+mappedPath;
            String folderPath="/File Plan/"+mappedPath;
            /*
            if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Blackberry Implementation")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Cellular Phones/Blackberry Implementation";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Telecommunications/Cellular Phones/Blackberry Implementation";
            }
            else if(folderPath.equals("/File Plan/RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships")){
                recordPath="/Records Management/File Plan/PROTECTION OF RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships";
                folderPath="/File Plan/PROTECTION OF RAND WATER PIPELINES AND SERVICES/Affecting Rand Water's Services/Establishment of townships/Individual Townships";
            }
            else if(folderPath.equals("/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS - COSATU")){
                recordPath="/Records Management/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS (COSATU)";
                folderPath="/File Plan/HUMAN RESOURCES/Labour Relations/Liaison with Trade Unions/CONGRESS OF SOUTH AFRICAN TRADE UNIONS (COSATU)";
            }
            else if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ELECTRICITY SUPPLY TO PUMPING STATIONS AND DEPOTS - ZUIKERBOSCH P.S")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ZUIKERBOSCH";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ZUIKERBOSCH";
            }
            else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Other Committees - Meetings and Minutes (copies of minutes)/Executive Committee")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROL/Other Committees Meetings and Minutes (copies of minutes)";
                folderPath="/File Plan/ORGANISATION AND CONTROL/Other Committees Meetings and Minutes (copies of minutes)";
            }
            else if(folderPath.equals("/File Plan/RETAIL WATER OPERATIONS/Retail Sanitation")){
                recordPath="/Records Management/File Plan/RETAIL WATER/Retail Sanitation";
                folderPath="/File Plan/RETAIL WATER/Retail Sanitation";
            }
            else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Other Committees - Meetings and Minutes (copies of minutes)/Tender Committee")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROLOther Committees Meetings and Minutes  (copies of minutes)";
                folderPath="/File Plan/ORGANISATION AND CONTROLOther Committees Meetings and Minutes  (copies of minutes)";
            }


            else if(folderPath.equals("/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/ELECTRICITY SUPPLY TO PUMPING STATIONS AND DEPOTS - ZUIKERBOSCH P.S")){
                recordPath="/Records Management/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/Zuikerbosch";
                folderPath="/File Plan/INSTALLATIONS AND PLANTS/Electricity/Supply to Pumping Stations and Depots/Zuikerbosch";

            }
             else if(folderPath.equals("/File Plan/RETAIL WATER OPERATIONS/Client")){
                recordPath="/Records Management/File Plan/RETAIL WATER/Client";
                folderPath="/File Plan/RETAIL WATER/Client";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/PRIVATE SECURITY INDUSTRY REGULATORY AUTHORITY")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/Private Security Industry Regulatory Authority (PSIRA)";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/Private Security Industry Regulatory Authority (PSIRA)";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/Membership of Councils, Institutions, Committees, Societies and Other Bodies/SOUTH AFRICAN ASSOCIATION OF WATER BOARDS")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/ Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Administrative Matters/ Membership of Councils, Institutions, Committees, Societies and-Membership of Councils, Institutions, Committees, Societies and Other Bodies/South African Association of Water Utilities-South African Association of Water Utilities";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT GOVERNANCE AND COMPLIANCE")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT Governance Compliance";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT Governance Compliance";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Information Services/IT VD OT (INFORMATION TECHNOLOGY VD OPERATIONS TECHNOLOGY)")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT vs OT (Information Technology vs Operations Technology)";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Information Services/IT vs OT (Information Technology vs Operations Technology)";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/POLICIES, RECORDS MANAGEMENT & CONTROL")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Policies";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Policies";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Records Management and Control/POST OFFICE - SERVICES,RECORDS MANAGEMENT & CONTROL")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Post Office";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Post Office";
            }
             else if(folderPath.equals("/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/SERVICES AND ADMINISTRATION-RECORDS MANAGEMENT AND CONTROL - COURIER SERVICE")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Courier Service";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Records Management and Control/Courier Service";
            }
             else if(folderPath.equals("/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply")){
                recordPath="/Records Management/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply (also refer to Main Series 9)";
                folderPath="/File Plan/STRATEGIC CUSTOMER PARTNERSHIPS/Water Supply (also refer to Main Series 9)";
            }
             else if(folderPath.equals("/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder’s Compact")){
                recordPath="/Records Management/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder's Compact";
                folderPath="/File Plan/ORGANISATION AND CONTROL/Strategic Planning/Shareholder's Compact";
            }
             else if(folderPath.equals("/File Plan/6-SERVICES AND ADMINISTRATION/Services/Building Management/SERVICE AND ADMINISTRATION-BUILDING MANAGEMENT - FARM RIETVLEI 101 IR ( HEAD OFFICE BUILDING)")){
                recordPath="/Records Management/File Plan/SERVICES AND ADMINISTRATION/Services/Building Management/Farm Rietvlei 101 I. R.";
                folderPath="/File Plan/SERVICES AND ADMINISTRATION/Services/Building Management/Farm Rietvlei 101 I. R.";
            }*/
            Folder recordFolder=null;
            try{
                recordFolder=Factory.Folder.fetchInstance(destCon.fetchOS("FPOSRW"), recordPath, null);

            }
            catch(Exception ex){
                ex.printStackTrace();
                recordFailed=true;
                return ex.getMessage()+"_RECORDERROR";
            }
            //Folder recordFolder2=Factory.Folder.fetchInstance(destCon.fetchOS("FPOSRW"), "/Records Management/File Plan/RandWater/COMMUNICATION AND PUBLIC RELATIONS", null);
            //System.out.println(recordFolder);
            //System.out.println(recordFolder2);
            doc.getProperties().putValue("RecordFolderPath", folderPath);
        }

        sourceCon.endAuth();
        destCon.startAuth();
        doc.set_ContentElements(cel);
        //doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,CheckinType.MAJOR_VERSION);
        //doc.save(RefreshMode.REFRESH);
        IOUtils.deleteDirectory(new File("docs"));
        //System.out.println(21);
        String docId="0";//doc.get_Id().toString();
        doc = null;
        cel=null;
        System.gc();
        if(recordFailed)
            return docId+"_RECORDERROR";
        else
            return docId;

    }
}