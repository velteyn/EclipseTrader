
package org.eclipsetrader.ui.internal.charts.views;

import java.util.List;

import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;

public class TradeFactory implements IChartObjectFactory {

    private List<IStore> trades;
    private IChartParameters parameters;

    public TradeFactory() {
    }

    public void setTrades(List<IStore> trades) {
        this.trades = trades;
    }

    @Override
    public IChartObject createObject(IDataSeries source) {
        if (trades == null) {
            return null;
        }
        return new TradeObject(trades);
    }

    @Override
    public String getId() {
        return "org.eclipsetrader.ui.charts.trades";
    }

    @Override
    public String getName() {
        return "Trades";
    }

    @Override
    public IChartParameters getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(IChartParameters parameters) {
        this.parameters = parameters;
    }
}
