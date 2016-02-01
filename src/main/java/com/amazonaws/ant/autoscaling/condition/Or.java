package com.amazonaws.ant.autoscaling.condition;

import com.amazonaws.ant.autoscaling.Condition;

import java.util.ArrayList;
import java.util.List;

public class Or implements Condition
{

    private List<Condition> conditions = new ArrayList<Condition>();

    public void add(Condition condition) {
        this.conditions.add(condition);
    }

    @Override
    public boolean eval(String value)
    {
        for(Condition condition : this.conditions) {
            if(condition.eval(value))
            {
                return true;
            }
        }
        return false;
    }

}
