package org.eclipsetrader.jessx.business;

import java.util.List;

public interface NewsListener {
    public void newsLoaded(List<NewsItem> newsItems);
}
