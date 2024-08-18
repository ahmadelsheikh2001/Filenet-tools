package FileNet;

import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;

public class FileNetFetch {

    public static CustomObject fetchCustomObject(String documentId) {
        ObjectStore os = FileNetConnections.Connect();
        CustomObject customObj = Factory.CustomObject.fetchInstance(os, documentId, null);
        return customObj;
    }
}
