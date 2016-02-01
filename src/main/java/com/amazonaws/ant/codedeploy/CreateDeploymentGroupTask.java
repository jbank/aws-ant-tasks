package com.amazonaws.ant.codedeploy;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.KeyValueNestedElement;
import com.amazonaws.ant.SimpleNestedElement;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient;
import com.amazonaws.services.codedeploy.model.CreateDeploymentGroupRequest;
import com.amazonaws.services.codedeploy.model.EC2TagFilter;
import com.amazonaws.services.codedeploy.model.EC2TagFilterType;

import org.apache.tools.ant.BuildException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CreateDeploymentGroupTask extends AWSAntTask
{

    private String applicationName;
    private String deploymentGroupName;
    private String deploymentConfigName;
    private String serviceRoleArn;
    private List<EC2TagFilter> ec2TagFilters = new LinkedList<EC2TagFilter>();
    private List<String> autoScalingGroups = new LinkedList<String>();

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

        if (serviceRoleArn == null)
        {
            areMalformedParams = true;
            errors.append("Missing parameter: serviceRoleArn is required. \n");
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
        CreateDeploymentGroupRequest createDeploymentGroupRequest = new CreateDeploymentGroupRequest()
                .withApplicationName(this.applicationName)
                .withDeploymentGroupName(this.deploymentGroupName)
                .withDeploymentConfigName(this.deploymentConfigName)
                .withServiceRoleArn(this.serviceRoleArn);

        if(this.ec2TagFilters.size() > 0)
        {
            createDeploymentGroupRequest.setEc2TagFilters(this.ec2TagFilters);
        }
        if(this.autoScalingGroups.size() > 0)
        {
            createDeploymentGroupRequest.setAutoScalingGroups(this.autoScalingGroups);
        }

        try
        {
            client.createDeploymentGroup(createDeploymentGroupRequest);
            System.out.println("Create deployment group " + applicationName + " request submitted.");
        }
        catch (Exception e)
        {
            throw new BuildException("Could not create deployment group " + e.getMessage(), e);
        }
    }

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
     * Set the name of this service role arn. Required.
     *
     * @param serviceRoleArn The service role arn
     */
    public void setServiceRoleArn(String serviceRoleArn)
    {
        this.serviceRoleArn = serviceRoleArn;
    }

    /**
     * Allows you to add any number of nested preconfigured TagFilter
     * elements.
     *
     * @param tagFilter a preconfigured TagFilter object.
     */
    public void addConfiguredTagFilter(TagFilter tagFilter)
    {
        this.ec2TagFilters.add(new EC2TagFilter()
                .withKey(tagFilter.getKey())
                .withValue(tagFilter.getValue())
                .withType(tagFilter.getType()));
    }

    /**
     * Allows you to add any number of nested preconfigured AutoScaleGroup
     * elements.
     *
     * @param autoScalingGroup a preconfigured AutoScalingGroup object.
     */
    public void addConfiguredAutoScalingGroup(AutoScalingGroup autoScalingGroup)
    {
        List<String> autoScalingGroups = Arrays.asList(autoScalingGroup.getValue().split(autoScalingGroup.getDelimiter()));
        this.autoScalingGroups.addAll(autoScalingGroups);
    }

    /**
     * Nested element for specifying a TagFilter. Set the key to the name of the
     * tag, and the value to the value of the tag.
     */
    public static class TagFilter extends KeyValueNestedElement
    {
        private String type = EC2TagFilterType.KEY_AND_VALUE.toString();

        /**
         * Set whether to use the previous value when setting a parameter that
         * has already been set.
         *
         * @param type Whether to use the previous value of this parameter.
         */
        public void setType(String type)
        {
            this.type = type;
        }

        public String getType()
        {
            return this.type;
        }
    }

    /**
     * Nested element for specifying a AutoScalingGroup. Set the value to the
     * AutoScalingGroup you want to add to the deployment group.
     */
    public static class AutoScalingGroup extends SimpleNestedElement
    {
        private String delimiter = Constants.DEFAULT_DELIMITER;
        
        public void setDelimiter(String delimiter)
        {
            this.delimiter = delimiter;
        }
        
        public String getDelimiter()
        {
            return this.delimiter;
        }
        
    }
}
