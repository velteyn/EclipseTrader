package org.eclipsetrader.jessx.business;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipsetrader.jessx.business.event.PlayerTypeEvent;
import org.eclipsetrader.jessx.business.event.PlayerTypeListener;
import org.eclipsetrader.jessx.business.event.ProgrammedInfoEvent;
import org.eclipsetrader.jessx.business.event.ProgrammedInfoListener;
import org.eclipsetrader.jessx.utils.Utils;
import org.eclipsetrader.jessx.utils.XmlExportable;
import org.eclipsetrader.jessx.utils.XmlLoadable;
import org.jdom.Content;
import org.jdom.Element;

public class Scenario implements XmlExportable, XmlLoadable {
	private HashMap playerTypes = new HashMap<Object, Object>();

	private Vector playerTypeListeners = new Vector();

	private List<InformationItem> informationItems = new ArrayList<InformationItem>();

	private Vector infoListners = new Vector();

    private List<NewsItem> newsItems = new ArrayList<NewsItem>();

	private String password = "";

	private boolean passwordUsed = false;

	private boolean listOfParticipantsUsed = false;

	private Vector listOfParticipants = new Vector();

	public DividendLimitation getDividendInfo(String assetName, String playerType) {
		Utils.logger.debug("Getting dividend info for asset : " + assetName + " and playerType: " + playerType);
		return ((PlayerType) this.playerTypes.get(playerType)).getDividendInfo(assetName);
	}

	public void setDividendInfo(String assetName, String playerType, DividendLimitation value) {
		((PlayerType) this.playerTypes.get(playerType)).setDividendInfo(assetName, value);
	}

	public PlayerType getPlayerType(String name) {
		return (PlayerType) this.playerTypes.get(name);
	}

	public HashMap getPlayerTypes() {
		return this.playerTypes;
	}

	public void addPlayerType(PlayerType playerType) {
		if (playerType != null) {
			this.playerTypes.put(playerType.getPlayerTypeName(), playerType);
			firePlayerTypeAdded(playerType);
		}
	}

	public void removePlayerType(PlayerType playerType) {
		if (playerType != null && this.playerTypes.containsValue(playerType)) {
			this.playerTypes.remove(playerType.getPlayerTypeName());
			firePlayerTypeRemoved(playerType);
		}
	}

	protected void firePlayerTypeAdded(PlayerType playerType) {
		for (int i = 0; i < this.playerTypeListeners.size(); i++)
			((PlayerTypeListener) this.playerTypeListeners.elementAt(i)).playerTypeModified(new PlayerTypeEvent(playerType, 1, null));
	}

	protected void firePlayerTypeRemoved(PlayerType playerType) {
		for (int i = 0; i < this.playerTypeListeners.size(); i++)
			((PlayerTypeListener) this.playerTypeListeners.elementAt(i)).playerTypeModified(new PlayerTypeEvent(playerType, 0, null));
	}

	public void addPlayerTypeListener(PlayerTypeListener listener) {
		this.playerTypeListeners.add(listener);
	}

	public void removePlayerTypeListener(PlayerTypeListener listener) {
		this.playerTypeListeners.remove(listener);
	}

	public List<InformationItem> getListInformation() {
		return this.informationItems;
	}

	protected void fireInfoAdded(String[] info) {
		for (int i = 0; i < this.infoListners.size(); i++) {
			((ProgrammedInfoListener) this.infoListners.elementAt(i)).programmedInfoModified(new ProgrammedInfoEvent(info, 2));
			Utils.logger.debug("fireadd" + info);
		}
	}

	protected void fireInfoRemoved(int num) {
		for (int i = 0; i < this.infoListners.size(); i++) {
			((ProgrammedInfoListener) this.infoListners.elementAt(i)).programmedInfoModified(new ProgrammedInfoEvent(new Integer(num), 0));
			Utils.logger.debug("fireremov" + num);
		}
	}

	protected void fireInfoClear() {
		for (int i = 0; i < this.infoListners.size(); i++)
			((ProgrammedInfoListener) this.infoListners.elementAt(i)).programmedInfoModified(new ProgrammedInfoEvent(new Integer(-1), 1));
		Utils.logger.debug("fireremovall-1");
	}

	public void addProgrammedInfoListener(ProgrammedInfoListener listener) {
		this.infoListners.add(listener);
	}

	public void removedInfoListener(ProgrammedInfoListener listener) {
		this.infoListners.remove(listener);
	}

	protected void fireProgammedInfoLoad() {
		for (int i = 0; i < this.infoListners.size(); i++)
			((ProgrammedInfoListener) this.infoListners.elementAt(i)).programmedInfoModified(new ProgrammedInfoEvent(this.informationItems, 3));
	}

	public void saveToXml(Element node) {
		Utils.logger.debug("Saving scenario...");
		Vector<String> keys = Utils.convertAndSortMapToVector(getPlayerTypes());
		int keysCount = keys.size();
		for (int i = 0; i < keysCount; i++) {
			Element ptNode = new Element("PlayerType");
			getPlayerType(keys.get(i)).saveToXml(ptNode);
			node.addContent((Content) ptNode);
		}
		Element informationNode = new Element("Information");
		for (InformationItem item : this.informationItems) {
			Element infoNode = new Element("Information");
			infoNode.setAttribute("Content", item.getContent());
			infoNode.setAttribute("Category", item.getCategory());
			infoNode.setAttribute("Period", item.getPeriod());
			infoNode.setAttribute("Time", item.getTime());
			informationNode.addContent((Content) infoNode);
		}
		node.addContent((Content) informationNode);
	}

	public void loadFromXml(Element node) {
		Utils.logger.debug("Loading scenario...");
		Iterator<Element> ptIter = node.getChildren("PlayerType").iterator();
		while (ptIter.hasNext()) {
			Element ptElem = ptIter.next();
			PlayerType pt = new PlayerType(ptElem);
			addPlayerType(pt);
		}
        Element infoParent = node.getChild("Information");
        if (infoParent != null) {
            this.informationItems.clear();
            Iterator<Element> infoIter = infoParent.getChildren("Information").iterator();
            while (infoIter.hasNext()) {
                Element infoElem = infoIter.next();
                String infoContent = infoElem.getAttributeValue("Content");
                String infoCategory = infoElem.getAttributeValue("Category");
                String infoPeriod = infoElem.getAttributeValue("Period");
                String infoTime = infoElem.getAttributeValue("Time");
                this.informationItems.add(new InformationItem(infoContent, infoCategory, infoPeriod, infoTime));
            }
        } else {
            Utils.logger.debug("Scenario Information node missing; skipping programmed info load.");
        }

        fireProgammedInfoLoad();

        Element newsElement = node.getChild("News");
        if (newsElement != null) {
            Iterator<Element> newsIter = newsElement.getChildren("Item").iterator();
            while (newsIter.hasNext()) {
                Element newsElem = newsIter.next();
                String priority = newsElem.getAttributeValue("priority");
                String asset = newsElem.getAttributeValue("asset");
                String sentiment = newsElem.getAttributeValue("sentiment");
                String text = newsElem.getText();
                newsItems.add(new NewsItem(priority, asset, text, sentiment));
            }
        }
	}

    public List<NewsItem> getNewsItems() {
        return newsItems;
    }

	public void setPasswordUsed(boolean b) {
		this.passwordUsed = b;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public boolean isPasswordUsed() {
		return this.passwordUsed;
	}

	public void setlistOfParticipantsUsed(boolean b) {
		this.listOfParticipantsUsed = b;
	}

	public void setlistOfParticipants(Vector listOfParticipants) {
		this.listOfParticipants = listOfParticipants;
	}

	public Vector getlistOfParticipants() {
		return this.listOfParticipants;
	}

	public boolean islistOfParticipantsUsed() {
		return this.listOfParticipantsUsed;
	}
}
