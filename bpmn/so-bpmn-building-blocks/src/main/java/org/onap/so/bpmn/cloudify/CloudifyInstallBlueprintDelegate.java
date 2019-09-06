package org.onap.so.bpmn.cloudify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.cloudify.client.APIV31;
import org.onap.so.cloudify.client.APIV31Impl;
import org.onap.so.cloudify.client.DeploymentV31;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a simple synchronous blueprint installation.
 *
 * 
 * @author dewayne
 *
 */
public class CloudifyInstallBlueprintDelegate extends AbstractJavaDelegate {
    private static Logger log = LoggerFactory.getLogger(CloudifyInstallBlueprintDelegate.class);

    // limitation: only archives with blueprint.yaml will work
    private final static String INP_BLUEPRINT_KEY = "InputCfy_blueprint";
    private final static String INP_CREDENTIALS_KEY = "InputCfy_credentials";
    private final static String INP_BLUEPRINT_YAML_KEY = "InputCfy_blueprint_yaml";
    private final static String INP_BLUEPRINT_NAME_KEY = "InputCfy_blueprint_name";
    private final static String INP_DPMT_INPUTS_KEY = "InputCfy_deployment_inputs";
    private final static String INSTALL_WF = "install";
    private final static String DEFAULT_BP_FILENAME = "blueprint.yaml";


    /**
     * Performs a simple blueprint installation. That means: - single blueprint YAML file - user/password authorization
     * - the deployment name in Cloudify will be the same as the blueprint name - the blueprint will have tenant
     * visibility
     */
    public void execute(DelegateExecution execution) throws Exception {
        checkInputs(execution);

        String blueprint = (String) execution.getVariable(INP_BLUEPRINT_KEY);
        String blueprint_name = (String) execution.getVariable(INP_BLUEPRINT_NAME_KEY);
        @SuppressWarnings("unchecked")
        Map<String, String> credentials = (Map<String, String>) execution.getVariable(INP_CREDENTIALS_KEY);
        Map<String, String> inputs = execution.hasVariable(INP_DPMT_INPUTS_KEY)
                ? (Map<String, String>) execution.getVariable(INP_DPMT_INPUTS_KEY)
                : new HashMap<>();


        APIV31Impl client = getCloudifyClient(credentials);

        // Upload blueprint
        try {
            uploadBlueprint(client, blueprint, blueprint_name);
        } catch (Exception e) {
            log.error("Cloudify blueprint upload failed: " + e.getMessage());
            throw e;
        }

        // Create deployment
        String did = null;
        try {
            did = createDeployment(client, blueprint_name, inputs);
        } catch (Exception e) {
            log.error("Cloudify deployment creation failed: " + e.getMessage());
            throw e;
        }

        // Run install workflow
        try {
            runWorkflow(INSTALL_WF, execution, client, did, null);
        } catch (Exception e) {
            log.error("Cloudify install workflow failed: " + e.getMessage());
            throw e;
        }
    }

    /******************************************************************
     * PRIVATE METHODS
     ******************************************************************/

    private void checkInputs(DelegateExecution execution) throws Exception {

        StringBuilder sb = new StringBuilder();
        if (!execution.hasVariable(INP_BLUEPRINT_KEY)) {
            sb.append("required input not supplied: " + INP_BLUEPRINT_KEY + "\n");
        }
        if (!execution.hasVariable(INP_CREDENTIALS_KEY)) {
            sb.append("required input not supplied: " + INP_CREDENTIALS_KEY + "\n");
        } else {
            Map<String, String> creds = (Map<String, String>) execution.getVariable(INP_CREDENTIALS_KEY);
            if (!creds.containsKey("url")) {
                sb.append("required credentials entry not supplied: url\n");
            }
            if (!creds.containsKey("username")) {
                sb.append("required credentials entry not supplied: username\n");
            }
            if (!creds.containsKey("password")) {
                sb.append("required credentials entry not supplied: password\n");
            }
            if (!creds.containsKey("tenant")) {
                sb.append("required credentials entry not supplied: tenant\n");
            }
        }
        if (!execution.hasVariable(INP_CREDENTIALS_KEY)) {
            sb.append("required input not supplied: " + INP_CREDENTIALS_KEY + "\n");
        }
        if (!execution.hasVariable(INP_BLUEPRINT_NAME_KEY)) {
            sb.append("required input not supplied: " + INP_BLUEPRINT_NAME_KEY + "\n");
        }
        if (sb.length() > 0) {
            throw new Exception(sb.toString());
        }
    }

    /**
     * Create a Cloudify deployment with reasonable defaults
     * 
     * @param client the Cloudify client instance
     * @param bid the blueprint id
     * @param inputs the inputs if any
     * @return the deployment id
     */
    private String createDeployment(APIV31Impl client, String bid, Map<String, String> inputs) {
        log.info("creating deployment: " + bid);
        DeploymentV31 deployment = client.createDeployment(bid, bid, inputs, false, false, APIV31.Visibility.TENANT);
        return bid;
    }


    /**
     * Create an archive and push it to Cloudify
     * 
     * @param execution
     * @param client
     * @param blueprint the actual blueprint yaml
     * @param the
     * @throws Exception
     */
    private void uploadBlueprint(APIV31Impl client, String blueprint, String blueprint_name) throws Exception {
        log.info("uploading blueprint '" + blueprint_name + "'");

        // Create archive
        File archive = this.createBlueprintArchive(blueprint);

        // Upload
        FileInputStream fis = null;
        byte[] data = new byte[(int) archive.length()];
        try {
            fis = new FileInputStream(archive);
            fis.read(data);
        } finally {
            fis.close();
            archive.delete();
        }

        client.uploadBlueprint(blueprint_name, DEFAULT_BP_FILENAME, APIV31.Visibility.TENANT, data);

    }

    /**
     * Create a valid blueprint archive from the supplied blueprint file contents
     * 
     * @param blueprint the blueprint
     * @return a File object pointing to the archive
     */
    private File createBlueprintArchive(String blueprint) {
        String dirname = makeTempFileName("sobpmn", null);
        File dir = new File(dirname);
        if (!dir.mkdir()) {
            log.error("archive directory creation failed");
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(dirname + File.separator + "blueprint.yaml");
            fos.write(blueprint.getBytes());
            fos.close();
        } catch (Exception e) {
            log.error("error writing blueprint file:" + e.getMessage());
            return null;
        }

        File zip = null;
        try {
            zip = createSimpleZipFile(dirname);
        } catch (Exception e) {
            log.error("error creating zip file: " + e.getMessage());
            return null;
        }

        return zip;
    }

    /**
     * Creates a zip file that includes the supplied directory. File placed in same directory as supplied directory. NOT
     * a general zip function.
     * 
     * @param dirname directory to zip
     * @return A File object pointing to the zip
     * @throws IOException
     */
    private File createSimpleZipFile(String dirname) throws IOException {
        final Path sourceDir = Paths.get(dirname);
        String zipFileName = dirname.concat(".zip");
        final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                try {
                    Path targetFile = file.subpath(1, file.getNameCount());
                    outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                    byte[] bytes = Files.readAllBytes(file);
                    outputStream.write(bytes, 0, bytes.length);
                    outputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new UncheckedIOException(e);
                }
            }
        });
        outputStream.close();
        return new File(zipFileName);
    }

    /**
     * Create a temporary file name/path based in the system temp dir
     * 
     * @param prefix added to generated name at beginning followed by '-'
     * @param suffix added to end (if not null), following .'.'
     * @return the name
     */
    private String makeTempFileName(String prefix, String suffix) {
        String extension = suffix;
        String start = prefix + "-";
        if (prefix == null) {
            start = "";
        }
        if (suffix == null) {
            extension = "";
        }
        String path = System.getProperty("java.io.tmpdir") + File.separator + start + UUID.randomUUID().toString()
                + extension;
        log.debug("temp path=" + path);
        return path;
    }
}
