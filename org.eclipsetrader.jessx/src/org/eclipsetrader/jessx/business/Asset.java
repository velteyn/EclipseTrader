// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.business;


import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.jessx.utils.Utils;
import org.eclipsetrader.jessx.utils.XmlExportable;
import org.eclipsetrader.jessx.utils.XmlLoadable;
import org.jdom.Content;
import org.jdom.Element;

import javax.swing.JPanel;


public abstract class Asset implements XmlExportable, XmlLoadable
{
    private String name;
    private DividendModel dividendModel;
    private String activity;
    
    transient ISecurity security;

    public ISecurity getSecurity() {
		return security;
	}

	public void setSecurity(ISecurity security) {
		this.security = security;
	}

	public Asset() {
        this.dividendModel = new DividendModel();
    }
    
    public void setAssetName(final String assetname) {
        this.name = assetname;
    }
    
    public void setDividendModel(final DividendModel divModel) {
        this.dividendModel = divModel;
    }
    
    public DividendModel getDividendModel() {
        return this.dividendModel;
    }
    
    public String getAssetName() {
        return this.name;
    }
    
    public static JPanel getAssetServerGenericGui() {
        //return new AssetServerGenericGui();
    	return null; //edoz modify
    }
    
    public abstract JPanel getAssetSetupGui();
    
    public JPanel getPanel() {
        return this.getAssetSetupGui();
    }
    
    @Override
    public String toString() {
        return this.getAssetName();
    }
    
    public String getAssetType() {
        return this.getClass().toString().substring(this.getClass().toString().lastIndexOf(".") + 1);
    }
    
    public void setAssetActivity(final String assetActivity) {
        this.activity = assetActivity;
    }
    
    public String getAssetActivity() {
        return this.activity;
    }
    
    public static void saveAssetToXml(final Element node, final Asset assetToSave) {
        node.setAttribute("type", assetToSave.getAssetType()).setAttribute("name", assetToSave.getAssetName());
        final Element divModelNode = new Element("DividendModel");
        assetToSave.getDividendModel().saveToXml(divModelNode);
        node.addContent(divModelNode);
        assetToSave.saveToXml(node);
    }
    
    public static Asset loadAssetFromXml(final Element node) {
        final String assetType = node.getAttributeValue("type");
        if (assetType == null) {
            Utils.logger.error("Invlid xml: no asset type in asset definition.");
            return null;
        }
        Asset asset;
        try {
            asset = AssetCreator.createAsset(assetType);
        }
        catch (AssetNotCreatedException ex) {
            Utils.logger.error("Asset type not found on server: " + ex.toString());
            return null;
        }
        final String assetName = node.getAttributeValue("name");
        if (assetName == null) {
            Utils.logger.error("Invalid asset definition in xml files: no asset name given.");
            return null;
        }
        asset.setAssetName(assetName);
        final DividendModel divModel = new DividendModel();
        divModel.loadFromXml(node.getChild("DividendModel"));
        asset.setDividendModel(divModel);
        return asset;
    }
}
