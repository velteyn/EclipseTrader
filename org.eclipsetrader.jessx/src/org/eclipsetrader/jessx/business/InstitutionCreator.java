﻿// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business;

import java.util.Iterator;
import java.util.Hashtable;

import org.eclipsetrader.jessx.utils.Utils;

public abstract class InstitutionCreator
{
    public static Hashtable institutionFactories;
    
    static {
        InstitutionCreator.institutionFactories = new Hashtable();
    }
    
    public static Institution createInstitution(final String name) throws InstitutionNotCreatedException {
        Utils.logger.info("Trying to create the following institution: " + name);
        Utils.logger.debug("Looking for its class in the Hashtable.");
        final Class institutionClass = (Class) InstitutionCreator.institutionFactories.get(name);
        if (institutionClass == null) {
            Utils.logger.error("Class not found. The institution has never been loaded.");
            Utils.logger.error("As all institutions from the modules/institutions directory has already been loaded, we are looking in the classPath.");
            Utils.logger.error("Here is a list of loaded institution: ");
            final Iterator inIter = InstitutionCreator.institutionFactories.keySet().iterator();
            while (inIter.hasNext()) {
                Utils.logger.error(inIter.next().toString());
            }
            throw new InstitutionNotCreatedException();
        }
        Utils.logger.debug("Returning the result of the create method of the factory: the institution.");
        try {
            return (Institution) institutionClass.newInstance();
        }
        catch (IllegalAccessException ex) {
            Utils.logger.error("error creating the requested institution: " + ex.toString());
            ex.printStackTrace();
            throw new InstitutionNotCreatedException();
        }
        catch (InstantiationException ex2) {
            Utils.logger.error("error creating the requested institution: " + ex2.toString());
            ex2.printStackTrace();
            throw new InstitutionNotCreatedException();
        }
    }
}
