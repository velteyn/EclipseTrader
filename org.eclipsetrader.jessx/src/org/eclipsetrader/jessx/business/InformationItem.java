package org.eclipsetrader.jessx.business;

public class InformationItem {

    private String content;
    private String category;
    private String period;
    private String time;

    public InformationItem(String content, String category, String period, String time) {
        this.content = content;
        this.category = category;
        this.period = period;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public String getPeriod() {
        return period;
    }

    public String getTime() {
        return time;
    }
}
