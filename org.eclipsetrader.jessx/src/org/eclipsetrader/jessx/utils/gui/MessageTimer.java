// 
//This program is free software; GNU license ; USE AT YOUR RISK , WITHOUT ANY WARRANTY
// 

package org.eclipsetrader.jessx.utils.gui;

  
import java.util.Vector;

import org.eclipsetrader.jessx.utils.Utils;
import org.eclipsetrader.jessx.business.BusinessCore;
import org.eclipsetrader.jessx.business.InformationItem;
import org.eclipsetrader.jessx.net.Information;
import org.eclipsetrader.jessx.server.net.NetworkCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageTimer extends Thread {
    private List<InformationItem> listInformation;
    private List<InformationItem> listInformationSorted;

    public MessageTimer(final List<InformationItem> informations) {
        this.listInformation = new ArrayList<>(informations);
        this.checkInformationsToSend(this.listInformation);
        this.listInformationSorted = this.sort();
        Utils.logger.info("MessageTimer created...");
    }

    public List<InformationItem> sort() {
        List<InformationItem> listSorted = new ArrayList<>(this.listInformation);
        Collections.sort(listSorted, new Comparator<InformationItem>() {
            @Override
            public int compare(InformationItem o1, InformationItem o2) {
                int period1 = Integer.parseInt(o1.getPeriod());
                int period2 = Integer.parseInt(o2.getPeriod());
                if (period1 != period2) {
                    return Integer.compare(period1, period2);
                }
                int time1 = Integer.parseInt(o1.getTime());
                int time2 = Integer.parseInt(o2.getTime());
                return Integer.compare(time1, time2);
            }
        });
        return listSorted;
    }

    public void checkInformationsToSend(final List<InformationItem> information) {
        final int periodCount = BusinessCore.getGeneralParameters().getPeriodCount();
        final int periodDuration = BusinessCore.getGeneralParameters().getPeriodDuration();
        information.removeIf(item -> Integer.parseInt(item.getTime()) >= periodDuration || Integer.parseInt(item.getPeriod()) > periodCount);
    }

    @Override
    public void run() {
        final int size = this.listInformationSorted.size();
        if (size != 0) {
            int i = 0;
            try {
                this.listInformationSorted.add(this.listInformationSorted.get(size - 1));
                while (NetworkCore.getExperimentManager().getExperimentState() != 0 && i < size) {
                    while (NetworkCore.getExperimentManager().getExperimentState() == 2 && i < size) {
                        final long timeTemp = 1000 * Integer.parseInt(this.listInformationSorted.get(i).getTime()) - NetworkCore.getExperimentManager().getTimeInPeriod();
                        if (timeTemp > 0L) {
                            Thread.sleep(timeTemp);
                        }
                        if (NetworkCore.getExperimentManager().getPeriodNum() + 1 == Integer.parseInt(this.listInformationSorted.get(i).getPeriod())) {
                            do {
                                if (this.listInformationSorted.get(i).getCategory().equals("All players")) {
                                    NetworkCore.sendToAllPlayers(new Information(this.listInformationSorted.get(i).getContent()));
                                } else {
                                    NetworkCore.sendToPlayerCategory(new Information(this.listInformationSorted.get(i).getContent()), this.listInformationSorted.get(i).getCategory());
                                }
                            } while (++i < size && Integer.parseInt(this.listInformationSorted.get(i - 1).getTime()) == Integer.parseInt(this.listInformationSorted.get(i).getTime()) && NetworkCore.getExperimentManager().getPeriodNum() + 1 == Integer.parseInt(this.listInformationSorted.get(i).getPeriod()));
                        } else {
                            while (i < size && NetworkCore.getExperimentManager().getPeriodNum() + 1 > Integer.parseInt(this.listInformationSorted.get(i).getPeriod())) {
                                ++i;
                                Utils.logger.warn("A message has not be sent");
                            }
                            final long time = NetworkCore.getExperimentManager().getTimeRemainingInPeriod();
                            if (time <= 0L) {
                                continue;
                            }
                            Thread.sleep(time);
                        }
                    }
                    while (NetworkCore.getExperimentManager().getExperimentState() == 1 && i < size) {
                        Thread.sleep(300L);
                        final long n = Math.abs(1000 * Integer.parseInt(this.listInformationSorted.get(i).getTime()));
                    }
                }
            } catch (InterruptedException ex1) {
                Utils.logger.warn("MessageTimer sleep interrupted. " + ex1.toString());
            }
        }
    }
}
