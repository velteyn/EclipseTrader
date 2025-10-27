package org.eclipsetrader.jessx.trobot;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.Order;
import org.eclipsetrader.jessx.business.OrderBook;
import org.eclipsetrader.jessx.business.operations.LimitOrder;
import org.eclipsetrader.jessx.net.NetworkWritable;
import org.jdom.Document;
import org.eclipsetrader.jessx.utils.Utils;

public class DiscreetIT extends Animator {
  public DiscreetIT(int name, double InactivityPercentage) {
    super(name, InactivityPercentage);
    Utils.logger.info(String.format("DiscreetIT bot %d created", name));
  }
  
  protected void MyAct() {
    reactToNews(getRobotCore().getInstitutions().keySet().iterator().next());
    Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
    while (iterInstit.hasNext()) {
      String institname = iterInstit.next();
      Institution instit = getRobotCore().getInstitution(institname);
      if ((new Date()).getTime() - ((Date)getDatesLastOrder().get(institname)).getTime() > NextWakeUp(institname) && (
        (LinkedList)getOrderBooks().get(institname)).size() > 0) {
        Utils.logger.info(String.format("DiscreetIT bot %s is acting on institution %s", getLogin(), institname));
        OrderBook ob = ((LinkedList<OrderBook>)getOrderBooks().get(institname)).getLast();
        float DifferentInstitutions = getRobotCore().getInstitutions().size();
        Utils.logger.info("(Initialisation Ordre) Nombre d'institutions diff"+ DifferentInstitutions);
        String assetname = instit.getAssetName();
        Utils.logger.info("(Initialisation Ordre) Assetname:" + assetname);
        int assets = getRobotCore().getPortfolio().getOwnings(assetname);
        Utils.logger.info("(Initialisation Ordre) Nombre d'Assets:" + assets);
        float cash = getRobotCore().getPortfolio().getCash();
        Utils.logger.info("(Initialisation Ordre) Cash:" + cash);
        int side = (int)Math.round(Math.random());
        int i = 0;
        if (ob.getAsk().size() >= 1 && ob.getBid().size() >= 1 && side == 0) {
          Utils.logger.info("Dde l'itpour le calcul de l'assetValue:");
          Iterator<String> iterAssets = getRobotCore().getInstitutions().keySet().iterator();
          int AssetsValue = 0;
          while (iterAssets.hasNext()) {
            String assetsnameAV = iterAssets.next();
            Institution institAV = getRobotCore().getInstitution(institname);
            String assetnameAV = institAV.getAssetName();
            if (((LinkedList)getOrderBooks().get(assetsnameAV)).size() > 0) {
              OrderBook obAv = ((LinkedList<OrderBook>)getOrderBooks().get(assetsnameAV)).getLast();
              Utils.logger.info("AV Assetname:" + assetsnameAV);
              Utils.logger.info("AV Quantitd'assets:" + assets);
              if (obAv.getAsk().size() >= 1 && obAv.getBid().size() >= 1) {
                int indiceAskAv = 0;
                int indiceBidAv = 0;
                int indiceTailleAv = 0;
                int totalQttySumAv = 0;
                int QttySumAv = 0;
                while (indiceAskAv < obAv.getAsk().size()) {
                  if (indiceTailleAv >= 5)
                    break; 
                  if (indiceAskAv < obAv.getAsk().size() - 2 && indiceAskAv > 0) {
                    if (((Order)obAv.getAsk().elementAt(indiceAskAv)).getOrderPrice(0) == (
                      (Order)obAv.getAsk().elementAt(indiceAskAv + 1)).getOrderPrice(0))
                      totalQttySumAv += ((Order)obAv.getAsk().elementAt(indiceAskAv)).getMaxQtty(); 
                  } else {
                    indiceTailleAv++;
                  } 
                  indiceAskAv++;
                } 
                while (indiceBidAv < obAv.getBid().size()) {
                  if (indiceTailleAv >= 5)
                    break; 
                  if (indiceBidAv < obAv.getBid().size() - 2 && indiceBidAv > 0 && (
                    (Order)obAv.getBid().elementAt(indiceBidAv)).getOrderPrice(1) == (
                    (Order)obAv.getBid().elementAt(indiceBidAv + 1)).getOrderPrice(1))
                    totalQttySumAv += ((Order)obAv.getBid().elementAt(indiceBidAv)).getMaxQtty(); 
                  indiceBidAv++;
                } 
                while (indiceAskAv < obAv.getAsk().size()) {
                  if (indiceTailleAv >= 5 || 
                    QttySumAv >= totalQttySumAv / 2)
                    break; 
                  if (indiceAskAv < obAv.getAsk().size() - 2 && indiceAskAv > 0) {
                    if (((Order)obAv.getAsk().elementAt(indiceAskAv)).getOrderPrice(0) == (
                      (Order)obAv.getAsk().elementAt(indiceAskAv + 1)).getOrderPrice(0))
                      QttySumAv += ((Order)obAv.getAsk().elementAt(indiceAskAv)).getMaxQtty(); 
                  } else {
                    indiceTailleAv++;
                  } 
                  indiceAskAv++;
                } 
                float orderPriceAV = (int)((((Order)obAv.getBid().elementAt(indiceBidAv - 1)).getOrderPrice(1) + Math.random() * ((
                  (Order)obAv.getAsk().elementAt(indiceAskAv - 1)).getOrderPrice(0) - (
                  (Order)obAv.getBid().elementAt(indiceBidAv - 1)).getOrderPrice(1))) * 100.0D) / 100.0F;
                Utils.logger.info("AV OrderPrice:" + orderPriceAV);
                AssetsValue = (int)(AssetsValue + orderPriceAV * getRobotCore().getPortfolio().getOwnings(assetnameAV));
                i++;
                Utils.logger.info("AV Valeur des Assets:" + AssetsValue);
              } 
            } 
          } 
          Utils.logger.info("Fin de l'itde calcul de l'AssetValue");
          int indiceAskOb = 0;
          int indiceBidOb = 0;
          int indiceTailleOb = 0;
          int totalQttySum = 0;
          int QttySum = 0;
          while (indiceAskOb < ob.getAsk().size()) {
            if (indiceTailleOb >= 5)
              break; 
            if (indiceAskOb < ob.getAsk().size() - 2 && indiceAskOb > 0) {
              if (((Order)ob.getAsk().elementAt(indiceAskOb)).getOrderPrice(0) == (
                (Order)ob.getAsk().elementAt(indiceAskOb + 1)).getOrderPrice(0))
                totalQttySum += ((Order)ob.getAsk().elementAt(indiceAskOb)).getMaxQtty(); 
            } else {
              indiceTailleOb++;
            } 
            indiceAskOb++;
          } 
          while (indiceBidOb < ob.getBid().size()) {
            if (indiceTailleOb >= 5)
              break; 
            if (indiceBidOb < ob.getBid().size() - 2 && indiceBidOb > 0 && (
              (Order)ob.getBid().elementAt(indiceBidOb)).getOrderPrice(1) == (
              (Order)ob.getBid().elementAt(indiceBidOb + 1)).getOrderPrice(1))
              totalQttySum += ((Order)ob.getBid().elementAt(indiceBidOb)).getMaxQtty(); 
            indiceBidOb++;
          } 
          while (indiceAskOb < ob.getAsk().size()) {
            if (indiceTailleOb >= 5 || 
              QttySum >= totalQttySum / 2)
              break; 
            if (indiceAskOb < ob.getAsk().size() - 2 && indiceAskOb > 0) {
              if (((Order)ob.getAsk().elementAt(indiceAskOb)).getOrderPrice(0) == (
                (Order)ob.getAsk().elementAt(indiceAskOb + 1)).getOrderPrice(0))
                QttySum += ((Order)ob.getAsk().elementAt(indiceAskOb)).getMaxQtty(); 
            } else {
              indiceTailleOb++;
            } 
            indiceAskOb++;
          } 
          float orderPrice = (int)(((Order)ob.getBid().elementAt(indiceBidOb - 1)).getOrderPrice(1) * 100.0F) / 100.0F;
          int QA = 0;
          Utils.logger.info("(Ordre) (QuantitNombre d'assets:" + assets);
          Utils.logger.info("(Ordre) (QuantitOrderPrice:" + orderPrice);
          Utils.logger.info("(Ordre) (QuantitAssetsValue:" + AssetsValue);
          Utils.logger.info("(Ordre) (QuantitCash:" + cash);
          Utils.logger.info("(Ordre) (QuantitNombre d'institutions:" + DifferentInstitutions);
          Utils.logger.info("(Ordre) (QuantitPourcentage : (assets*orderPrice/(AssetsValue+cash):" + (assets * orderPrice / (AssetsValue + cash)));
          Utils.logger.info("(Ordre) (QuantitPourcentage voulu : 1/(DifferentInstitutions):" + (1.0F / DifferentInstitutions));
          if (assets * orderPrice / (AssetsValue + cash) > 1.0F / (DifferentInstitutions + 1.0F) && i == DifferentInstitutions) {
            QA = (int)Math.floor((int)Math.abs(assets - (AssetsValue + cash) / orderPrice * (DifferentInstitutions + 1.0F)));
            if (QA > assets * 25 / 100)
              QA = assets * 25 / 100; 
          } else {
            QA = 0;
          } 
          Utils.logger.info("QA:" + QA);
          LimitOrder lo = new LimitOrder();
          lo.setEmitter(getLogin());
          lo.setInstitutionName(institname);
          lo.setPrice(orderPrice);
          lo.setQuantity(QA);
          lo.setSide(0);
          Utils.logger.info(String.format("DiscreetIT bot %s sending BUY order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), institname, lo.getPrice()));
          getRobotCore().send((NetworkWritable)lo);
          continue;
        } 
        if (ob.getBid().size() >= 1 && ob.getAsk().size() >= 1 && side == 1) {
          Iterator<String> iterAssets = getRobotCore().getInstitutions().keySet().iterator();
          int AssetsValue = 0;
          while (iterAssets.hasNext()) {
            String assetsnameAV = iterAssets.next();
            Institution institAV = getRobotCore().getInstitution(institname);
            String assetnameAV = institAV.getAssetName();
            if (((LinkedList)getOrderBooks().get(assetsnameAV)).size() > 0) {
              OrderBook obAv = ((LinkedList<OrderBook>)getOrderBooks().get(assetsnameAV)).getLast();
              Utils.logger.info("AV AssetName:" + assetsnameAV);
              Utils.logger.info("AV Quantitd'assets:" + assets);
              if (obAv.getBid().size() >= 1 && obAv.getAsk().size() >= 1) {
                int indiceBidAv = 0;
                int indiceAskAv = 0;
                int indiceTailleAv = 0;
                int totalQttySumAv = 0;
                int QttySumAv = 0;
                while (indiceBidAv < obAv.getBid().size()) {
                  if (indiceTailleAv >= 5)
                    break; 
                  if (indiceBidAv < obAv.getBid().size() - 2 && indiceBidAv > 0) {
                    if (((Order)obAv.getBid().elementAt(indiceBidAv)).getOrderPrice(1) == (
                      (Order)obAv.getBid().elementAt(indiceBidAv + 1)).getOrderPrice(1))
                      totalQttySumAv += ((Order)obAv.getBid().elementAt(indiceBidAv)).getMaxQtty(); 
                  } else {
                    indiceTailleAv++;
                  } 
                  indiceBidAv++;
                } 
                while (indiceAskAv < obAv.getAsk().size()) {
                  if (indiceTailleAv >= 5)
                    break; 
                  if (indiceAskAv < obAv.getAsk().size() - 2 && indiceAskAv > 0 && (
                    (Order)obAv.getAsk().elementAt(indiceAskAv)).getOrderPrice(0) == (
                    (Order)obAv.getAsk().elementAt(indiceAskAv + 1)).getOrderPrice(0))
                    totalQttySumAv += ((Order)obAv.getAsk().elementAt(indiceAskAv)).getMaxQtty(); 
                  indiceAskAv++;
                } 
                while (indiceBidAv < obAv.getBid().size()) {
                  if (indiceTailleAv >= 5 || 
                    QttySumAv >= totalQttySumAv / 2)
                    break; 
                  if (indiceBidAv < obAv.getBid().size() - 2 && indiceBidAv > 0) {
                    if (((Order)obAv.getBid().elementAt(indiceBidAv)).getOrderPrice(1) == (
                      (Order)obAv.getBid().elementAt(indiceBidAv + 1)).getOrderPrice(1))
                      QttySumAv += ((Order)obAv.getAsk().elementAt(indiceBidAv)).getMaxQtty(); 
                  } else {
                    indiceTailleAv++;
                  } 
                  indiceBidAv++;
                } 
                float orderPriceAV = (int)((((Order)obAv.getBid().elementAt(indiceBidAv - 1)).getOrderPrice(1) + Math.random() * ((
                  (Order)obAv.getAsk().elementAt(indiceAskAv - 1)).getOrderPrice(0) - (
                  (Order)obAv.getBid().elementAt(indiceBidAv - 1)).getOrderPrice(1))) * 100.0D) / 100.0F;
                Utils.logger.info("OrderPrice:" + orderPriceAV);
                AssetsValue = (int)(AssetsValue + orderPriceAV * getRobotCore().getPortfolio().getOwnings(assetnameAV));
                i++;
                Utils.logger.info("Valeur des Assets:" + AssetsValue);
              } 
            } 
          } 
          int indiceBidOb = 0;
          int indiceAskOb = 0;
          int indiceTailleOb = 0;
          int totalQttySum = 0;
          int QttySum = 0;
          while (indiceBidOb < ob.getBid().size()) {
            if (indiceTailleOb >= 5)
              break; 
            if (indiceBidOb < ob.getBid().size() - 2 && indiceBidOb > 0) {
              if (((Order)ob.getBid().elementAt(indiceBidOb)).getOrderPrice(1) == (
                (Order)ob.getBid().elementAt(indiceBidOb + 1)).getOrderPrice(1))
                totalQttySum += ((Order)ob.getBid().elementAt(indiceBidOb)).getMaxQtty(); 
            } else {
              indiceTailleOb++;
            } 
            indiceBidOb++;
          } 
          while (indiceAskOb < ob.getAsk().size()) {
            if (indiceTailleOb >= 5)
              break; 
            if (indiceAskOb < ob.getAsk().size() - 2 && indiceAskOb > 0 && (
              (Order)ob.getAsk().elementAt(indiceAskOb)).getOrderPrice(0) == (
              (Order)ob.getAsk().elementAt(indiceAskOb + 1)).getOrderPrice(0))
              totalQttySum += ((Order)ob.getAsk().elementAt(indiceAskOb)).getMaxQtty(); 
            indiceAskOb++;
          } 
          while (indiceBidOb < ob.getBid().size()) {
            if (indiceTailleOb >= 5 || 
              QttySum >= totalQttySum / 2)
              break; 
            if (indiceBidOb < ob.getBid().size() - 2 && indiceBidOb > 0) {
              if (((Order)ob.getBid().elementAt(indiceBidOb)).getOrderPrice(1) == (
                (Order)ob.getBid().elementAt(indiceBidOb + 1)).getOrderPrice(1))
                QttySum += ((Order)ob.getAsk().elementAt(indiceBidOb)).getMaxQtty(); 
            } else {
              indiceTailleOb++;
            } 
            indiceBidOb++;
          } 
          float orderPrice = (int)((Order)ob.getAsk().elementAt(indiceAskOb - 1)).getOrderPrice(0) * 100.0F / 100.0F;
          int QB = 0;
          Utils.logger.info("Nombre d'Assets:" + assets);
          Utils.logger.info("OrderPrice:" + orderPrice);
          Utils.logger.info("AssetsValue:" + AssetsValue);
          Utils.logger.info("Cash:" + cash);
          Utils.logger.info("n:" + DifferentInstitutions);
          Utils.logger.info("(Ordre) (QuantitPourcentage : (assets*orderPrice/(AssetsValue+cash):" + (assets * orderPrice / (AssetsValue + cash)));
          Utils.logger.info("(Ordre) (QuantitPourcentage voulu : 1/(DifferentInstitutions):" + (1.0F / DifferentInstitutions));
          if (assets == 0) {
            QB = (int)Math.floor((int)Math.abs(cash / orderPrice * (DifferentInstitutions + 1.0F)));
          } else if (assets * orderPrice / (AssetsValue + cash) < 1.0F / (DifferentInstitutions + 1.0F) && i == DifferentInstitutions) {
            QB = (int)Math.floor(((int)Math.abs((AssetsValue + cash) / orderPrice * (DifferentInstitutions + 1.0F)) - assets));
            if (QB > assets * 25 / 100)
              QB = assets * 25 / 100; 
          } else {
            QB = 0;
          } 
          Utils.logger.info("QB:" + QB);
          LimitOrder lo = new LimitOrder();
          lo.setEmitter(getLogin());
          lo.setInstitutionName(institname);
          lo.setPrice(orderPrice);
          lo.setQuantity(QB);
          lo.setSide(1);
          Utils.logger.info(String.format("DiscreetIT bot %s sending SELL order for %d shares of %s at %.2f", getLogin(), lo.getQuantity(), institname, lo.getPrice()));
          getRobotCore().send((NetworkWritable)lo);
        } 
      } 
    } 
  }
  
  public void objectReceived(Document xmlObject) {
    super.objectReceived(xmlObject);
  }
  
  protected String chooseName(int i) {
    return "NonDetectableIT" + (i + 1);
  }
}
