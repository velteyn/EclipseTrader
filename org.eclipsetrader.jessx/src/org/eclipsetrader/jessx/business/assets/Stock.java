// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business.assets;

import org.eclipsetrader.jessx.business.Asset;
import org.eclipsetrader.jessx.business.AssetCreator;
import org.jdom.Element;
import javax.swing.JPanel;




public class Stock extends Asset
{
    static {
        try {
            System.out.println("Loading stock...");
            AssetCreator.assetFactories.put("Stock", Class.forName("org.eclipsetrader.jessx.business.assets.Stock"));
        }
        catch (ClassNotFoundException exception) {
            System.out.println("Unabled to locate the Stock class. Reason: bad class name spelling.");
            exception.printStackTrace();
        }
    }
    
    @Override
    public JPanel getAssetSetupGui() {
    	 try {
			throw new Exception("EDOZ sono schiantato !!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public void saveToXml(final Element node) {
    }
    
    public void loadFromXml(final Element node) {
    }
}
