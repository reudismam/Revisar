/**
 * CIMClientXML.java
 *
 * (C) Copyright IBM Corp. 2005, 2009
 *
 * THIS FILE IS PROVIDED UNDER THE TERMS OF THE ECLIPSE PUBLIC LICENSE 
 * ("AGREEMENT"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS FILE 
 * CONSTITUTES RECIPIENTS ACCEPTANCE OF THE AGREEMENT.
 *
 * You can obtain a current copy of the Eclipse Public License from
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * @author: Roberto Pineiro, IBM, roberto.pineiro@us.ibm.com  
 * @author: Chung-hao Tan, IBM ,chungtan@us.ibm.com
 * 
 * 
 * Change History
 * Flag       Date        Prog         Description
 *------------------------------------------------------------------------------- 
 *   18045    2005-08-10  pineiro5     Some code clean up in multiple points
 * 1483394    2006-05-15  lupusalex    Indication listener threads don't close
 * 1488846    2006-05-15  mszermutzky  Bad format Locale information send to CIM Server
 * 1381768    2006-05-29  thschaef     CIMClient.close() faulty on HTTPClientPool
 * 1498130    2006-05-31  lupusalex    Selection of xml parser on a per connection basis
 * 1514405    2006-07-03  lupusalex    getInstance() returns a keyless CIMInstance
 * 1498130    2006-07-17  lupusalex    review issues
 * 1535756    2006-08-07  lupusalex    Make code warning free
 * 1365082    2006-09-05  ebak         Possible bugs in namespace creation
 * 1552457    2006-09-13  taphorn      NullPointer Exception while authenticating without PW
 * 1574345    2006-10-11  lupusalex    Client fails w/ NPE when processing chunked response
 * 1516242    2006-11-27  lupusalex    Support of OpenPegasus local authentication
 * 1637546    2007-01-27  lupusalex    CIMEnumerationImpl has faulty close function
 * 1646434    2007-01-28  lupusalex    CIMClient close() invalidates all it's enumerations
 * 1702751    2007-04-18  lupusalex    Further leak prevention
 * 1954069    2008-05-14  blaschke-oss connection leak and exception in CIMClientXML
 * 1931266    2008-06-25  raman_arora  M-POST not supported in java-client
 * 1984588    2008-07-31  blaschke-oss HttpClient not closed on cimclient close
 * 2382765    2008-12-03  blaschke-oss HTTP header field Accept-Language does not include *
 * 2807325    2009-06-22  blaschke-oss Change licensing from CPL to EPL
 */

package org.sblim.wbem.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sblim.wbem.cim.CIMAuthenticationException;
import org.sblim.wbem.cim.CIMClass;
import org.sblim.wbem.cim.CIMDataType;
import org.sblim.wbem.cim.CIMException;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMNameSpace;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMQualifierType;
import org.sblim.wbem.cim.CIMTransportException;
import org.sblim.wbem.cim.CIMValue;
import org.sblim.wbem.client.indications.CIMEventDispatcher;
import org.sblim.wbem.client.indications.CIMIndicationHandler;
import org.sblim.wbem.client.indications.CIMIndicationListenertList;
import org.sblim.wbem.client.indications.CIMListener;
import org.sblim.wbem.client.operations.CIMAssociatorNamesOp;
import org.sblim.wbem.client.operations.CIMAssociatorsOp;
import org.sblim.wbem.client.operations.CIMCreateNameSpaceOp;
import org.sblim.wbem.client.operations.CIMDeleteNameSpaceOp;
import org.sblim.wbem.client.operations.CIMEnumClassNamesOp;
import org.sblim.wbem.client.operations.CIMEnumClassesOp;
import org.sblim.wbem.client.operations.CIMEnumInstanceNamesOp;
import org.sblim.wbem.client.operations.CIMEnumInstancesOp;
import org.sblim.wbem.client.operations.CIMEnumNameSpaceOp;
import org.sblim.wbem.client.operations.CIMEnumQualifierTypesOp;
import org.sblim.wbem.client.operations.CIMExecQueryOp;
import org.sblim.wbem.client.operations.CIMGetClassOp;
import org.sblim.wbem.client.operations.CIMGetInstanceOp;
import org.sblim.wbem.client.operations.CIMGetPropertyOp;
import org.sblim.wbem.client.operations.CIMGetQualifierTypeOp;
import org.sblim.wbem.client.operations.CIMInvokeMethodOp;
import org.sblim.wbem.client.operations.CIMOperation;
import org.sblim.wbem.client.operations.CIMReferenceNamesOp;
import org.sblim.wbem.client.operations.CIMReferencesOp;
import org.sblim.wbem.http.AuthInfo;
import org.sblim.wbem.http.AuthorizationHandler;
import org.sblim.wbem.http.HttpClientPool;
import org.sblim.wbem.http.HttpConnectionHandler;
import org.sblim.wbem.http.HttpHeader;
import org.sblim.wbem.http.HttpHeaderParser;
import org.sblim.wbem.http.HttpServerConnection;
import org.sblim.wbem.http.HttpUrlConnection;
import org.sblim.wbem.http.io.DebugInputStream;
import org.sblim.wbem.util.Benchmark;
import org.sblim.wbem.util.SessionProperties;
import org.sblim.wbem.xml.CIMClientXML_HelperImpl;
import org.sblim.wbem.xml.CIMResponse;
import org.sblim.wbem.xml.CIMXMLParserImpl;
import org.sblim.wbem.xml.XMLDefaultHandlerImpl;
import org.sblim.wbem.xml.parser.XMLPullParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CIMClientXML implements CIMOMHandle {

	private static final String CLASSNAME = "org.sblim.wbem.client.CIMClientXML";

	private URI iUri;

	private CIMIndicationListenertList iIndicationClient;

	private HttpServerConnection iIndicationServer = null;

	private Object iLock = new Object();

	private int iCounter = 0; // TODO this is not theadsafe

	private int iNsCounter = 1;

	private CIMClientXML_HelperImpl iXmlHelper = null;

	private CIMNameSpace iNamespace;

	private HttpUrlConnection iConnection;

	private boolean iUseMPost = true;
	
	private boolean iMPostFailed = false;
	
	private volatile long iMPostFailTime = 0;
	
	private volatile long iCurrentTime = 0;

	private boolean iUseHttp11 = true;

	private Locale iLocale = Locale.getDefault();

	private HttpClientPool iHttpClientPool = new HttpClientPool();

	private AuthorizationHandler iAuthorizationHandler;

	private String iAuthorization;

	private Logger iLogger = null;

	private SessionProperties iSessionProperties;

	public CIMClientXML(CIMNameSpace pNamespace, Principal pPrincipal, Object pCredential, String pProtocol)
			throws CIMException {
		this(pNamespace, pPrincipal, pCredential, pProtocol, null);
	}

	public CIMClientXML(CIMNameSpace pNamespace, Principal pPrincipal, Object pCredential, String pProtocol,
			SessionProperties pProperties) throws CIMException {
		try {
			init(pNamespace, pPrincipal, pCredential, pProtocol, pProperties);
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void init(CIMNameSpace pNamespace, Principal pPrincipal, Object pCredential, String pProtocol,
			SessionProperties pProperties) throws CIMException {
		String methodName = "init";
		iLogger = SessionProperties.getGlobalProperties().getLogger();
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, "init(CIMNameSpace, Principal, Object)", new Object[] { pNamespace,
					pPrincipal, pCredential });
		}

		try {

			iSessionProperties = (pProperties != null) ? pProperties : SessionProperties
					.getGlobalProperties();
			iHttpClientPool.setSessionProperties(pProperties);

			iXmlHelper = new CIMClientXML_HelperImpl();

			if (pNamespace == null) { throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
					"null namespace argument"); }

			iNamespace = (CIMNameSpace) pNamespace.clone(); // create a local copy
			iUri = iNamespace.getHostURI();
			if (iUri == null) { throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
					"Malformed URI is equal to NULL"); }

			AuthInfo authInfo = AuthInfo.createAuthorizationInfo(iSessionProperties
					.getHttpAuthenticationModule(), Boolean.FALSE, iNamespace.getHost(), iNamespace
					.getPort(), null, null, null);

			// if the principal and the credential is not given we can use
			// default values. This can be done
			// in the GlobalProperties or the cim.defaults
			String usePrincipal = "";
			char[] useCredential = new char[] {};
			
			
			if (pCredential != null && (pCredential instanceof PasswordCredential)) {
				if (((PasswordCredential)pCredential).getUserPassword() != null) {
					useCredential = ((PasswordCredential)pCredential).getUserPassword();
				}
			}

			if (pPrincipal != null && pPrincipal.getName() != null) {
				usePrincipal = pPrincipal.getName();
			}

			boolean defaultAuthEnabled = iSessionProperties.isCredentialsDefaultEnabled();
			
			if (iLogger.isLoggable(Level.FINER)) 
				iLogger.log(Level.FINER, "Default authorization "+(defaultAuthEnabled?"IS":"NOT")+" enabled !");
			
			if ( (usePrincipal.equals("") || useCredential.equals(new char[]{})) && defaultAuthEnabled) {
				if (iLogger.isLoggable(Level.FINER)) 
					iLogger.log(Level.FINER, "Principal and/or Credential not set - using default authorization!");
				
				usePrincipal = iSessionProperties.getDefaultPrincipal();
				useCredential = iSessionProperties.getDefaultCredentials().toCharArray();
			}

			authInfo.setCredentials(new PasswordAuthentication(usePrincipal, useCredential));

			iAuthorizationHandler = new AuthorizationHandler();
			iAuthorizationHandler.addAuthorizationInfo(authInfo);
			iIndicationClient = new CIMIndicationListenertList();

			iXmlHelper = new CIMClientXML_HelperImpl();

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error occured during initilizing", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error occured during initilizing", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	protected void getCIMOMCapabilities() {
	// TODO
	}

	public void setLocale(Locale pLocale) {
		String methodName = "setLocale(Locale)";

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, pLocale);
		}

		try {
			if (pLocale == null) {
				CIMException e = new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
						"null Locale object");
				if (iLogger.isLoggable(Level.INFO)) {
					iLogger.log(Level.INFO, "Invalid Locale object - null reference", e);
				}
				throw e;
			}
			iLocale = pLocale;

		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public Locale getLocale() {
		String methodName = "getLocale()";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, iLocale);
			iLogger.exiting(CLASSNAME, methodName, iLocale);
		}
		return iLocale;
	}

	public void useHttp11(boolean pValue) {
		String methodName = "useHttp11(boolean)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, Boolean.valueOf(pValue));
		}

		iUseHttp11 = pValue;

		if (!iUseHttp11) {
			iUseMPost = false;
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.log(Level.FINER, "Setting useHttp11:" + iUseHttp11);
			iLogger.log(Level.FINER, "Setting MPost:" + iUseMPost);
		}
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, methodName);
		}
	}

	public void useMPost(boolean pValue) {
		String methodName = "useMPost(boolean)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, Boolean.valueOf(pValue));
		}

		iUseMPost = pValue;
		if (iUseMPost) iUseHttp11 = true;

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.log(Level.FINER, "Setting useHttp11:" + iUseHttp11);
			iLogger.log(Level.FINER, "Setting MPost:" + iUseMPost);
		}

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.exiting(CLASSNAME, methodName);
		}
	}

	public void preCheck(CIMObjectPath pPath) throws CIMException {
		if (pPath == null) { throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null object path"); }
		String ns = pPath.getNameSpace();
		if (ns == null || ns.length() == 0) {
			pPath.setNameSpace(iNamespace.getNameSpace());
		}
		// String host = objectName.getHost();
		// if (host == null || host.length() ==0 ) {
		// objectName.setHost(nameSpace.getHost());
		// }
	}

	public void preCheck(CIMNameSpace pNamespace) throws CIMException {
		if (pNamespace == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null namespace");
	}

	public CIMNameSpace getNameSpace() {
		return iNamespace;
	}

	public Enumeration associatorNames(CIMObjectPath pPath) throws CIMException {
		return associatorNames(pPath, null, null, null, null);
	}

	public Enumeration associatorNames(CIMObjectPath pPath, String pAssociationClass,
			String pResultClass, String pRole, String pResultRole) throws CIMException {
		String methodName = "associatorNames(CIMObjectPath, String, String, String, String)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath, pAssociationClass,
					pResultClass, pRole, pResultRole });
		}
		preCheck(pPath);

		Enumeration enumeration = null;
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();

			iXmlHelper.createCIMMessage(doc, iXmlHelper.associatorNames_request(doc, pPath,
					pAssociationClass, pResultClass, pRole, pResultRole));

			InputStreamReader is = transmitRequest("AssociatorNames", hh, doc);
			enumeration = getEnumeration(is, pPath);

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing associatorNames request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing associatorNames request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);

		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, enumeration);
			}
		}

		return enumeration;
	}

	public Enumeration associators(CIMObjectPath pPath, String pAssociationClass, String pResultClass,
			String pRole, String pResultRole, boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			String[] pPropertyList) throws CIMException {

		String methodName = "associators(CIMObjectPath, String, String, String, String, boolean, boolean,  String[])";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath, pAssociationClass,
					pResultClass, pRole, pResultRole, Boolean.valueOf(pIncludeQualifiers),
					Boolean.valueOf(pIncludeClassOrigin), pPropertyList });
		}
		preCheck(pPath);
		Enumeration enumeration = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.associators_request(doc, pPath,
					pAssociationClass, pResultClass, pRole, pResultRole, pIncludeQualifiers,
					pIncludeClassOrigin, pPropertyList));

			InputStreamReader is = transmitRequest("Associators", hh, doc);

			enumeration = getEnumeration(is, pPath);

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing associator request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing associator request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, enumeration);
			}
		}

		return enumeration;
	}

	public void deleteInstance(CIMObjectPath pPath) throws CIMException {
		String methodName = "deleteInstance(CIMObjectPath)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath });
		}
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();

			iXmlHelper.createCIMMessage(doc, iXmlHelper.deleteInstance_request(doc, pPath));

			InputStreamReader is = transmitRequest("DeleteInstance", hh, doc);
			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteInstance request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteInstance request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public Enumeration enumerateInstanceNames(CIMObjectPath pPath) throws CIMException {
		String methodName = "enumerateInstanceNames(CIMObjectPath)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath });
		}
		preCheck(pPath);
		Enumeration enumeration = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.enumerateInstanceNames_request(doc,
					pPath));

			InputStreamReader is = transmitRequest("EnumerateInstanceNames", hh, doc);

			enumeration = getEnumeration(is, pPath);

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateInstanceNames request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateInstanceNames request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}

		return enumeration;
	}

	public Enumeration enumerateInstances(CIMObjectPath pPath, boolean pDeep,
			boolean pLocalOnly, boolean pIncludeQualifiers, boolean pIncludeClassOrigin,
			java.lang.String[] pPropertyList) throws CIMException {
		String methodName = "enumerateInstances(CIMObjectPath, boolean, boolean, boolean, boolean, String[])";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath,
					Boolean.valueOf(pDeep), Boolean.valueOf(pLocalOnly),
					Boolean.valueOf(pIncludeQualifiers), Boolean.valueOf(pIncludeClassOrigin),
					pPropertyList });
		}
		preCheck(pPath);
		Enumeration enumeration = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.enumerateInstances_request(doc, pPath,
					pDeep, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList));

			InputStreamReader is = transmitRequest("EnumerateInstances", hh, doc);
			enumeration = getEnumeration(is, pPath);

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateInstances request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateInstances request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.exiting(CLASSNAME, methodName, enumeration);
			}

		}
		return enumeration;
	}

	public CIMInstance getInstance(CIMObjectPath pPath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, java.lang.String[] pPropertyList)
			throws CIMException {
		String methodName = "getInstance(CIMObjectPath, boolean, boolean, boolean, String[])";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath,
					Boolean.valueOf(pLocalOnly), Boolean.valueOf(pIncludeQualifiers),
					Boolean.valueOf(pIncludeClassOrigin), pPropertyList });
		}
		preCheck(pPath);
		CIMInstance inst = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.getInstance_request(doc, pPath,
					pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList));

			InputStreamReader is = transmitRequest("GetInstance", hh, doc);

			CIMEnumeration enumeration = getEnumeration(is, pPath);
			try {
				if (enumeration.hasMoreElements()) {
					inst = (CIMInstance) enumeration.nextElement();
					inst.setObjectPath(pPath);
				}
			} finally {
				enumeration.close();
			}

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing getInstance request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing getInstance request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, inst);
			}
		}

		return inst;
	}

	public CIMClass getClass(CIMObjectPath pPath, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, java.lang.String[] pPropertyList)
			throws CIMException {
		String methodName = "getClass(CIMObjectPath, boolean, boolean, boolean, String[])";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath,
					Boolean.valueOf(pLocalOnly), Boolean.valueOf(pIncludeQualifiers), pPropertyList });
		}
		preCheck(pPath);
		CIMClass cimClass = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.getClass_request(doc, pPath, pLocalOnly,
					pIncludeQualifiers, pIncludeClassOrigin, pPropertyList));

			InputStreamReader is = transmitRequest("GetClass", hh, doc);

			CIMEnumeration enumeration = getEnumeration(is, pPath);
			try {
				if (enumeration.hasMoreElements()) {
					cimClass = (CIMClass) enumeration.nextElement();
				}
			} finally {
				enumeration.close();
			}

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing getClass request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing getClass request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, cimClass);
			}
		}

		return cimClass;
	}

	public CIMObjectPath createInstance(CIMObjectPath pObjectName, CIMInstance pInstance)
			throws CIMException {
		String methodName = "createInstance(CIMObjectPath, CIMInstance)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pObjectName, pInstance });
		}
		preCheck(pObjectName);
		CIMObjectPath ref = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pObjectName.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.createInstance_request(doc, pObjectName,
					pInstance));

			InputStreamReader is = transmitRequest("CreateInstance", hh, doc);

			CIMEnumeration enumeration = getEnumeration(is, pObjectName);
			try {
				if (enumeration.hasMoreElements()) {
					ref = (CIMObjectPath) enumeration.nextElement();
				}
			} finally {
				enumeration.close();
			}

		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createInstance request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createInstance request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, ref);
			}
		}
		return ref;
	}

	public CIMValue invokeMethod(CIMObjectPath pObjectName, String pMethodName, Vector pInputArguments,
			Vector pOutputArguments) throws CIMException {
		String mName = "invokeMethod(CIMObjectPath, String, Vector, Vector)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, mName, new Object[] { pObjectName, pMethodName, pInputArguments,
					pOutputArguments });
		}
		preCheck(pObjectName);
		CIMValue rtnValue = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pObjectName.toString(), "UTF-8", "US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.invokeMethod_request(doc, pObjectName,
					pMethodName, pInputArguments));

			InputStreamReader is = transmitRequest(pMethodName, hh, doc);

			CIMResponse response;
			if (getXmlParser() == SessionProperties.SAX_PARSER
					|| getXmlParser() == SessionProperties.PULL_PARSER) {
				XMLDefaultHandlerImpl hndlr = new XMLDefaultHandlerImpl(iSessionProperties
						.isDebugXMLInput());
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(new InputSource(is), hndlr);

				Vector o = hndlr.getObjects();
				response = (CIMResponse) o.elementAt(0);
			} else {
				response = getSingleResponse(is);
			}

			response.checkError();
			Vector resultSet = response.getFirstReturnValue();

			if (resultSet.size() > 0 && (resultSet.elementAt(0) instanceof CIMValue)) rtnValue = (CIMValue) resultSet
					.elementAt(0); // there must be at least one value
			// TODO how the other values are going to be handled?

			// TODO each parameter needs to be filtered, if the corresponding
			// value is an instance
			// or a class, the namespace must be fixed
			Vector outParamValues = response.getParamValues();

			if (pOutputArguments != null) {
				pOutputArguments.addAll(outParamValues);
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing invokeMethod request", e);
			}
			throw (e);
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing invokeMethod request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, mName, rtnValue);
			}
		}

		return rtnValue;
	}

	public void createClass(CIMObjectPath pPath, CIMClass pClass) throws CIMException {
		String methodName = "createClass(CIMObjectPath, CIMClass)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath, pClass });
		}
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.createClass_request(doc, pPath, pClass));

			InputStreamReader is = transmitRequest("CreateClass", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createClass request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createClass request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public void createQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType)
			throws CIMException {
		String methodName = "createQualifierType(CIMObjectPath, CIMQualifierType)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath, pQualifierType });
		}
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.createQualifierType_request(doc, pPath,
					pQualifierType));

			InputStreamReader is = transmitRequest("CreateQualifierType", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createQualifierType request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing createQualifierType request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public void deleteClass(CIMObjectPath pPath) throws CIMException {
		String methodName = "deleteClass(CIMObjectPath)";
		if (iLogger.isLoggable(Level.INFO)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath });
		}
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.deleteClass_request(doc, pPath));

			InputStreamReader is = transmitRequest("DeleteClass", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteClass request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteClass request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public void deleteQualifierType(CIMObjectPath pPath) throws CIMException {
		String methodName = "deleteQualifierType(CIMObjectPath)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath });
		}
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.deleteQualifierType_request(doc, pPath));

			InputStreamReader is = transmitRequest("DeleteQualifierType", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteQualifierType request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing deleteQualifierType request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName);
			}
		}
	}

	public Enumeration enumerateClasses(CIMObjectPath pPath, boolean pDeep, boolean pLocalOnly,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin) throws CIMException {
		String methodName = "deleteQualifierType(CIMObjectPath, boolean, boolean, boolean, boolean)";
		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pPath,
					Boolean.valueOf(pDeep), Boolean.valueOf(pLocalOnly),
					Boolean.valueOf(pIncludeQualifiers), Boolean.valueOf(pIncludeClassOrigin) });
		}
		preCheck(pPath);
		Enumeration enumeration = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.enumerateClasses_request(doc, pPath,
					pDeep, pLocalOnly, pIncludeQualifiers, pIncludeClassOrigin));

			InputStreamReader is = transmitRequest("EnumerateClasses", hh, doc);

			enumeration = getEnumeration(is, pPath);
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateClasses request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateClasses request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, enumeration);
			}
		}

		return enumeration;
	}

	public Enumeration enumerateClassNames(CIMObjectPath pObjectName, boolean pDeep)
			throws CIMException {
		String methodName = "enumerateClassNames(CIMObjectPath, boolean)";

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName,
					new Object[] { pObjectName, Boolean.valueOf(pDeep) });
		}

		preCheck(pObjectName);
		Enumeration enumeration = null;

		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pObjectName.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.enumerateClassNames_request(doc, pObjectName,
					pDeep));

			InputStreamReader is = transmitRequest("EnumerateClassNames", hh, doc);

			enumeration = getEnumeration(is, pObjectName);
		} catch (CIMException e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateClassNames request", e);
			}
			throw e;
		} catch (Exception e) {
			if (iLogger.isLoggable(Level.INFO)) {
				iLogger.log(Level.INFO, "Error while processing enumerateClassNames request", e);
			}
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		} finally {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.exiting(CLASSNAME, methodName, enumeration);
			}
		}

		return enumeration;
	}

	public Enumeration execQuery(CIMObjectPath pPath) throws CIMException {
		return this.execQuery(pPath, null, CIMClient.WQL);
	}

	public CIMValue getProperty(CIMObjectPath pPath, String pPropertyName) throws CIMException {
		// This is a bug in the specification. Only the value of the property is
		// returned with no datatype, so we the the property from the instance,
		// to get
		// the datatype
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.getInstance_request(doc, pPath, false,
					false, false, new String[] { pPropertyName }));

			InputStreamReader is = transmitRequest("GetInstance", hh, doc);

			CIMEnumeration enumeration = getEnumeration(is, pPath);
			try {
				if (enumeration.hasMoreElements()) {
					CIMInstance inst = (CIMInstance) enumeration.nextElement();
					if (inst != null) {
						CIMProperty property = inst.getProperty(pPropertyName);
						if (property != null) return property.getValue();
					}
				}
			} finally {
				enumeration.close();
			}

			return null;
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public Enumeration referenceNames(CIMObjectPath pPath) throws CIMException {
		return referenceNames(pPath, null, null);
	}

	public Enumeration referenceNames(CIMObjectPath pPath, String pResultClass, String pRole)
			throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.referenceNames_request(doc, pPath,
					pResultClass, pRole));

			InputStreamReader is = transmitRequest("ReferenceNames", hh, doc);

			return getEnumeration(is, pPath);
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public Enumeration references(CIMObjectPath pPath) throws CIMException {
		return references(pPath, null, null, true, true, null);
	}

	public Enumeration references(CIMObjectPath pPath, String pResultClass, String pRole,
			boolean pIncludeQualifiers, boolean pIncludeClassOrigin, String[] pPropertyList)
			throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.references_request(doc, pPath,
					pResultClass, pRole, pIncludeQualifiers, pIncludeClassOrigin, pPropertyList));

			InputStreamReader is = transmitRequest("References", hh, doc);

			return getEnumeration(is, pPath);
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void setClass(CIMObjectPath pPath, CIMClass pClass) throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.setClass_request(doc, pPath, pClass));

			InputStreamReader is = transmitRequest("ModifyClass", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void setInstance(CIMObjectPath pPath, CIMInstance pInstance, boolean pIncludeQualifiers,
			String[] pPropertyList) throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.setInstance_request(doc, pPath, pInstance,
					pIncludeQualifiers, pPropertyList));
			InputStreamReader is = transmitRequest("ModifyInstance", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void setProperty(CIMObjectPath pPath, String pPropertyName) throws CIMException {
		setProperty(pPath, pPropertyName, null);
	}

	public void setProperty(CIMObjectPath pPath, String pPropertyName, CIMValue pValue)
			throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.setProperty_request(doc, pPath,
					pPropertyName, pValue));

			InputStreamReader is = transmitRequest("SetProperty", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void setQualifierType(CIMObjectPath pPath, CIMQualifierType pQualifierType) throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper
					.createCIMMessage(doc, iXmlHelper.setQualifierType_request(doc, pPath, pQualifierType));

			InputStreamReader is = transmitRequest("SetQualifierType", hh, doc);

			CIMEnumeration result = getEnumeration(is, pPath);
			if (result!=null) {
				result.close();
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public void close() throws CIMException {
		close(false);
	}

	public void close(boolean pKeepEnumerations) throws CIMException {
		if (iLogger.isLoggable(Level.WARNING)) iLogger.log(Level.WARNING, "The CIM Client on: " + iUri
				+ " has been closed!");

		if (iConnection != null) {
			iConnection.close(pKeepEnumerations);
		}

		if (iIndicationServer != null) {
			iIndicationServer.close();
		}
	}

	public void createNameSpace(CIMNameSpace pNamespace) throws CIMException {
		preCheck(pNamespace);
		String namespace = pNamespace.getNameSpace();
		CIMInstance inst = new CIMInstance();
		inst.setClassName("CIM_NameSpace");
		// ebak: NameSpace->Name, since: There is no "NameSpace" property in CIM_NameSpace, instead
		// the "Name" property should be used.
		CIMProperty prop = new CIMProperty("Name");
		// ebak: now namespace contains the full namespace string, since there is no hierarchy between
		// different namespaces
		prop.setValue(new CIMValue(namespace, CIMDataType.getPredefinedType(CIMDataType.STRING)));
		Vector v = new Vector();
		v.add(prop);
		inst.setProperties(v);
		// ebak: DSP0200 recommends to create CIM_NameSpace instances in the namespace "root"
		CIMObjectPath op = new CIMObjectPath(null, "root");
		createInstance(op, inst);
	}

	public void deleteNameSpace(CIMNameSpace pNamespace) throws CIMException {
		preCheck(pNamespace);

		CIMObjectPath op = new CIMObjectPath();
		// op.setObjectName("CIM_NameSpace");
		op.setNameSpace(pNamespace);
		deleteInstance(op);
	}

	public Enumeration enumNameSpace(CIMObjectPath pPath, boolean pDeep) throws CIMException {
		preCheck(pPath);
		pPath.setObjectName("CIM_NameSpace");
		return enumerateInstanceNames(pPath);
	}

	public Enumeration execQuery(CIMObjectPath pPath, String pQuery, String pQueryLanguage)
			throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.execQuery_request(doc, pPath, pQuery,
					pQueryLanguage));

			InputStreamReader is = transmitRequest("ExecQuery", hh, doc);

			return getEnumeration(is, pPath);
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public CIMQualifierType getQualifierType(CIMObjectPath pPath) throws CIMException {
		return getQualifierType(pPath, null);
	}

	public CIMQualifierType getQualifierType(CIMObjectPath pPath, String pQualifierType)
			throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.getQualifier_request(doc, pPath, pQualifierType));

			InputStreamReader is = transmitRequest("GetQualifier", hh, doc);

			CIMEnumeration enumeration = getEnumeration(is, pPath);
			try {
				if (enumeration.hasMoreElements()) {
					return (CIMQualifierType) enumeration.nextElement();
				}
			} finally {
				enumeration.close();
			}
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
		return null;
	}

	public Enumeration enumQualifierTypes(CIMObjectPath pPath) throws CIMException {
		preCheck(pPath);
		try {
			HttpHeader hh = new HttpHeader();
			hh.addField("CIMObject", HttpHeader.encode(pPath.getNameSpace(), "UTF-8",
					"US-ASCII"));

			Document doc = iXmlHelper.newDocument();
			iXmlHelper.createCIMMessage(doc, iXmlHelper.enumQualifierTypes_request(doc, pPath));

			InputStreamReader is = transmitRequest("EnumerateQualifiers", hh, doc);

			return getEnumeration(is, pPath);
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public BatchResult performBatchOperations(BatchHandle pBatchHandle) throws CIMException {
		Vector operations = pBatchHandle.getOperations();
		BatchResult result = null;

		if (operations.size() < 2) throw new CIMException(CIMException.CIM_ERR_FAILED,
				"Invalid number of batch operations (" + operations.size() + ")");

		Iterator iter = operations.iterator();
		while (iter.hasNext()) {
			CIMOperation op = (CIMOperation) iter.next();
			if (op instanceof CIMCreateNameSpaceOp || op instanceof CIMDeleteNameSpaceOp) preCheck(op
					.getNameSpace());
			else preCheck(op.getObjectName());
		}

		HttpHeader hh = new HttpHeader();
		hh.addField("CIMBatch", "CIMBatch");

		Document doc = iXmlHelper.newDocument();

		try {
			iXmlHelper.performBatchOperation_request(doc, operations);
			InputStreamReader is = transmitRequest(null, hh, doc);

			CIMResponse response;
			if (getXmlParser() == SessionProperties.SAX_PARSER
					|| getXmlParser() == SessionProperties.PULL_PARSER) {

				XMLDefaultHandlerImpl hndlr = new XMLDefaultHandlerImpl(iSessionProperties
						.isDebugXMLInput());
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				saxParser.parse(new InputSource(is), hndlr);

				Vector o = hndlr.getObjects();

				response = (CIMResponse) o.elementAt(0);

			} else {
				response = getMultiResponse(is);
			}

			Vector allResponses = response.getAllResponses();

			if (allResponses.size() != operations.size()) throw new CIMException(
					CIMException.CIM_ERR_FAILED,
					"Batch operation result set size does not the size of the request");

			iter = allResponses.iterator();
			for (int i = 0; i < allResponses.size(); i++) {
				CIMResponse resp = (CIMResponse) allResponses.elementAt(i);
				CIMOperation op = (CIMOperation) operations.elementAt(i);

				if (resp.isSuccessul()) {
					if (op instanceof CIMGetClassOp || op instanceof CIMGetInstanceOp
							|| op instanceof CIMGetPropertyOp
							|| op instanceof CIMGetQualifierTypeOp) {

						Vector v = resp.getFirstReturnValue();
						if (v.size() > 0) {
							if (op instanceof CIMGetPropertyOp) {
								CIMInstance inst = (CIMInstance) v.elementAt(0);
								CIMProperty property = inst.getProperty(((CIMGetPropertyOp) op)
										.getPropertyName());
								if (property != null) op.setResult(property.getValue());
							} else if (op instanceof CIMGetClassOp
									|| op instanceof CIMGetInstanceOp) {
								op.setResult(fixResultSet(op.getObjectName(), v, iNamespace));
							} else op.setResult(v.elementAt(0));
						} else op.setResult(null);
					} else if (op instanceof CIMAssociatorNamesOp || op instanceof CIMAssociatorsOp
							|| op instanceof CIMEnumClassesOp || op instanceof CIMEnumClassNamesOp
							|| op instanceof CIMEnumInstanceNamesOp
							|| op instanceof CIMEnumInstancesOp || op instanceof CIMEnumNameSpaceOp
							|| op instanceof CIMEnumQualifierTypesOp
							|| op instanceof CIMReferenceNamesOp || op instanceof CIMReferencesOp
							|| op instanceof CIMExecQueryOp) {

						op.setResult(fixResultSet(op.getObjectName(), resp.getFirstReturnValue(),
								iNamespace).elements());
					} else if (op instanceof CIMInvokeMethodOp) {
						Vector outParamValues = resp.getParamValues();
						Vector outArgs = ((CIMInvokeMethodOp) op).getOutParams();

						// TODO fix the objectpath of instances and classes
						// returned as parameters
						if (outArgs != null) {
							outArgs.addAll(outParamValues);
						}
					}
				} else {
					op.setResult(resp.getException());
				}
			}
			result = new BatchResult(operations.toArray(new CIMOperation[operations.size()]));
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}

		return result;
	}

	private boolean initializeIndicationServer() {
		if (iIndicationServer == null) {
			synchronized (this) {
				if (iIndicationServer == null) {
					CIMEventDispatcher dispatcher = new CIMEventDispatcher(iIndicationClient);
					CIMIndicationHandler indicationHdlr = new CIMIndicationHandler(dispatcher,
							iSessionProperties);
					try {
						iIndicationServer = new HttpServerConnection(new HttpConnectionHandler(
								indicationHdlr), 0, false, iSessionProperties);
						iIndicationServer.setName("CIMListener - Http Server");
						iIndicationServer.start();
						return true;
					} catch (Exception e) {
						dispatcher.kill();

						if (iLogger.isLoggable(Level.WARNING)) {
							iLogger.log(Level.WARNING,
									"could not initialize CIM Indication Listener", e);
						}
					}
				} else return true;
			}
		} else return true;
		return false;
	}

	public void addCIMListener(CIMListener pListener) throws CIMException {

		if (initializeIndicationServer()) {
			iIndicationClient.addListener(pListener);
		} else throw new CIMException(CIMException.CIM_ERR_FAILED,
				"Could not initialize indication listener");
	}

	public void addCIMListener(CIMListener pListener, String pId) throws CIMException {
		if (pListener == null) throw new IllegalArgumentException("null CIMListener argument");
		if (initializeIndicationServer()) {
			iIndicationClient.addListener(pListener);
		} else throw new CIMException(CIMException.CIM_ERR_FAILED,
				"Could not initialize indication listener");
	}

	public void removeCIMListener(CIMListener pListener) throws CIMException {
		iIndicationClient.removeListener(pListener);
	}

	public CIMInstance getIndicationListener(CIMListener pListener) throws CIMException {

		if (pListener == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null listener argument");

		if (!initializeIndicationServer()) throw new CIMException(CIMException.CIM_ERR_FAILED,
				"Could not initialize indication listener");

		int port;
		String host;
		try {
			host = iIndicationServer.getHostIP();
			port = iIndicationServer.getPort();

			CIMClass clazz = getClass(new CIMObjectPath("CIM_ListenerDestinationCIMXML"), false,
					false, false, null);
			CIMInstance inst = clazz.newInstance();
			inst.setProperty("CreationClassName", new CIMValue("CIM_IndicationHandlerCIMXML",
					CIMDataType.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("SystemCreationClassName", new CIMValue("CIM_ComputerSystem",
					CIMDataType.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("Name", new CIMValue(getUniqueID(host, port), CIMDataType
					.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("SystemName", new CIMValue(host, CIMDataType
					.getPredefinedType(CIMDataType.STRING)));

			String id = String.valueOf(pListener.hashCode());
			inst.setProperty("Destination", new CIMValue("http://" + host + ":" + port + "/" + id,
					CIMDataType.getPredefinedType(CIMDataType.STRING)));

			return inst;
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	public CIMInstance getIndicationHandler(CIMListener pListener) throws CIMException {

		// TODO do we need the listener? This is handled by the CIMClient
		// implementation
		if (pListener == null) throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
				"null listener argument");

		if (!initializeIndicationServer()) throw new CIMException(CIMException.CIM_ERR_FAILED,
				"Could not initialize indication server");

		int port;
		String host;
		try {
			host = iIndicationServer.getHostIP();
			port = iIndicationServer.getPort();

			String id = String.valueOf(pListener.hashCode());
			CIMClass clazz = getClass(new CIMObjectPath("CIM_IndicationHandler"), false, false,
					false, null);
			CIMInstance inst = clazz.newInstance();
			inst.setProperty("CreationClassName", new CIMValue("CIM_IndicationHandler", CIMDataType
					.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("SystemCreationClassName", new CIMValue("CIM_ComputerSystem",
					CIMDataType.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("Name", new CIMValue(getUniqueID(host, port), CIMDataType
					.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("SystemName", new CIMValue(host, CIMDataType
					.getPredefinedType(CIMDataType.STRING)));
			inst.setProperty("Destination", new CIMValue("http://" + host + ":" + port + "/" + id,
					CIMDataType.getPredefinedType(CIMDataType.STRING)));

			return inst;
		} catch (CIMException e) {
			throw e;
		} catch (Exception e) {
			throw new CIMException(CIMException.CIM_ERR_FAILED, e);
		}
	}

	private String getUniqueID(String pHost, int pPort) {
		int cntr;
		synchronized (iLock) {
			cntr = iCounter++;
		}
		String uniqueId = pHost + ":" + pPort + ":" + System.currentTimeMillis() + ":" + cntr++;
		return uniqueId;
	}

	public HttpUrlConnection newConnection(String pCimMethod, HttpHeader pHeader) {
		String methodName = "newConnection(String, HttpHeader)";

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pCimMethod, pHeader });
		}
		// if (connection == null) {
		iConnection = new HttpUrlConnection(iUri, iHttpClientPool, iAuthorizationHandler);
		
		// retry HTTP MPOST only if failure is more than 24 hours old
		if (iMPostFailed) {
			iCurrentTime = System.currentTimeMillis();
			if ( (iCurrentTime - iMPostFailTime) > 24 * 60 * 60 * 1000 )
				iMPostFailed = false;
		}
		
		if (iUseMPost && ! iMPostFailed) 
			iConnection.setRequestMethod("M-POST");
		else {
			iConnection.setRequestMethod("POST");
		}
			
		iConnection.useHttp11(iUseHttp11);
		// } else
		// connection.reset();

		String localeStr = iLocale.getLanguage();
		if (iLocale.getCountry().length() > 0) localeStr = localeStr + '-' + iLocale.getCountry();

		// String locale = .toString();
		iConnection.setDoOutput(true);
		iConnection.setDoInput(true);
		iConnection.setRequestProperty("Content-type", "application/xml; charset=\"utf-8\"");
		iConnection.setRequestProperty("Accept", "text/html, text/xml, application/xml");
		iConnection.setRequestProperty("Cache-Control", "no-cache");
		iConnection.setRequestProperty("Content-Language", localeStr);
		iConnection.setRequestProperty("Accept-Language", localeStr + ", *");
		if (iAuthorization != null) iConnection.setRequestProperty("Authorization", iAuthorization);

		String prefix = "";
		if (iConnection.getRequestMethod().equalsIgnoreCase("M-POST")) {
			iNsCounter++;
			if (iNsCounter > 99) iNsCounter = 0;
			String ns = (iNsCounter < 10 ? "0" : "") + iNsCounter;
			iConnection.setRequestProperty("Man", "http://www.dmtf.org/cim/mapping/http/v1.0;ns="
					+ ns);
			// connection.setRequestProperty("Opt",
			// "http://www.dmtf.org/cim/mapping/http/v1.0;ns="+ns);
			prefix = ns + "-";
		}
		iConnection.setRequestProperty(prefix + "CIMProtocolVersion", "1.0");

		iConnection.setRequestProperty(prefix + "CIMOperation", "MethodCall");
		try {
			iConnection.setRequestProperty(prefix + "CIMMethod", HttpHeader.encode(pCimMethod,
					"UTF-8", "US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			if (iLogger.isLoggable(Level.WARNING)) {
				iLogger.log(Level.WARNING, "Unable to encode HTTP Header", e);
			}
			iConnection.setRequestProperty(prefix + "CIMMethod", pCimMethod);
		}
		Iterator iter = pHeader.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			iConnection.setRequestProperty(prefix + entry.getKey().toString(), entry.getValue()
					.toString());
		}

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.log(Level.FINER, "headers = " + pHeader);
			iLogger.exiting(CLASSNAME, methodName, iConnection);
		}

		return iConnection;
	}

	private CIMEnumeration getEnumeration(InputStreamReader pStream, CIMObjectPath pPath) throws Exception {
		try {
			CIMEnumeration enumeration;

			if (getXmlParser() == SessionProperties.SAX_PARSER
					|| getXmlParser() == SessionProperties.PULL_PARSER) {
				enumeration = new CIMEnumerationImpl(new XMLDefaultHandlerImpl(iSessionProperties
						.isDebugXMLInput()), pPath, iNamespace, pStream, iConnection
						.getHttpClient(), getXmlParser() == SessionProperties.SAX_PARSER);
			} else {
				enumeration = new CIMEnumerationImpl(getSingleResponse(pStream)
						.getFirstReturnValue().elements(), pPath, iNamespace);
			}
			return enumeration;
		} catch (Exception e) {
			iConnection.disconnect();
			throw e;
		}
	}

	public CIMResponse getMultiResponse(InputStreamReader pStream) throws IOException, SAXException {
		Document doc = iXmlHelper.parse(new org.xml.sax.InputSource(pStream));
		if (iSessionProperties.isDebugXMLInput()) CIMClientXML_HelperImpl.dumpDocument(doc); // debug

		pStream.close();
		Element rootE = doc.getDocumentElement();
		CIMResponse response = (CIMResponse) CIMXMLParserImpl.parseCIM(rootE);
		return response;
	}

	public CIMResponse getDocument(InputStreamReader pStream) throws IOException, SAXException {
		Document doc = iXmlHelper.parse(new org.xml.sax.InputSource(pStream));
		if (iSessionProperties.isDebugXMLInput()) CIMClientXML_HelperImpl.dumpDocument(doc); // debug
		pStream.close();

		Element rootE = doc.getDocumentElement();
		CIMResponse response = (CIMResponse) CIMXMLParserImpl.parseCIM(rootE);
		response.checkError();

		return response;
	}

	private CIMResponse getSingleResponse(InputStreamReader pStream) throws IOException, SAXException {
		XMLDefaultHandlerImpl handler = new XMLDefaultHandlerImpl(iSessionProperties
				.isDebugXMLInput());
		handler.parse(new XMLPullParser(pStream));
		Vector v = handler.getObjects();
		if (v.size() > 0) {
			CIMResponse response = (CIMResponse) v.elementAt(0);
			response.checkError();
			return response;
		}
		throw new IllegalStateException("XML parsing produces no CIM response");
	}

	public InputStreamReader transmitRequest(String pCimMethod, HttpHeader pHeader, Document pDocument)
			throws CIMException, IOException, ProtocolException, SAXException {
		String methodName = "transmitRequest(String, HttpHeader, Document )";

		if (iLogger.isLoggable(Level.FINER)) {
			iLogger.entering(CLASSNAME, methodName, new Object[] { pCimMethod, pHeader });
		}
		Benchmark.startTransportTimer();

		int retries = iSessionProperties.getRetriesNumber();
		while (retries-- >= 0) {
			if (iLogger.isLoggable(Level.FINER)) {
				iLogger.log(Level.FINER, "Attempting to connect.. num of attempts left:" + retries);
			}

			// Disconnect existing connection to prevent object leak
			if (iConnection != null) {
				iConnection.disconnect();
			}
			iConnection = newConnection(pCimMethod, pHeader);
			try {
				if (iLogger.isLoggable(Level.FINER)) {
					iLogger.log(Level.FINER, "connecting...");
				}
				iConnection.connect();
			} catch (UnknownHostException uhe) {
				throw new CIMTransportException(CIMTransportException.EXT_ERR_UNKNOWN_SERVER, uhe);
			} catch (SocketException e) {
				iConnection.disconnect();
				throw new CIMTransportException(CIMTransportException.EXT_ERR_UNABLE_TO_CONNECT, e);
			}

			OutputStream os = iConnection.getOutputStream();

			if (iSessionProperties.isDebugXMLOutput()) CIMClientXML_HelperImpl.dumpDocument(pDocument); // debug
			CIMClientXML_HelperImpl.serialize(os, pDocument);
			os.flush();
			os.close();

			int resultCode = 200;
			try {
				resultCode = iConnection.getResponseCode();
			} catch (SocketException e) {
				iConnection.disconnect();
				throw new CIMException(CIMTransportException.EXT_ERR_UNABLE_TO_CONNECT, e);
			} catch (SocketTimeoutException e) {
				iConnection.disconnect();
				throw new CIMException(CIMTransportException.EXT_ERR_TIME_OUT, e);
			} catch (Throwable e) {
				iConnection.disconnect();
				throw new CIMException(CIMException.CIM_ERR_FAILED, e);
			}

			HttpHeader headers = parseHeaders(iConnection);
			String auth = iConnection.getRequestProperty("Authorization");
			if (auth != null) {
				iAuthorization = auth;
			}

			String cimError = headers.getField("CIMError");
			// boolean isMPost =
			// "M-POST".equalsIgnoreCase(conn.getRequestMethod());

			if (resultCode == HttpUrlConnection.HTTP_OK) {

				if (iSessionProperties.isContentLengthRetryEnabled()) {
					String contentLengthField = headers.getField("Content-length");
					if (contentLengthField != null && contentLengthField.trim().length() > 0) {
						int contentLength = Integer.parseInt(contentLengthField);
						int lengthToCheck = iSessionProperties.getContentLength();

						if (contentLength < lengthToCheck) {
							if (iLogger.isLoggable(Level.WARNING)) iLogger.log(Level.WARNING,
									"Content Length below :" + lengthToCheck + " ...retrying !");
							continue;
						}
					}
				}

				String charset = getCharacterSet(headers);

				InputStream is;
				if (iSessionProperties.isDebugInputStream()) is = new DebugInputStream(iConnection
						.getInputStream(), iSessionProperties.getDebugOutputStream());
				else is = iConnection.getInputStream();
				// This timer is not acurate anymore
				Benchmark.stopTransportTimer();
				return new InputStreamReader(is, charset);
				// 500
			} else if (resultCode == HttpUrlConnection.HTTP_INTERNAL_ERROR) { // 500
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"Received HTTP Error 500 - HTTP_INTERNAL_ERROR - retying !");
				// retry

				// 501
			} else if (resultCode == HttpUrlConnection.HTTP_NOT_IMPLEMENTED) { // 501
				// TODO if there is an error with the default xml
				// encoder/decoder, load the correct version
				// The problem is that the CIMOM does not return the DTD
				// version, CIM version or Protocol Version
				// that is expecting
				String cimProtocolVersion = headers.getField("CIMProtocolVersion");
				if (cimProtocolVersion == null && cimError == null && iUseMPost) {
					if (iLogger.isLoggable(Level.FINER)) iLogger
							.log(Level.FINER,
									"Received HTTP Error 501 - HTTP NOT IMPLEMENTED - retying with HTTP POST !");
					iMPostFailTime = System.currentTimeMillis();
					iMPostFailed = true;
				} else {
					if (cimProtocolVersion == null || cimProtocolVersion.length() == 0) cimProtocolVersion = "1.0";

					if ("unsupported-protocol-version".equalsIgnoreCase(cimError)) {
						if (iLogger.isLoggable(Level.FINER)) iLogger
								.log(Level.FINER,
										"Received HTTP Error 501 - HTTP NOT IMPLEMENTED - unsupported-protocol-version !");
						break;
					} else if ("multiple-request-unsupported".equalsIgnoreCase(cimError)) {
						if (iLogger.isLoggable(Level.FINER)) iLogger
								.log(Level.FINER,
										"Received HTTP Error 501 - HTTP NOT IMPLEMENTED - multiple-request-unsupported !");
						break;
					} else if ("unsupported-cim-version".equalsIgnoreCase(cimError)) {
						if (iLogger.isLoggable(Level.FINER)) iLogger
								.log(Level.FINER,
										"Received HTTP Error 501 - HTTP NOT IMPLEMENTED - unsupported-cim-version !");
						break;
					} else if ("unsupported-dtd-version".equalsIgnoreCase(cimError)) {
						if (iLogger.isLoggable(Level.FINER)) iLogger
								.log(Level.FINER,
										"Received HTTP Error 501 - HTTP NOT IMPLEMENTED - unsupported-dtd-version !");
						break;
					}
				}

				// 400
			} else if (resultCode == HttpUrlConnection.HTTP_BAD_REQUEST) {
				if ("request-not-valid".equalsIgnoreCase(cimError)) {} else if ("request-not-well-formed"
						.equalsIgnoreCase(cimError)) {
					if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
							"Received HTTP Error 400 - BAD REQUEST - request-not-well-formed !");
					break;
				} else if ("request-not-loosely-valid".equalsIgnoreCase(cimError)) {
					if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
							"Received HTTP Error 400 - BAD REQUEST - request-not-loosely-valid !");
					break;
				}
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"Received HTTP Error 400 - BAD REQUEST - cimError not specified!");
				break;

				// 401
			} else if (resultCode == HttpUrlConnection.HTTP_UNAUTHORIZED) {
				if (iLogger.isLoggable(Level.FINER)) iLogger
						.log(Level.FINER,
								"Received HTTP Error 401 - UNAUTHORIZED. Throwing CIMAuthenticationException!");
				iConnection.disconnect();
				throw new CIMAuthenticationException(
						CIMAuthenticationException.EXT_ERR_AUTHENTICATION);

				// 403
			} else if (resultCode == HttpUrlConnection.HTTP_FORBIDDEN) {
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"Received HTTP Error 403 - FORBIDDEN.");
				break;

				// 407
			} else if (resultCode == HttpUrlConnection.HTTP_PROXY_AUTH) {
				if (iLogger.isLoggable(Level.FINER)) iLogger
						.log(Level.FINER,
								"Received HTTP Error 407 - ERR PROXY AUTHENTICATION. Throwing CIMAuthenticationException!");
				iConnection.disconnect();
				throw new CIMAuthenticationException(
						CIMAuthenticationException.EXT_ERR_PROXY_AUTHENTICATION);

				// 405
			} else if (resultCode == HttpUrlConnection.HTTP_BAD_METHOD) {
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"Received HTTP Error 405 - BAD METHOD.");
				break;

				// 510 Not Extended - this is not a constant in
				// HttpURLConnection
			} else if (resultCode == 510) {
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"Received HTTP Error 510 on M-POST. Retrying with POST.");
				iMPostFailTime = System.currentTimeMillis();
				iMPostFailed = true;
			} else {
				if (iLogger.isLoggable(Level.FINER)) iLogger.log(Level.FINER,
						"No known HTTP error recognized. Retrying with POST.");
				iMPostFailTime = System.currentTimeMillis();
				iMPostFailed = true;
			}
		}

		Benchmark.stopTransportTimer();
		String[] errorMsg = new String[] {
				"\"HTTP " + iConnection.getResponseCode() + " "
				+ iConnection.getResponseMessage() + "\"",
				"CIMError:\"" + iConnection.getHeaderField("CIMError") + "\"" };
		iConnection.disconnect();
		throw new CIMTransportException(CIMTransportException.EXT_ERR_UNABLE_TO_CONNECT,
				errorMsg);
	}

	protected String getCharacterSet(HttpHeader pHeader) {
		String contentType = pHeader.getField("Content-type");
		String charset = "UTF-8";
		if (contentType != null && contentType.length() > 0) {
			HttpHeaderParser contentTypeHeader = new HttpHeaderParser(contentType);
			charset = contentTypeHeader.findValue("charset", charset);
		}
		return charset;
	}

	protected HttpHeader parseHeaders(URLConnection pConnection) {
		String man = pConnection.getHeaderField("Man");
		String opt = pConnection.getHeaderField("Opt");
		HttpHeader headers = new HttpHeader();
		int i;
		String ns = null;

		HttpHeaderParser manOptHeader = null;
		if (man != null && man.length() > 0) manOptHeader = new HttpHeaderParser(man);
		else if (opt != null && opt.length() > 0) manOptHeader = new HttpHeaderParser(opt);
		if (manOptHeader != null) ns = manOptHeader.findValue("ns");

		if (ns != null) {
			i = 0;
			String key;
			while ((key = pConnection.getHeaderFieldKey(++i)) != null) {
				if (key.startsWith(ns + "-")) headers.addField(key.substring(3), pConnection
						.getHeaderField(i));
				else headers.addField(key, pConnection.getHeaderField(i));
			}
		} else {
			i = 1;
			String key;
			while ((key = pConnection.getHeaderFieldKey(i)) != null) {
				headers.addField(key, pConnection.getHeaderField(i));
				i++;
			}
		}

		return headers;
	}

	protected static Vector fixResultSet(CIMObjectPath pRequest, Vector pVector, CIMNameSpace pNameSpace) {

		if (pVector != null && pRequest.getNameSpace() != null && pNameSpace.getNameSpace() != null) {
			Iterator iter = pVector.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				fixResult(pRequest, o, pNameSpace);
			}
		}
		return pVector;
	}

	protected static Object fixResult(CIMObjectPath pRequest, Object pObject, CIMNameSpace pNameSpace) {
		if (pObject != null) {

			String ns = pRequest.getNameSpace();
			// String host = request.getHost();
			if (ns == null) ns = pNameSpace.getNameSpace();
			// if (host == null) host = nameSpace.getHost();

			CIMObjectPath def = new CIMObjectPath();
			// def.setHost(host);
			def.setNameSpace(ns);

			CIMObjectPath op = null;
			if (pObject instanceof CIMObjectPath) {
				op = (CIMObjectPath) pObject;
			} else if (pObject instanceof CIMInstance) {
				op = ((CIMInstance) pObject).getObjectPath();
			} else if (pObject instanceof CIMClass) {
				op = ((CIMClass) pObject).getObjectPath();
			}
			if (op != null) {
				String opNs = op.getNameSpace();
				if (opNs == null || opNs.length() == 0) op.setNameSpace(def.getNameSpace());
				// String hst = op.getHost();
				// if (hst == null || hst.length() == 0)
				// op.setHost(def.getHost());
			}
		}
		return pObject;
	}

	private int getXmlParser() {
		return iSessionProperties.getXmlParser();
	}

	public SessionProperties getSessionProperties() {
		return iSessionProperties.isGlobal() ? iSessionProperties : null;
	}

	public void setSessionProperties(SessionProperties pProperties) {
		iSessionProperties = (pProperties != null) ? pProperties : SessionProperties
				.getGlobalProperties();
		iHttpClientPool.setSessionProperties(pProperties);
	}

}
