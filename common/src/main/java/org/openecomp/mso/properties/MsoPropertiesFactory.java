/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.properties;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.CryptoUtils;

/**
 * This EJB Singleton class returns an instance of the mso properties for a specified file.
 * This class can handle many config at the same time and is thread safe.
 * This instance is a copy of the one cached so it can be modified or reloaded without impacting the others class using
 * it.
 * The mso properties files loaded and cached here will be reloaded every X second (it's configurable with the init
 * method)
 * This class can be used as an EJB or can be instantiated directly as long as the EJB has been initialized for the current
 * module. Locks are made manually and not using EJB locks to allow this.
 *
 *
 */
@Singleton(name = "MsoPropertiesFactory")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@LocalBean
@Path("/properties")
public class MsoPropertiesFactory implements Serializable {

	private static final long serialVersionUID = 4365495305496742113L;

    protected static String prefixMsoPropertiesPath = System.getProperty ("mso.config.path");

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    // Keep a static copy of properties for global usage
    private static final ConcurrentHashMap <String, MsoPropertiesParameters> msoPropertiesCache;

    static {
        if (prefixMsoPropertiesPath == null) {
            // Hardcode if nothing is received
            prefixMsoPropertiesPath = "";
        }
        msoPropertiesCache = new ConcurrentHashMap <String, MsoPropertiesParameters> ();
    }

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock ();

    public MsoPropertiesFactory () {

    }

    private boolean isJsonFile(String propertiesFilePath) {
    	return propertiesFilePath.endsWith(".json");
    }
    
    
    private boolean isJavaPropertiesFile (String propertiesFilePath) {
    	return propertiesFilePath.endsWith(".properties");
    }
    
	private MsoPropertiesParameters createObjectType (MsoPropertiesParameters msoPropParams, String propertiesFilePath) throws MsoPropertiesException, IOException {
    	
		try {
	    	if (this.isJavaPropertiesFile(propertiesFilePath)) {
	    		
	    		msoPropParams.msoProperties =  new MsoJavaProperties();
	    		msoPropParams.msoPropertiesType = MsoPropertiesParameters.MsoPropertiesType.JAVA_PROP;
	    	 } else if (this.isJsonFile(propertiesFilePath)) {
	    		 
	    		msoPropParams.msoProperties =  new MsoJsonProperties();
	    		msoPropParams.msoPropertiesType = MsoPropertiesParameters.MsoPropertiesType.JSON_PROP;
	         } else {
	        	 throw new MsoPropertiesException("Unable to load the MSO properties file because format is not recognized (only .json or .properties): " + propertiesFilePath);
	         }
	
	    	msoPropParams.msoProperties.loadPropertiesFile (propertiesFilePath);
	    	
	    	return msoPropParams;
		} finally {
			if (msoPropParams.msoProperties!=null) {
				msoPropParams.refreshCounter = msoPropParams.msoProperties.getAutomaticRefreshInMinutes();
			}
		}

    }
    
    /**
     * This method is used to create a MsoProperties file cache and factory.
	 * The ID is kept in cache even if the config fails to be loaded.
	 * This is used to maintain the config ID until someone fixes the config file.
     *
     * @param msoPropertiesID A string representing the key of the config
     * @param propertiesFilePath The mso properties file to load
     *
     * @throws MsoPropertiesException In case of issues with the mso properties loading
     *
     * @see MsoPropertiesFactory#getMsoJavaProperties()
     * @see MsoPropertiesFactory#getMsoJsonProperties()
     */
    public void initializeMsoProperties (String msoPropertiesID,
                                         String propertiesFilePath) throws MsoPropertiesException {
             
        rwl.writeLock ().lock ();
        
        String msoPropPath="none";
        MsoPropertiesParameters msoPropertiesParams=new MsoPropertiesParameters();
        try {
        	msoPropPath = prefixMsoPropertiesPath + propertiesFilePath; 
        	if (msoPropertiesCache.get (msoPropertiesID) != null) {
                throw new MsoPropertiesException ("The factory contains already an instance of this mso properties: "
                                                  + msoPropPath);
            }
        	// Create the global MsoProperties object
        	msoPropertiesParams = createObjectType(msoPropertiesParams, msoPropPath);

        } catch (FileNotFoundException e) {
            throw new MsoPropertiesException ("Unable to load the MSO properties file because it has not been found:"
                                              + msoPropPath, e);

        } catch (IOException e) {
            throw new MsoPropertiesException ("Unable to load the MSO properties file because IOException occurs: "
                                              + msoPropPath, e);
        } finally {
        	// put it in all cases, just to not forget about him and attempt a default reload
        	msoPropertiesCache.put (msoPropertiesID, msoPropertiesParams);
            rwl.writeLock ().unlock ();
        }
    }

    public void removeMsoProperties (String msoPropertiesID) throws MsoPropertiesException {

        rwl.writeLock ().lock ();
        try {
            if (MsoPropertiesFactory.msoPropertiesCache.remove (msoPropertiesID) == null) {
                throw new MsoPropertiesException ("Mso properties not found in cache:" + msoPropertiesID);
            }
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    /**
     * This method clears all the configs in cache, the factory will then be free of any config.
     * 
     * @see MsoPropertiesFactory#initializeMsoProperties(String, String)
     */
    public void removeAllMsoProperties () {

        rwl.writeLock ().lock ();
        try {
            MsoPropertiesFactory.msoPropertiesCache.clear ();
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    /**
     * THis method can be used to change the file and timer fields of an existing MSO properties file.
     *
     * @param msoPropertiesID The MSO properties ID
     * @param newMsoPropPath The new file Path
     * @throws MsoPropertiesException In case of the MSO Properties is not found in cache
     */
    public void changeMsoPropertiesFilePath (String msoPropertiesID,
                                             String newMsoPropPath) throws MsoPropertiesException {

        rwl.writeLock ().lock ();
        try {
        	MsoPropertiesParameters msoPropInCache = MsoPropertiesFactory.msoPropertiesCache.get (msoPropertiesID);

            if (msoPropInCache != null) {
                msoPropInCache.msoProperties.propertiesFileName = prefixMsoPropertiesPath + newMsoPropPath;
                
            } else {
                throw new MsoPropertiesException ("Mso properties not found in cache:" + msoPropertiesID);
            }
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    private AbstractMsoProperties getAndCloneProperties(String msoPropertiesID, MsoPropertiesParameters.MsoPropertiesType type)
        throws MsoPropertiesException, CloneNotSupportedException {
    	 rwl.readLock ().lock ();
         try {
         	MsoPropertiesParameters msoPropInCache = MsoPropertiesFactory.msoPropertiesCache.get (msoPropertiesID);
             if (msoPropInCache == null) {
                 throw new MsoPropertiesException ("Mso properties not found in cache:" + msoPropertiesID);
             } else {
                 if (type.equals(msoPropInCache.msoPropertiesType)) {
                 	return msoPropInCache.msoProperties.clone ();
                 } else {
                 	throw new MsoPropertiesException ("Mso properties is not "+type.name()+" properties type:" + msoPropertiesID);
                 }
             }
         } finally {
             rwl.readLock ().unlock ();
         }
    }
    
    /**
     * Get the MSO Properties (As Java Properties) as a copy of the mso properties cache.
     * The object returned can therefore be modified.
     *
     * @return A copy of the mso properties, properties class can be empty if the file has not been read properly
     * @throws MsoPropertiesException If the mso properties does not exist in the cache
     */
    public MsoJavaProperties getMsoJavaProperties (String msoPropertiesID) throws MsoPropertiesException {

    	return (MsoJavaProperties)getAndCloneProperties(msoPropertiesID,MsoPropertiesParameters.MsoPropertiesType.JAVA_PROP);
    }
    
    /**
     * Get the MSO Properties (As JSON Properties) as a copy of the mso properties cache.
     * The object returned can therefore be modified.
     *
     * @return A copy of the mso properties, properties class can be empty if the file has not been read properly
     * @throws MsoPropertiesException If the mso properties does not exist in the cache
     */
    public MsoJsonProperties getMsoJsonProperties (String msoPropertiesID) throws MsoPropertiesException {

    	return (MsoJsonProperties)getAndCloneProperties(msoPropertiesID,MsoPropertiesParameters.MsoPropertiesType.JSON_PROP);
    }

    /**
     * Get all MSO Properties as a copy of the mso properties cache.
     * The objects returned can therefore be modified.
     *
     * @return A List of copies of the mso properties, can be empty
     */
    public List <AbstractMsoProperties> getAllMsoProperties () throws CloneNotSupportedException {

        List <AbstractMsoProperties> resultList = new LinkedList <AbstractMsoProperties> ();
        rwl.readLock ().lock ();
        try {
        	for (MsoPropertiesParameters msoProp:MsoPropertiesFactory.msoPropertiesCache.values ()) {
        		resultList.add(msoProp.msoProperties.clone());
        	}
            return resultList;
        } finally {
            rwl.readLock ().unlock ();
        }
    }

    /**
     * This method is not intended to be called, it's used to refresh the config automatically
     *
     * @return true if Properties have been reloaded, false otherwise
     */
    @Schedule(minute = "*/1", hour = "*", persistent = false)
    public boolean reloadMsoProperties () {
    	AbstractMsoProperties msoPropInCache = null;
        try {
            if (!rwl.writeLock ().tryLock () && !rwl.writeLock ().tryLock (30L, TimeUnit.SECONDS)) {
                LOGGER.debug ("Busy write lock on mso properties factory, skipping the reloading");
                return false;
            }
        } catch (InterruptedException e1) {
            LOGGER.debug ("Interrupted while trying to acquire write lock on mso properties factory, skipping the reloading");
            Thread.currentThread ().interrupt ();
            return false;
        }
        try {
            for (Entry <String, MsoPropertiesParameters> entryMsoPropTimer : MsoPropertiesFactory.msoPropertiesCache.entrySet ()) {

                if (entryMsoPropTimer.getValue ().refreshCounter <= 1) {
                    // It's time to reload the config
                    msoPropInCache = MsoPropertiesFactory.msoPropertiesCache.get (entryMsoPropTimer.getKey ()).msoProperties;
                    try {
                    	AbstractMsoProperties oldProps = msoPropInCache.clone ();
                        msoPropInCache.reloadPropertiesFile ();
                        entryMsoPropTimer.getValue().refreshCounter=entryMsoPropTimer.getValue().msoProperties.getAutomaticRefreshInMinutes();
                     
                        if (!msoPropInCache.equals (oldProps)) {
                            LOGGER.info (MessageEnum.LOAD_PROPERTIES_SUC, msoPropInCache.getPropertiesFileName (), "", "");
                        }
                    } catch (FileNotFoundException ef) {
                        LOGGER.error (MessageEnum.NO_PROPERTIES, msoPropInCache.propertiesFileName, "", "", MsoLogger.ErrorCode.PermissionError, "", ef);
                    } catch (Exception e) {
                        LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, msoPropInCache.propertiesFileName, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "", e);
                    }

                } else {
                	--entryMsoPropTimer.getValue().refreshCounter;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.error (MessageEnum.LOAD_PROPERTIES_FAIL, "Unknown. Global issue while reloading", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "", e);
            return false;
        } finally {
            rwl.writeLock ().unlock ();
        }
    }

    /**
     * This method can be used to known if the MSO properties instance hold is different from the one in cache
     *
     * @param msoPropertiesID The MSO properties ID
     * @param oldMsoProperties The MSO Properties instance that must be compared to
     * @return True if they are the same, false otherwise
     * @throws MsoPropertiesException 
     */
    public boolean propertiesHaveChanged (String msoPropertiesID, AbstractMsoProperties oldMsoProperties) throws MsoPropertiesException {
        rwl.readLock ().lock ();
        try {
        	if (MsoPropertiesFactory.msoPropertiesCache.get (msoPropertiesID) == null) {
        		throw new MsoPropertiesException ("Mso properties not found in cache:" + msoPropertiesID);
        	}
        		 
        	AbstractMsoProperties msoPropInCache = MsoPropertiesFactory.msoPropertiesCache.get (msoPropertiesID).msoProperties;
            if (oldMsoProperties != null) {
                return !oldMsoProperties.equals (msoPropInCache);
            } else {
                return msoPropInCache != null;
            }
        } finally {
            rwl.readLock ().unlock ();
        }
    }

    @GET
    @Path("/show")
    @Produces("text/plain")
    public Response showProperties () {

        List <AbstractMsoProperties> listMsoProp = this.getAllMsoProperties ();
        StringBuffer response = new StringBuffer ();

        if (listMsoProp.isEmpty ()) {
            response.append ("No file defined");
        }

        for (AbstractMsoProperties properties : listMsoProp) {

        	response.append(properties.toString());
        }

        return Response.status (200).entity (response).build ();
    }

    @GET
    @Path("/encrypt/{value}/{cryptKey}")
    @Produces("text/plain")
    public Response encryptProperty (@PathParam("value") String value, @PathParam("cryptKey") String cryptKey) {
        try {
            String encryptedValue = CryptoUtils.encrypt (value, cryptKey);
            return Response.status (200).entity (encryptedValue).build ();
        } catch (Exception e) {
            LOGGER.error (MessageEnum.GENERAL_EXCEPTION_ARG, "Encryption error", "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Error in encrypting property", e);
            return Response.status (500).entity (e.getMessage ()).build ();
        }
    }
}
