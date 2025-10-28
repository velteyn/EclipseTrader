
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
            return;
        }

        for (IStore trade : trades) {
            long time = ((Date) trade.fetchProperties(null).getProperty(IPropertyConstants.PURCHASE_DATE)).getTime();
            double price = (Double) trade.fetchProperties(null).getProperty(IPropertyConstants.PURCHASE_PRICE);

            int x = graphics.mapToHorizontalAxis(time);
            int y = graphics.mapToVerticalAxis(price);

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
