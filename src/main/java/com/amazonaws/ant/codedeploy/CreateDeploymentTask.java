package com.amazonaws.ant.codedeploy;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.opsworks.Constants;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.amazonaws.services.codedeploy.model.*;
import org.apache.tools.ant.BuildException;

public class CreateDeploymentTask extends AWSAntTask
{

    private static final String SUCCEEDED = "Succeeded";

    private String applicationName;
    private String deploymentGroupName;
    private String deploymentConfigName;
    private String description;
    private Boolean ignoreApplicationStopFailures;
    private RevisionLocation revisionLocation = null;

    private String propertyNameForDeploymentId = Constants.DEPLOYMENT_ID_PROPERTY;
    private boolean waitForDeployment = false;


    /**
     * Set the name of this application. Required.
     *
     * @param applicationName The application name
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    /**
     * Set the name of this deployment group. Required.
     *
     * @param deploymentGroupName The deployment group name
     */
    public void setDeploymentGroupName(String deploymentGroupName)
    {
        this.deploymentGroupName = deploymentGroupName;
    }

    /**
     * Set the name of this deployment config. Required.
     *
     * @param deploymentConfigName The deployment config name
     */
    public void setDeploymentConfigName(String deploymentConfigName)
    {
        this.deploymentConfigName = deploymentConfigName;
    }

    /**
     * Set whether to ignore application stop failures. Not required.
     *
     * @param ignoreApplicationStopFailures Whether to ignore application stop failures.
     */
    public void setIgnoreApplicationStopFailures(boolean ignoreApplicationStopFailures)
    {
        this.ignoreApplicationStopFailures = ignoreApplicationStopFailures;
    }

    /**
     * Set the description of this deployment. Not required.
     *
     * @param description The deployment description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Allows you to add a preconfigured S3Revision nested element. Required.
     *
     * @param revision A preconfigured S3RevisionLocation object.
     */
    public void addConfiguredS3Revision(S3RevisionLocation revision)
    {
        if (this.revisionLocation != null)
        {
            System.out.println("Warning: It seems you've tried to set more than one revision."
                    + " You can only specify one revision per deployment."
                    + " Only the last revision you specify will be deployed.");
        }
        this.revisionLocation = revision.getRevisionLocation();
    }

    /**
     * Allows you to add a preconfigured GitHubRevision nested element. Required.
     *
     * @param revision A preconfigured GitHubRevisionLocation object.
     */
    public void addConfiguredGitHubRevision(GitHubRevisionLocation revision)
    {
        if (this.revisionLocation != null)
        {
            System.out.println("Warning: It seems you've tried to set more than one revision."
                    + " You can only specify one revision per deployment."
                    + " Only the last revision you specify will be deployed.");
        }
        this.revisionLocation = revision.getRevisionLocation();
    }

    /**
     * Set the name of the property to set this deployment ID to. Not required,
     * default is "deploymentId"
     *
     * @param propertyNameForDeploymentId The name of the property to set this deployment's ID to.
     */
    public void setPropertyNameForDeploymentId(String propertyNameForDeploymentId)
    {
        this.propertyNameForDeploymentId = propertyNameForDeploymentId;
    }

    /**
     * Set whether to block the build until this deployment successfully finishes.
     * Not required, default is false.
     *
     * @param waitForDeployment Whether to block the build until this deployment successfully
     *                          finishes
     */
    public void setWaitForDeployment(boolean waitForDeployment)
    {
        this.waitForDeployment = waitForDeployment;
    }

    private void checkParams()
    {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (applicationName == null)
        {
            areMalformedParams = true;
            errors.append("Missing parameter: applicationName is required. \n");
        }

        if (deploymentGroupName == null)
        {
            areMalformedParams = true;
            errors.append("Missing parameter: deploymentGroupName is required. \n");
        }

        if (deploymentConfigName == null)
        {
            areMalformedParams = true;
            errors.append("Missing parameter: deploymentConfigName is required. \n");
        }

        if (revisionLocation == null)
        {
            areMalformedParams = true;
            errors.append("Missing parameter: revision is required. \n");
        }

        if (areMalformedParams)
        {
            throw new BuildException(errors.toString());
        }

    }

    @Override
    public void execute() throws BuildException
    {
        checkParams();
        AmazonCodeDeployClient client = getOrCreateClient(AmazonCodeDeployClient.class);
        CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest()
                .withApplicationName(this.applicationName)
                .withDeploymentGroupName(this.deploymentGroupName)
                .withDeploymentConfigName(this.deploymentConfigName)
                .withDescription(this.description)
                .withIgnoreApplicationStopFailures(this.ignoreApplicationStopFailures)
                .withRevision(this.revisionLocation);

        String deploymentId;
        try
        {
            deploymentId = client.createDeployment(createDeploymentRequest).getDeploymentId();
            System.out.println("Create deployment " + applicationName + " request submitted.");

            if (waitForDeployment 
                    && !WaitForDeploymentToReachStateTask.waitForCodeDeployDeploymentToReachStatus(client, deploymentId, SUCCEEDED))
            {
                throw new BuildException("The code deploy deployment failed");
            }
        }
        catch (Exception e)
        {
            throw new BuildException("Could not create deployment " + e.getMessage(), e);
        }

        if (deploymentId != null)
        {
            if (this.propertyNameForDeploymentId.equals(Constants.DEPLOYMENT_ID_PROPERTY)
                    && getProject().getProperty(Constants.DEPLOYMENT_ID_PROPERTY) != null)
            {
                getProject().addReference(Constants.DEPLOYMENT_ID_PROPERTY, true);
            }
            else
            {
                getProject().addReference(Constants.DEPLOYMENT_ID_PROPERTY, false);
                getProject().setNewProperty(propertyNameForDeploymentId, deploymentId);
            }
        }
    }

    public interface Revision
    {
        RevisionLocation getRevisionLocation();
    }

    public static class S3RevisionLocation implements Revision
    {
        private String bucket;
        private String key;
        private String bundleType;
        private String version;
        private String etag;

        public void setBucket(String bucket)
        {
            this.bucket = bucket;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public void setBundleType(String bundleType)
        {
            this.bundleType = bundleType;
        }

        public void setVersion(String version)
        {
            this.version = version;
        }

        public void setEtag(String etag)
        {
            this.etag = etag;
        }

        @Override
        public RevisionLocation getRevisionLocation()
        {
            return new RevisionLocation()
                    .withRevisionType(RevisionLocationType.S3)
                    .withS3Location(new S3Location()
                            .withBucket(this.bucket)
                            .withKey(this.key)
                            .withBundleType(this.bundleType)
                            .withVersion(this.version)
                            .withETag(this.etag));
        }
    }

    public static class GitHubRevisionLocation implements Revision
    {

        private String repository;
        private String commitId;

        public void setRepository(String repository)
        {
            this.repository = repository;
        }

        public void setCommitId(String commitId)
        {
            this.commitId = commitId;
        }

        @Override
        public RevisionLocation getRevisionLocation()
        {
            return new RevisionLocation()
                    .withRevisionType(RevisionLocationType.GitHub)
                    .withGitHubLocation(new GitHubLocation()
                            .withRepository(this.repository)
                            .withCommitId(this.commitId));
        }
    }
}
