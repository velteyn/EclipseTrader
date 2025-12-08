
package org.eclipsetrader.ui.internal.charts.views;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.repositories.IPropertyConstants;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.ui.charts.ChartObjectFocusEvent;
import org.eclipsetrader.ui.charts.DataBounds;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectVisitor;
import org.eclipsetrader.ui.charts.IGraphics;

public class TradeObject implements IChartObject {

    private List<IStore> trades;

    public TradeObject(List<IStore> trades) {
        this.trades = trades;
    }

    @Override
    public void paint(IGraphics graphics) {
        if (trades == null) {
            System.out.println("TradeObject: trades list is null");
            return;
        }
        System.out.println("TradeObject: Painting " + trades.size() + " trades");

        for (IStore trade : trades) {
            Object dateObj = trade.fetchProperties(null).getProperty(IPropertyConstants.PURCHASE_DATE);
            Object priceObj = trade.fetchProperties(null).getProperty(IPropertyConstants.PURCHASE_PRICE);
            
            if (dateObj == null || priceObj == null) {
                System.out.println("TradeObject: Missing properties for trade " + trade);
                continue;
            }

            long time = ((Date) dateObj).getTime();
            double price = (Double) priceObj;

            int x = graphics.mapToHorizontalAxis(time);
            int y = graphics.mapToVerticalAxis(price);
            
            System.out.println("TradeObject: Drawing trade at time=" + time + ", price=" + price + " -> x=" + x + ", y=" + y);

            graphics.setForegroundColor(new RGB(255, 0, 0));
            graphics.drawArc(x - 2, y - 2, 4, 4, 0, 360);
        }
    }

    public void dispose() {
    }

    @Override
    public IDataSeries getDataSeries() {
        return null;
    }

    @Override
    public void setDataBounds(DataBounds bounds) {
    }

    @Override
    public void paintScale(org.eclipsetrader.ui.charts.Graphics graphics) {
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public String getToolTip(int x, int y) {
        return null;
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return false;
    }

    @Override
    public void handleFocusGained(ChartObjectFocusEvent event) {
    }

    @Override
    public void handleFocusLost(ChartObjectFocusEvent event) {
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void accept(IChartObjectVisitor visitor) {
    }
}
