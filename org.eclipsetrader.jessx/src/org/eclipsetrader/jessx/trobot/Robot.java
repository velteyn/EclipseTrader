package org.eclipsetrader.jessx.trobot;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;

import org.eclipsetrader.jessx.business.Deal;
import org.eclipsetrader.jessx.business.Institution;
import org.eclipsetrader.jessx.business.Operator;
import org.eclipsetrader.jessx.business.OrderBook;
import org.eclipsetrader.jessx.business.Portfolio;
import org.eclipsetrader.jessx.client.ClientCore;
import org.eclipsetrader.jessx.client.event.ExperimentDeveloppmentListener;
import org.eclipsetrader.jessx.client.event.NetworkListener;
import org.eclipsetrader.jessx.net.DividendInfo;
import org.eclipsetrader.jessx.business.NewsItem;
import org.eclipsetrader.jessx.net.ExpUpdate;
import org.eclipsetrader.jessx.net.NetworkWritable;
import org.eclipsetrader.jessx.net.OperatorPlayed;
import org.jdom.Document;
import org.jdom.Element;

public abstract class Robot extends Thread implements ExperimentDeveloppmentListener, NetworkListener {
  private String login;
  
  private RobotCore robotCore;
  
  private LinkedList<NewsItem> news;

  private LinkedList<Deal> deals;
  
  private LinkedList<DividendInfo> dividendInfos;
  
  private HashMap<String, LinkedList<OrderBook>> orderBooks;
  
  private HashMap<String, Timer> mySchedulers;
  
  private boolean ordersAllowed;
  
  private boolean hasToRun = true;
  
  private HashMap<String, Date> datesLastOrder;
  
  public Robot(int name) {
    this.deals = new LinkedList<Deal>();
    this.news = new LinkedList<NewsItem>();
    this.dividendInfos = new LinkedList<DividendInfo>();
    this.orderBooks = new HashMap<String, LinkedList<OrderBook>>();
    this.datesLastOrder = new HashMap<String, Date>();
    this.mySchedulers = new HashMap<String, Timer>();
    this.robotCore = new RobotCore(this);
    this.robotCore.getExperimentManager().addListener(this);
    setLogin(chooseName(name));
    this.ordersAllowed = false;
    try {
      this.robotCore.connecToServer("127.0.0.1", getLogin(), "");
    } catch (IOException e) {
      e.printStackTrace();
    } 
    this.robotCore.addNetworkListener(this, "ExperimentUpdate");
    this.robotCore.addNetworkListener(this, "Initialisation");
    this.robotCore.addNetworkListener(this, "Deal");
    this.robotCore.addNetworkListener(this, "Institution");
    this.robotCore.addNetworkListener(this, "DividendInfo");
    this.robotCore.addNetworkListener(this, "Portfolio");
    this.robotCore.addNetworkListener(this, "OperatorPlayed");
    this.robotCore.addNetworkListener(this, "OrderBook");
    this.robotCore.addNetworkListener(this, "News");
  }
  
  public String getLogin() {
    return this.login;
  }
  
  public abstract long NextWakeUp(String paramString);
  
  public abstract long BasicNextWakeUp();
  
  public RobotCore getRobotCore() {
    return this.robotCore;
  }
  
  public final void Act() {
    if (this.ordersAllowed)
      MyAct(); 
    if (this.hasToRun && this.ordersAllowed) {
      Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
      while (iterInstit.hasNext()) {
        String instit = iterInstit.next();
        long Delay = BasicNextWakeUp();
        Task task = new Task(this, Delay);
        ((Timer)this.mySchedulers.get(instit)).schedule(task, Delay);
      } 
    } 
  }
  
  public void objectReceived(Document xmlDoc) {
    if (xmlDoc.getRootElement().getName().equals("ExperimentUpdate")) {
      ExpUpdate update = new ExpUpdate(0, "", 0);
      if (update.initFromNetworkInput(xmlDoc.getRootElement()) && 
        update.getUpdateType() == 7)
        this.robotCore.send((NetworkWritable)new ExpUpdate(7, "", -1)); 
    } else if (xmlDoc.getRootElement().getName().equals("Initialisation")) {
      this.robotCore.initializeForNewExperiment();
      initializeForNewExperiment();
    } else if (xmlDoc.getRootElement().getName().equals("Deal")) {
      Deal deal = new Deal("", 0.0F, 0, 0L, "", "", 0.0F, "", "");
      if (deal.initFromNetworkInput(xmlDoc.getRootElement()) && 
        ClientCore.getInstitution(deal.getDealInstitution()) != null)
        this.deals.add(deal); 
    } else if (xmlDoc.getRootElement().getName().equals("Institution")) {
      Institution instit = Institution.loadInstitutionFromXml(xmlDoc.getRootElement());
      this.robotCore.addInstitution(instit);
      this.orderBooks.put(instit.getName(), new LinkedList<OrderBook>());
      this.datesLastOrder.put(instit.getName(), new Date());
      Timer temp = new Timer(instit.getName());
      this.mySchedulers.put(instit.getName(), temp);
    } else if (xmlDoc.getRootElement().getName().equals("DividendInfo")) {
      DividendInfo divInfo = new DividendInfo();
      divInfo.initFromNetworkInput(xmlDoc.getRootElement());
      this.dividendInfos.add(divInfo);
    } else if (xmlDoc.getRootElement().getName().equals("Portfolio")) {
        if (this.robotCore.getPortfolio() == null) {
            this.robotCore.setPortfolio(new Portfolio());
        }
      this.robotCore.getPortfolio().initFromNetworkInput(xmlDoc.getRootElement());
    } else if (xmlDoc.getRootElement().getName().equals("OperatorPlayed")) {
      OperatorPlayed opPlayed = new OperatorPlayed("");
      if (opPlayed.initFromNetworkInput(xmlDoc.getRootElement())) {
        Institution inst = this.robotCore.getInstitution(opPlayed.getInstitutionName());
        if (inst != null) {
          Operator op = inst.getOperator(opPlayed.getOperatorName());
          if (op != null)
            this.robotCore.addOperatorPlayed(op); 
        } 
      } 
    } else if (xmlDoc.getRootElement().getName().equals("OrderBook")) {
        OrderBook ob = new OrderBook();
        ob.initFromNetworkInput(xmlDoc.getRootElement());
        this.datesLastOrder.put(ob.getInstitution(), new Date());

        LinkedList<OrderBook> list = this.orderBooks.get(ob.getInstitution());
        if (list == null) {
            list = new LinkedList<OrderBook>();
            this.orderBooks.put(ob.getInstitution(), list);
        }
        list.add(ob);
    } else if (xmlDoc.getRootElement().getName().equals("News")) {
        Iterator<Element> newsIter = xmlDoc.getRootElement().getChildren("Item").iterator();
        while (newsIter.hasNext()) {
            Element newsElem = newsIter.next();
            String priority = newsElem.getAttributeValue("priority");
            String asset = newsElem.getAttributeValue("asset");
            String text = newsElem.getText();
            news.add(new NewsItem(priority, asset, text));
        }
    }
  }
  
  public void initializeForNewExperiment() {}
  
  public void experimentBegins() {}
  
  public void experimentFinished() {
    Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
    while (iterInstit.hasNext()) {
      String instit = iterInstit.next();
      ((Timer)this.mySchedulers.get(instit)).cancel();
    } 
  }
  
  public void periodBegins() {
    this.ordersAllowed = true;
    if (this.hasToRun) {
      Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
      while (iterInstit.hasNext()) {
        String instit = iterInstit.next();
        long Delay = BasicNextWakeUp();
        Task task = new Task(this, Delay);
        ((Timer)this.mySchedulers.get(instit)).schedule(task, Delay);
      } 
    } 
  }
  
  public void periodFinished() {
    this.ordersAllowed = false;
    Iterator<String> iterInstit = getRobotCore().getInstitutions().keySet().iterator();
    while (iterInstit.hasNext()) {
      String instit = iterInstit.next();
      ((Timer)this.mySchedulers.get(instit)).cancel();
      this.mySchedulers.put(instit, new Timer());
    } 
  }
  
  public void run() {
    while (this.hasToRun) {
      try {
        sleep(500L);
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  protected abstract void MyAct();

  protected void reactToNews(String institution) {
      LinkedList<NewsItem> news = getNews();
      for (NewsItem item : news) {
          if (item.getAsset().equals(institution)) {
                if (item.getPriority().equals("HIGH")) {
                    if (item.getText().contains("rise")) {
                        buy(institution, 100);
                    } else if (item.getText().contains("fall")) {
                        sell(institution, 100);
                    }
                } else if (item.getPriority().equals("MEDIUM")) {
                    if (item.getText().contains("rise")) {
                        buy(institution, 50);
                    } else if (item.getText().contains("fall")) {
                        sell(institution, 50);
                    }
                }
          }
      }
  }
  
  protected abstract String chooseName(int paramInt);
  
  protected void buy(String institution, int quantity) {
      LimitOrder lo = new LimitOrder();
      lo.setEmitter(getLogin());
      lo.setInstitutionName(institution);
      lo.setPrice(100);
      lo.setQuantity(quantity);
      lo.setSide(0);
      getRobotCore().send((NetworkWritable)lo);
  }

  protected void sell(String institution, int quantity) {
      LimitOrder lo = new LimitOrder();
      lo.setEmitter(getLogin());
      lo.setInstitutionName(institution);
      lo.setPrice(100);
      lo.setQuantity(quantity);
      lo.setSide(1);
      getRobotCore().send((NetworkWritable)lo);
  }

  public AbstractList<Deal> getDeals() {
    return this.deals;
  }
  
  public void setLogin(String login) {
    this.login = login;
  }
  
  public HashMap<String, Timer> getMySchedulers() {
    return this.mySchedulers;
  }
  
  public boolean isHasToRun() {
    return this.hasToRun;
  }
  
  public void setHasToRun(boolean hasToRun) {
    this.hasToRun = hasToRun;
  }
  
  public HashMap<String, Date> getDatesLastOrder() {
    return this.datesLastOrder;
  }
  
  public LinkedList<DividendInfo> getDividendInfos() {
    return this.dividendInfos;
  }
  
  public HashMap<String, LinkedList<OrderBook>> getOrderBooks() {
    return this.orderBooks;
  }
  
  public boolean isOrdersAllowed() {
    return this.ordersAllowed;
  }

  public LinkedList<NewsItem> getNews() {
    return this.news;
  }
}
