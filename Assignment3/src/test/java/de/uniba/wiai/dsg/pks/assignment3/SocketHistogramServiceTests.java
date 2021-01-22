package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor.ExecutorHistogramService;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client.SocketHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class SocketHistogramServiceTests extends AbstractHistogramServiceTests {

    @BeforeEach
    public void before() {
        this.histogramService = new SocketHistogramService("localhost", 1337);
    }
}
