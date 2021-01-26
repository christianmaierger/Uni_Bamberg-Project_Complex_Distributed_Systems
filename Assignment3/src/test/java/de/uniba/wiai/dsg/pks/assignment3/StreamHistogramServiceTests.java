package de.uniba.wiai.dsg.pks.assignment3;

import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream.StreamHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class StreamHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new StreamHistogramService();
    }
}