package org.eclipsetrader.jessx.trobot;

import java.util.Date;
import java.util.Iterator;

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
    reactToNews(getRobotCore().getInstitutions().keySet().iterator().next());
    Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
    while (iterInstit.hasNext()) {
      String instit = iterInstit.next();
      if ((new Date()).getTime() - ((Date)getDatesLastOrder().get(instit)).getTime() > NextWakeUp(instit)) {
        Utils.logger.info(String.format("NotDiscreet bot %s is acting on institution %s", getLogin(), instit));
        int quantity = 1 + (int)(Math.random() * 49.0D);
        int side = (int)Math.round(Math.random());
        float price = ((int)(this.lowLimit + (this.highLimit - this.lowLimit) * Math.random()) * 100 / 100);
        LimitOrder lo = new LimitOrder();
        lo.setEmitter(getLogin());
        lo.setInstitutionName(instit);
        lo.setPrice(price);
        lo.setQuantity(quantity);
        lo.setSide(side);
        if (side == 0) {
            Utils.logger.info(String.format("NotDiscreet bot %s sending BUY order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), instit, lo.getPrice()));
        } else {
            Utils.logger.info(String.format("NotDiscreet bot %s sending SELL order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), instit, lo.getPrice()));
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
