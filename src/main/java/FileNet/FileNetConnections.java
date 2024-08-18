package FileNet;

import util.CEConnection;
import util.PropertiesHelper;
import com.filenet.api.core.ObjectStore;

public class FileNetConnections  {
    static PropertiesHelper pHelp = PropertiesHelper.getInstance("Application.properties");

    public static ObjectStore userConnection(String userName, String password) {
        CEConnection conn = new CEConnection();

        try {
          
            
            conn.establishConnection(userName, password, pHelp.getProperty("stanza"), pHelp.getProperty("uri"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String obj = pHelp.getProperty("objectStore");
        ObjectStore os = conn.fetchOS(obj);
        return os;
    }

    public static ObjectStore Connect() {
        CEConnection conn = new CEConnection();
        try {
        	  System.out.println("Connecting with:");
              System.out.println("User: " + pHelp.getProperty("user"));
              System.out.println("Password: " + pHelp.getProperty("password"));
              System.out.println("Stanza: " + pHelp.getProperty("stanza"));
              System.out.println("URI: " + pHelp.getProperty("uri"));

            conn.establishConnection(pHelp.getProperty("user"), pHelp.getProperty("password"), pHelp.getProperty("stanza"), pHelp.getProperty("uri"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String obj = pHelp.getProperty("objectStore");
        ObjectStore os = conn.fetchOS(obj);
        return os;
    }
}
