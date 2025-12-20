/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marco Maccaferri - initial API and implementation
 * Edoardo BAROLO    - virtual investor
 *
 */

package org.eclipsetrader.jessx.internal.news;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.jessx.business.BusinessCore;
import org.eclipsetrader.jessx.business.NewsItem;
import org.eclipsetrader.jessx.business.Scenario;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.internal.repository.HeadLine;

import org.eclipsetrader.jessx.business.NewsListener;
import org.eclipsetrader.jessx.internal.JessxActivator;
import org.eclipsetrader.news.core.INewsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NewsProvider implements INewsProvider, NewsListener {

    private boolean isRunning = false;
    private List<IHeadLine> headlines = new ArrayList<IHeadLine>();

    public NewsProvider() {
        Scenario scenario = BusinessCore.getScenario();
        if (scenario != null) {
            scenario.addNewsListener(this);
        }
        refresh();
    }

    @Override
    public String getId() {
        return "jessx.news.provider";
    }

    @Override
    public String getName() {
        return "JessX News Provider";
    }

    @Override
    public IHeadLine[] getHeadLines() {
        return headlines.toArray(new IHeadLine[headlines.size()]);
    }

    @Override
    public void start() {
        isRunning = true;
        System.out.println("JessX News Provider started.");
    }

    @Override
    public void stop() {
        isRunning = false;
        System.out.println("JessX News Provider stopped.");
    }

    @Override
    public void refresh() {
        headlines.clear();

        Scenario scenario = BusinessCore.getScenario();
        if (scenario != null) {
            List<NewsItem> newsItems = scenario.getNewsItems();
            for (NewsItem item : newsItems) {
                ISecurity[] securities = new ISecurity[1];
                securities[0] = new Security(item.getAsset(), null);
                headlines.add(new HeadLine(new Date(), "JessX", item.getText(), securities, ""));
            }
        }
    }

    @Override
    public void newsLoaded(List<NewsItem> newsItems) {
        refresh();
        
        BundleContext context = JessxActivator.getDefault().getBundle().getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(INewsService.class.getName());
        if (serviceReference != null) {
            INewsService service = (INewsService) context.getService(serviceReference);
            if (service != null) {
                service.addHeadLines(getHeadLines());
            }
            context.ungetService(serviceReference);
        }
    }
}
