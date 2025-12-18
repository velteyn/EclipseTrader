package org.eclipsetrader.jessx.trobot;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.OrderBook;
import org.eclipsetrader.jessx.business.Order;
import org.jdom.Document;
import org.eclipsetrader.jessx.business.operations.LimitOrder;
import org.eclipsetrader.jessx.net.NetworkWritable;
import org.eclipsetrader.jessx.utils.Utils;

public class NotDiscreet extends Animator {
  private int lowLimit;
  
  private int highLimit;
  
  public NotDiscreet(int i, double InactivityPercentage, int lowLimit, int highLimit) {
    super(i, InactivityPercentage);
    this.lowLimit = lowLimit;
    this.highLimit = highLimit;
    Utils.logger.info(String.format("NotDiscreet bot %d created with price range [%d, %d]", i, lowLimit, highLimit));
  }
  
  protected void MyAct() {
    for (Object institution : getRobotCore().getInstitutions().keySet()) {
        reactToNews((String)institution);
    }
    Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
    while (iterInstit.hasNext()) {
      String instit = iterInstit.next();
      if ((new Date()).getTime() - ((Date)getDatesLastOrder().get(instit)).getTime() > NextWakeUp(instit)) {
        Institution institution = getRobotCore().getInstitution(instit);
        String symbol = institution.getAssetName();
        Utils.logger.info(String.format("NotDiscreet bot %s is acting on institution %s (%s)", getLogin(), instit, symbol));
        int quantity = 1 + (int)(Math.random() * 49.0D);
        int side = (int)Math.round(Math.random());
        
        float price = 0;
        boolean priceSet = false;
        
        LinkedList<OrderBook> books = getOrderBooks().get(instit);
        if (books != null && !books.isEmpty()) {
            OrderBook ob = books.getLast();
            if (side == 0) { // BUY
                 if (ob.getBid().size() > 0) {
                     float bestBid = ((Order)ob.getBid().elementAt(0)).getOrderPrice(1);
                     price = bestBid * (1.0f + (float)((Math.random() - 0.5) * 0.1));
                     priceSet = true;
                 } else if (ob.getAsk().size() > 0) {
                     float bestAsk = ((Order)ob.getAsk().elementAt(0)).getOrderPrice(0);
                     price = bestAsk * (1.0f - (float)(Math.random() * 0.1));
                     priceSet = true;
                 }
            } else { // SELL
                 if (ob.getAsk().size() > 0) {
                     float bestAsk = ((Order)ob.getAsk().elementAt(0)).getOrderPrice(0);
                     price = bestAsk * (1.0f + (float)((Math.random() - 0.5) * 0.1));
                     priceSet = true;
                 } else if (ob.getBid().size() > 0) {
                     float bestBid = ((Order)ob.getBid().elementAt(0)).getOrderPrice(1);
                     price = bestBid * (1.0f + (float)(Math.random() * 0.1));
                     priceSet = true;
                 }
            }
        }
        
        if (!priceSet) {
            price = ((int)(this.lowLimit + (this.highLimit - this.lowLimit) * Math.random()) * 100 / 100);
        } else {
             price = Math.max(1, Math.round(price * 100) / 100.0f);
        }

        LimitOrder lo = new LimitOrder();
        lo.setEmitter(getLogin());
        lo.setInstitutionName(instit);
        lo.setPrice(price);
        lo.setQuantity(quantity);
        lo.setSide(side);
        if (side == 0) {
            Utils.logger.info(String.format("NotDiscreet bot %s sending BUY order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), symbol, lo.getPrice()));
        } else {
            Utils.logger.info(String.format("NotDiscreet bot %s sending SELL order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), symbol, lo.getPrice()));
        }
        getRobotCore().send((NetworkWritable)lo);
      } 
    } 
  }
  
  public void objectReceived(Document xmlObject) {
    super.objectReceived(xmlObject);
  }
  
  protected String chooseName(int i) {
    return "PotentiallyDetectableZIT" + (i + 1);
  }
}
