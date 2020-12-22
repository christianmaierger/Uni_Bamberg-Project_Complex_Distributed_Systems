package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin.ForkJoinHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class ForkJoinHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new ForkJoinHistogramService();
    }
}
