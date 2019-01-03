package com.marklogic.appdeployer.command.servers;

import java.io.File;

import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.ConfigDir;
import com.marklogic.appdeployer.command.appservers.DeployOtherServersCommand;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.mgmt.resource.appservers.ServerManager;

/**
 * Main purpose of these tests is to ensure that we wait properly after deleting a server.
 */
public class ManageOtherServerTest extends AbstractAppDeployerTest {

    @Test
    public void updateMainAndRestRestApiServers() {
        // Create some other databases that have to be deleted to ensure we wait for a restart after deleting the ODBC
        // server
        initializeAppDeployer(new DeployContentDatabasesCommand(1), new DeployOtherDatabasesCommand(), new DeployOtherServersCommand());
        try {
            appDeployer.deploy(appConfig);
        } finally {
            appDeployer.undeploy(appConfig);
        }
    }

    @Test
    public void odbcAndXdbcServers() {
        appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/other-servers")));

        ServerManager mgr = new ServerManager(manageClient);

        initializeAppDeployer(new DeployOtherServersCommand());
        appDeployer.deploy(appConfig);

        assertTrue(mgr.exists("sample-app-xdbc"));
        assertTrue(mgr.exists("sample-app-odbc"));
        assertFalse(
                "The command should ignore the rest-api-server.json file, as that's processed by DeployRestApiServersCommand",
                mgr.exists("sample-app"));

        appDeployer.undeploy(appConfig);

        assertFalse(mgr.exists("sample-app-xdbc"));
        assertFalse(mgr.exists("sample-app-odbc"));
        assertFalse(mgr.exists("sample-app"));
    }

    @Test
    public void ignoreOdbcServer() {
        appConfig.setConfigDir(new ConfigDir(new File("src/test/resources/sample-app/other-servers")));

        ServerManager mgr = new ServerManager(manageClient);

        DeployOtherServersCommand c = new DeployOtherServersCommand();
        c.setFilenamesToIgnore("odbc-server.json");
        initializeAppDeployer(c);

        appDeployer.deploy(appConfig);

        final String message = "Both the ODBC and REST API server files should have been ignored";
        assertTrue(mgr.exists("sample-app-xdbc"));
        assertFalse(message, mgr.exists("sample-app-odbc"));
        assertFalse(message, mgr.exists("sample-app"));

        appDeployer.undeploy(appConfig);

        assertFalse(mgr.exists("sample-app-xdbc"));
        assertFalse(mgr.exists("sample-app-odbc"));
        assertFalse(mgr.exists("sample-app"));
    }
}
