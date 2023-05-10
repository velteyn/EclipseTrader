package org.eclipsetrader.jessx.internal.core.repository;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IFeedProperties;

@XmlRootElement(name = "list")
@XmlType(name = "org.eclipsetrader.jessx.IdentifiersList")
public class IdentifiersList {
	  private static IdentifiersList instance;

	    @XmlElementRef
	    private List<IdentifierType> identifiers;

	    public IdentifiersList() {
	        instance = this;
	        identifiers = new ArrayList<IdentifierType>();
	    }

	    public static IdentifiersList getInstance() {
	        return instance;
	    }

	    @XmlTransient
	    public List<IdentifierType> getIdentifiers() {
	        return identifiers;
	    }

	    public void setIdentifiers(List<IdentifierType> identifiers) {
	        this.identifiers = identifiers;
	    }

	    public IdentifierType getIdentifierFor(IFeedIdentifier identifier) {
	        String symbol = identifier.getSymbol();

	        IFeedProperties properties = (IFeedProperties) identifier.getAdapter(IFeedProperties.class);
	        if (properties != null) {
	            if (properties.getProperty("org.eclipsetrader.jessx.symbol") != null) {
	                symbol = properties.getProperty("org.eclipsetrader.jessx.symbol");
	            }
	        }

	        for (IdentifierType type : identifiers) {
	            if (type.getSymbol().equals(symbol)) {
	                type.setIdentifier(identifier);
	                return type;
	            }
	        }

	        IdentifierType type = new IdentifierType(symbol);
	        type.setIdentifier(identifier);
	        identifiers.add(type);
	        return type;
	    }
}
