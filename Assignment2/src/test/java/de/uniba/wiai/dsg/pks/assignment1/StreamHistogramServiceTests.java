package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment1.histogram.sequential.SequentialHistogramService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream.StreamHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class StreamHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new StreamHistogramService();
    }
}