package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighlevelHistogramService;
import org.junit.jupiter.api.BeforeEach;

public class HighlevelHistogramServiceTests extends AbstractHistogramServiceTests {

	@BeforeEach
	public void before() {
		this.histogramService = new HighlevelHistogramService();
	}
}
