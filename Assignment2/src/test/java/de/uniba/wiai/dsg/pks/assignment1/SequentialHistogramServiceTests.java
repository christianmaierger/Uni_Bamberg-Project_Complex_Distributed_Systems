package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment1.histogram.sequential.SequentialHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class SequentialHistogramServiceTests extends AbstractHistogramServiceTests {

	@BeforeEach
	public void before() {
		this.histogramService = new SequentialHistogramService();
	}
}