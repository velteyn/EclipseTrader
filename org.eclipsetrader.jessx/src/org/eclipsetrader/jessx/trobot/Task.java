// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.trobot;

import java.util.TimerTask;

public class Task extends TimerTask
{
    private Robot robot;
    private long Delay;
    
    public Task(final Robot robot, final long Delay) {
        this.robot = robot;
        this.Delay = Delay;
    }
    
    @Override
    public void run() {
        this.robot.Act();
    }
}
