package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowlevelHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class LowlevelHistogramServiceTests extends AbstractHistogramServiceTests {

	@BeforeEach
	public void before() {
		this.histogramService = new LowlevelHistogramService();
	}
}
