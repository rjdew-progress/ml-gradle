package com.rjrudin.marklogic.mgmt.groups;

import com.rjrudin.marklogic.mgmt.AbstractResourceManager;
import com.rjrudin.marklogic.mgmt.ManageClient;

public class GroupManager extends AbstractResourceManager {

    public GroupManager(ManageClient manageClient) {
        super(manageClient);
    }

    @Override
    protected boolean useAdminUser() {
        return true;
    }

    @Override
    public boolean delete(String payload) {
        String resourceId = getResourceId(payload);
        if (resourceId != null && resourceId.toUpperCase().equals("DEFAULT")) {
            return false;
        }
        return super.delete(payload);
    }
}
