﻿// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business;

public class InstitutionNotCreatedException extends Exception
{
    public InstitutionNotCreatedException() {
        super("Institution could not be created.");
    }
}
