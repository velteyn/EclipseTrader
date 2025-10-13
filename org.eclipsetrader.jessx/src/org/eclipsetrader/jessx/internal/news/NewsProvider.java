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

import org.eclipsetrader.core.feed.FeedIdentifier;
import org.eclipsetrader.core.feed.FeedProperties;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.instruments.Security;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.internal.repository.HeadLine;

public class NewsProvider implements INewsProvider {

    private boolean isRunning = false;
    private List<IHeadLine> headlines = new ArrayList<>();

    public NewsProvider() {
        // Inizializza alcuni dati mock all'avvio
        refresh();
    }

    @Override
    public String getId() {
        return "jessx.mock.news.provider";
    }

    @Override
    public String getName() {
        return "JessX Mock News Provider";
    }

    @Override
    public IHeadLine[] getHeadLines() {
        return headlines.toArray(new IHeadLine[headlines.size()]);
    }

    @Override
    public void start() {
        isRunning = true;
        System.out.println("JessX Mock News Provider started.");
        // Qui potresti simulare l'avvio di un feed di notizie
    }

    @Override
    public void stop() {
        isRunning = false;
        System.out.println("JessX Mock News Provider stopped.");
        // Qui potresti simulare l'arresto di un feed di notizie
    }

    @Override
    public void refresh() {
        if (isRunning) {
            System.out.println("Refreshing JessX Mock News Provider headlines.");
        } else {
            System.out.println("JessX Mock News Provider refreshing headlines (not running).");
        }
        // Simula il recupero di nuove notizie usando la classe HeadLine e Security
        headlines.clear();

        // Crea alcune istanze di MockFeedProperties
        FeedProperties properties1 = new FeedProperties();
        properties1.setProperty("exchange", "NASDAQ");
        FeedProperties properties2 = new FeedProperties();
        properties2.setProperty("exchange", "NASDAQ");
        FeedProperties properties3 = new FeedProperties();
        properties3.setProperty("exchange", "NYSE");

        // Crea alcune istanze di IFeedIdentifier
        IFeedIdentifier identifier1 = new FeedIdentifier("AAPL", properties1);
        IFeedIdentifier identifier2 = new FeedIdentifier("GOOGL", properties2);
        IFeedIdentifier identifier3 = new FeedIdentifier("MSFT", properties3);

        // Crea alcune istanze di ISecurity
        ISecurity security1 = new Security("Apple Inc.", identifier1);
        ISecurity security2 = new Security("Alphabet Inc.", identifier2);
        ISecurity security3 = new Security("Microsoft Corp.", identifier3);

        headlines.add(new HeadLine(new Date(), "Reuters", "Aggiornamenti sul Mercato Azionario: Indici in Rialzo", new ISecurity[]{security1, security2}, "http://www.example.com/news/1"));
        headlines.add(new HeadLine(new Date(System.currentTimeMillis() - 3600000), "Bloomberg", "Analisi Tecnica: Livelli Chiave da Monitorare", new ISecurity[]{security3}, "http://www.example.com/news/2"));
        headlines.add(new HeadLine(new Date(System.currentTimeMillis() - 7200000), "ANSA", "Ultime Notizie Economiche dalla Zona Euro", null, "http://www.example.com/news/3"));
        headlines.add(new HeadLine(new Date(System.currentTimeMillis() - 10800000), "Il Sole 24 Ore", "Previsioni degli Analisti per il Prossimo Trimestre", new ISecurity[]{security1, security3}, "http://www.example.com/news/4"));
        headlines.add(new HeadLine(new Date(System.currentTimeMillis() - 14400000), "Financial Times", "Impatto del Nuovo Regolamento sul Settore Bancario", new ISecurity[]{security2}, "http://www.example.com/news/5"));
        // Aggiungi altri titoli mock a piacere
    }
}
