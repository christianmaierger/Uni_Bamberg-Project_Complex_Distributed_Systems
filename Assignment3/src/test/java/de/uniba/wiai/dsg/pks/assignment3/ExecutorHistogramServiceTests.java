package de.uniba.wiai.dsg.pks.assignment3;

import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor.ExecutorHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class ExecutorHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new ExecutorHistogramService();
    }
}
