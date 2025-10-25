// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.client;


import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.Operator;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.net.OperatorPlayed;
import org.eclipsetrader.jessx.utils.Utils;
import org.jdom.Document;


public class DataManager implements NetworkListener
{
    public DataManager() {
        ClientCore.addNetworkListener(this, "Institution");
        ClientCore.addNetworkListener(this, "Portfolio");
        ClientCore.addNetworkListener(this, "OperatorPlayed");
    }
    
    public void objectReceived(final Document doc) {
        if (doc.getRootElement().getName().equals("Portfolio")) {
            ClientCore.getPortfolio().initFromNetworkInput(doc.getRootElement());
        }
        else if (doc.getRootElement().getName().equals("OperatorPlayed")) {
            final OperatorPlayed opPlayed = new OperatorPlayed("");
            if (opPlayed.initFromNetworkInput(doc.getRootElement())) {
                final Institution inst = ClientCore.getInstitution(opPlayed.getInstitutionName());
                if (inst != null) {
                    final Operator op = inst.getOperator(opPlayed.getOperatorName());
                    if (op != null) {
                        ClientCore.addOperatorPlayed(op);
                    }
                    else {
                        Utils.logger.warn("The operator has not been found on the institution given. (" + opPlayed.getInstitutionName() + ", " + opPlayed.getOperatorName() + ")");
                    }
                }
                else {
                    Utils.logger.warn("Operator plays on an institution we did not have. (" + opPlayed.getInstitutionName() + ", " + opPlayed.getOperatorName() + ")");
                }
            }
        }
        else if (doc.getRootElement().getName().equals("Institution")) {
            final Institution instit = Institution.loadInstitutionFromXml(doc.getRootElement());
            ClientCore.addInstitution(instit);
        }
    }
}
