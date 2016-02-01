package com.amazonaws.ant.autoscaling;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.autoscaling.Constants;
import com.amazonaws.ant.autoscaling.condition.Equals;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.TagDescription;
import com.amazonaws.util.StringUtils;


public class FindAutoScaleGroupsTask extends AWSAntTask
{

    private Map<String, AutoScalingGroupTag> tags = new HashMap<String, AutoScalingGroupTag>();
    private String propertyNameForAutoScalingGroupIds = Constants.AUTO_SCALING_GROUP_IDS_PROPERTY;
    private String delimiter = Constants.DEFAULT_DELIMITER;

    /**
     * Allows you to add any number of nested Auto Scaling Group tag elements.
     * 
     * @param autoScalingGroupTag
     *            a AutoScalingGroupTag object.
     */
    public void addConfiguredAutoScalingGroupTag( AutoScalingGroupTag autoScalingGroupTag )
    {
        tags.put(autoScalingGroupTag.getName(), autoScalingGroupTag);
    }

    public void setPropertyNameForAutoScalingGroupIds( String propertyNameForAutoScalingGroupIds )
    {
        this.propertyNameForAutoScalingGroupIds = propertyNameForAutoScalingGroupIds;
    }

    public void setDelimiter( String delimiter )
    {
        this.delimiter = delimiter;
    }

    private void checkParams()
    {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if( tags.isEmpty() )
        {
            areMalformedParams = true;
            errors.append("Missing parameter: tags are required. \n");
        }

        if( areMalformedParams )
        {
            throw new BuildException(errors.toString());
        }

    }

    @Override
    public void execute() throws BuildException
    {
        this.checkParams();

        AmazonAutoScaling client = getOrCreateClient(AmazonAutoScalingClient.class);
        try
        {
            List<String> autoScalingGroups = new ArrayList<String>();
            DescribeAutoScalingGroupsResult result = client.describeAutoScalingGroups();

            log("Found " + result.getAutoScalingGroups().size() + " AutoScalingGroups.", Project.MSG_DEBUG);
            for( AutoScalingGroup autoScalingGroup : result.getAutoScalingGroups() )
            {
                log("Checking AutoScalingGroup [" + autoScalingGroup.getAutoScalingGroupName() + "]", Project.MSG_DEBUG);
                for( TagDescription tag : autoScalingGroup.getTags() )
                {
                    AutoScalingGroupTag autoScalingGroupTag = this.tags.get(tag.getKey());
                    log("Looking for tag [" + tag.getKey() + "] -> " + (autoScalingGroupTag != null ? "[true]" : "[false]"), Project.MSG_DEBUG);
                    if( autoScalingGroupTag != null && autoScalingGroupTag.eval(tag.getValue()) )
                    {
                        log("Adding AutoScalingGroup named [" + autoScalingGroup.getAutoScalingGroupName() + "]", Project.MSG_DEBUG);
                        autoScalingGroups.add(autoScalingGroup.getAutoScalingGroupName());
                    }
                }

            }

            if( !autoScalingGroups.isEmpty() )
            {
                getProject().setProperty(propertyNameForAutoScalingGroupIds,
                        StringUtils.join(this.delimiter, autoScalingGroups.toArray(new String[autoScalingGroups.size()])));
            }
        }
        catch( Exception e )
        {
            throw new BuildException(
                    "Could not describe auto scaling groups " + e.getMessage(), e);
        }
    }

    /**
     * Nested element for specifying a AutoScalingGroupTag. Set the key to the
     * name of the parameter, and the value of the property to set.
     */
    public static class AutoScalingGroupTag implements Condition
    {

        private String name;
        private Condition condition;

        public void setName( String name )
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public void add( Condition condition )
        {
            this.condition = condition;
        }

        public boolean eval( String value )
        {
            return condition.eval(value);
        }

    }
}
