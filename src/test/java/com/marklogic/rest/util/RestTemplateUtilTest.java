package com.marklogic.rest.util;

import com.marklogic.junit.BaseTestHelper;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Many of these tests are just smoke tests that can be used to inspect logging as well. Ideally, they can soon depend
 * on the Manage server being configured to use SSL. Although testing the use of the default keystore would likely
 * still be out of scope due to the difficulty of using a certificate signed by an authority that's trusted by the
 * default keystore.
 */
public class RestTemplateUtilTest extends BaseTestHelper {

	private boolean configurerInvoked = false;
	private ManageConfig manageConfig = new ManageConfig();

	@Test
	@Deprecated
	public void configurerList() {
		assertEquals(4, RestTemplateUtil.DEFAULT_CONFIGURERS.size());

		assertFalse(configurerInvoked);
		HttpClientBuilderConfigurer configurer = (restConfig, builder) -> {
			logger.info("Just a test of adding a configurer");
			configurerInvoked = true;
			return builder;
		};

		new ManageClient(manageConfig);
		assertFalse(configurerInvoked, "The configurer should not be invoked now that ManageClient uses OkHttp " +
			"instead of Apache HTTP");

		RestTemplateUtil.newRestTemplate(manageConfig, configurer);
		assertTrue(configurerInvoked, "The configurer should have been invoked since a deprecated newRestTemplate " +
			"method was used that still honors HttpClientBuilderConfigurer instances");
	}

	@Test
	public void configureSimpleSsl() {
		manageConfig.setConfigureSimpleSsl(true);
		new ManageClient(manageConfig);
	}

	@Test
	public void simpleSslWithCustomProtocol() {
		manageConfig.setConfigureSimpleSsl(true);
		manageConfig.setSslProtocol("SSLv3");
		new ManageClient(manageConfig);
	}

	@Test
	public void simpleSslWithInvalidProtocol() {
		manageConfig.setConfigureSimpleSsl(true);
		manageConfig.setSslProtocol("invalid");
		try {
			new ManageClient(manageConfig);
			fail("Expected failure due to invalid protocol");
		} catch (Exception ex) {
			logger.info("Caught expected exception: " + ex.getMessage());
		}
	}

	@Test
	public void customSslContextAndHostnameVerifier() throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();

		manageConfig.setSslContext(sslContext);
		manageConfig.setHostnameVerifier(new AllowAllHostnameVerifier());

		new ManageClient(manageConfig);
	}

	@Test
	public void useDefaultKeystore() {
		manageConfig.setUseDefaultKeystore(true);
		new ManageClient(manageConfig);
	}

	@Test
	public void defaultKeystoreWithInvalidProtocol() {
		manageConfig.setUseDefaultKeystore(true);
		manageConfig.setSslProtocol("invalid");
		try {
			new ManageClient(manageConfig);
			fail("Expected failure due to invalid protocol");
		} catch (Exception ex) {
			logger.info("Caught expected exception: " + ex.getMessage());
		}
	}

	@Test
	public void defaultKeystoreWithInvalidAlgorithm() {
		manageConfig.setUseDefaultKeystore(true);
		manageConfig.setTrustManagementAlgorithm("invalid");
		try {
			new ManageClient(manageConfig);
			fail("Expected failure due to invalid algorithm");
		} catch (Exception ex) {
			logger.info("Caught expected exception: " + ex.getMessage());
		}
	}
}
