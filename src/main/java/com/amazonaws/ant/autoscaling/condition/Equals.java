package com.amazonaws.ant.autoscaling.condition;

import com.amazonaws.ant.autoscaling.Condition;

public class Equals implements Condition
{

    private String arg;
    
    public void setArg(String arg)
    {
        this.arg = arg;
    }

    @Override
    public boolean eval( String value )
    {
        return arg.equals(value);
    }
    
}
