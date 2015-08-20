package com.amazonaws.ant.codedeploy;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.amazonaws.services.codedeploy.model.CreateApplicationRequest;
import org.apache.tools.ant.BuildException;

public class CreateApplicationTask extends AWSAntTask
{

    private String applicationName;

    private void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (applicationName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: applicationName is required. \n");
        }

        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }

    }

    @Override
    public void execute() throws BuildException
    {
        checkParams();
        AmazonCodeDeployClient client = getOrCreateClient(AmazonCodeDeployClient.class);
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest()
                .withApplicationName(this.applicationName);

        try {
            client.createApplication(createApplicationRequest);
            System.out.println("Create application " + applicationName + " request submitted.");
        } catch (Exception e) {
            throw new BuildException("Could not create application " + e.getMessage(), e);
        }
    }

    /**
     * Set the name of this application. Required.
     *
     * @param applicationName
     *            The application name
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
