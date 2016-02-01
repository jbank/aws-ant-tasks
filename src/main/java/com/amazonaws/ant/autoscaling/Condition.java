package com.amazonaws.ant.autoscaling;

public interface Condition
{
    boolean eval(String value);
}
