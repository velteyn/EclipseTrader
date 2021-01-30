// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipse.trader.jessx.business;

public class OperationNotCreatedException extends Exception
{
    public OperationNotCreatedException() {
        super("Operation could not be created.");
    }
}
