package de.uniba.wiai.dsg.pks.assignment3;

import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.ActorHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class ActorHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new ActorHistogramService();
    }
}