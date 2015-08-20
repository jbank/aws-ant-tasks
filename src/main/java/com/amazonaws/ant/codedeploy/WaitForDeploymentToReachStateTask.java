package com.amazonaws.ant.codedeploy;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.amazonaws.services.codedeploy.model.GetDeploymentRequest;
import org.apache.tools.ant.BuildException;

import java.util.Arrays;
import java.util.List;

public class WaitForDeploymentToReachStateTask extends AWSAntTask
{
    private static final List<String> FAILED = Arrays.asList("Failed", "Stopped");
    private String deploymentId;
    private String status;

    /**
     * Set the id of the deployment. Required.
     *
     * @param deploymentId
     *            The deployment id
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * Set the status to wait for this deployment to reach. Should not contain
     * "Failed"
     *
     * @param status
     *            The status to wait for this deployment to reach.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Waits for the specified stack to reach the specified status. Returns true
     * if it does, returns false if it reaches a status with "FAILED", or if 30
     * minutes pass without reaching the desired status.
     */
    private void checkParams() {
        boolean areMissingParams = false;
        StringBuilder errors = new StringBuilder("");

        if (deploymentId == null) {
            areMissingParams = true;
            errors.append("Missing parameter: deploymentId is required \n");
        }

        if (status == null) {
            areMissingParams = true;
            errors.append("Missing parameter: status is required \n");
        }

        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AmazonCodeDeployClient client = getOrCreateClient(AmazonCodeDeployClient.class);
        if (!waitForCodeDeployDeploymentToReachStatus(client, deploymentId, status)) {
            throw new BuildException("The code deploy deployment failed");
        }
    }

    public static boolean waitForCodeDeployDeploymentToReachStatus(
            AmazonCodeDeployClient client, String deploymentId, String status) {
        int count = 0;
        while (true) {
            if (count++ == 100) {
                System.out
                        .println(deploymentId + " never reached state " + status);
                return false;
            }
            try {
                Thread.sleep(1000 * 30);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return false;
            }

            String deploymentStatus = client.getDeployment(new GetDeploymentRequest().withDeploymentId(deploymentId)).getDeploymentInfo().getStatus();
            if (deploymentStatus.equals(status)) {
                return true;
            } else if (FAILED.contains(deploymentStatus)) {
                System.out.println("The process failed with status " + deploymentStatus);
                return false;
            }
            System.out.println(deploymentId + " is in status " + deploymentStatus);
        }
    }
}
