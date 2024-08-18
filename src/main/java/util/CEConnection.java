package util;

import java.util.Vector;

import javax.security.auth.Subject;

import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;

public class CEConnection
{
	private Connection con;
	private Domain dom;
	private String domainName;
	private ObjectStoreSet ost;
	private Vector osnames;
	private boolean isConnected;
	private UserContext uc;
    public Subject sub;
    private String userName;
    private String password;
    private String stanza;

    public String getPassword() {
        return password;
    }

    public String getStanza() {
        return stanza;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStanza(String stanza) {
        this.stanza = stanza;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public CEConnection(){
        con = null;
		uc = UserContext.get();
		dom = null;
		domainName = null;
		ost = null;
		osnames = new Vector();
		isConnected = false;
    }

    public Connection getCon() {
        return con;
    }
    public Domain fetchDomain()
    {
        dom = Factory.Domain.fetchInstance(con, null, null);
        return dom;
    }

    public ObjectStore fetchOS(String name)
    {
        ObjectStore os = Factory.ObjectStore.fetchInstance(dom, name, null);
        return os;
    }
	
    public void establishConnection(String userName, String password, String stanza, String uri)
    {
        try{
            con = Factory.Connection.getConnection(uri);
            sub = UserContext.createSubject(con,userName,password,stanza);
            uc.pushSubject(sub);
            dom = fetchDomain();
            domainName = dom.get_Name();
            ost = getOSSet();
            isConnected = true;
        }
        catch(Exception ex){
            isConnected=false;
            CustomLogging.log("An exception occurred: " + ex.getMessage());
            System.out.print(ex.getMessage()+"\n");
            // ex.printStackTrace();
        }
    }

    public ObjectStoreSet getOSSet()
    {
        ost = dom.get_ObjectStores();
        return ost;
    }

    public boolean isConnected()
	{
		return isConnected;
	}

    public String getDomainName()
    {
        return domainName;
    }

    public void startAuth(){
        sub = UserContext.createSubject(con,userName,password,stanza);
        UserContext.get().pushSubject(sub);
    }

    public void endAuth(){
        UserContext.get().popSubject();
    }
}