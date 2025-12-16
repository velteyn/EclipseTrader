package org.eclipsetrader.jessx.internal.core;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;

import org.eclipsetrader.jessx.business.NewsItem;
import org.eclipsetrader.jessx.trobot.Discreet;
import org.eclipsetrader.jessx.trobot.Robot;
import org.junit.jupiter.api.Test;

public class RobotNewsReactionTest {

    @Test
    public void testReactToNews() {
        // Create spy robots with different personas
        Robot poorBot = spy(new Discreet(1, 10.0, "Poor"));
        doNothing().when(poorBot).buy("AAT", 10);
        doNothing().when(poorBot).sell("AAT", 10);

        Robot investmentGroupBot = spy(new Discreet(2, 10.0, "Investment Group"));
        doNothing().when(investmentGroupBot).buy("AAT", 100);
        doNothing().when(investmentGroupBot).sell("AAT", 100);

        Robot bigInstitutionBot = spy(new Discreet(3, 10.0, "Big Institution"));
        doNothing().when(bigInstitutionBot).buy("AAT", 1000);
        doNothing().when(bigInstitutionBot).sell("AAT", 1000);

        // Create mock news items with different sentiments
        NewsItem goodNews = new NewsItem("HIGH", "AAT", "AAT dividend outlook improves", "good");
        NewsItem badNews = new NewsItem("LOW", "AAT", "AAT faces minor regulatory delay", "bad");
        NewsItem neutralNews = new NewsItem("MEDIUM", "AAT", "AAT posts steady quarterly results", "neutral");

        // Add news to the bots' news lists
        poorBot.getNews().add(goodNews);
        poorBot.getNews().add(badNews);
        poorBot.getNews().add(neutralNews);

        investmentGroupBot.getNews().add(goodNews);
        investmentGroupBot.getNews().add(badNews);
        investmentGroupBot.getNews().add(neutralNews);

        bigInstitutionBot.getNews().add(goodNews);
        bigInstitutionBot.getNews().add(badNews);
        bigInstitutionBot.getNews().add(neutralNews);

        // Call the reactToNews method
        poorBot.reactToNews("AAT");
        investmentGroupBot.reactToNews("AAT");
        bigInstitutionBot.reactToNews("AAT");

        // Verify that the buy and sell methods are called with the correct quantities
        verify(poorBot, times(1)).buy("AAT", 10);
        verify(poorBot, times(1)).sell("AAT", 10);

        verify(investmentGroupBot, times(1)).buy("AAT", 100);
        verify(investmentGroupBot, times(1)).sell("AAT", 100);

        verify(bigInstitutionBot, times(1)).buy("AAT", 1000);
        verify(bigInstitutionBot, times(1)).sell("AAT", 1000);
    }
}
