package de.uniba.wiai.dsg.pks;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowlevelHistogramService;
import org.openjdk.jcstress.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LowlevelStressTest {

	static HistogramService service = new LowlevelHistogramService();
	
	@JCStressTest
	@Outcome(id = "terminated=true, histogram=[A] 0, [B] 0, [C] 0, [D] 0, [E] 0, [F] 0, [G] 0, [H] 0, [I] 0, [J] 0, [K] 0, [L] 0, [M] 0, [N] 0, [O] 0, [P] 0, [Q] 0, [R] 0, [S] 0, [T] 0, [U] 0, [V] 0, [W] 0, [X] 0, [Y] 0, [Z] 0,  lines: 0 files: 0 processedFiles: 0 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestNoFiles {

		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				Path source = Path.of("../../resources/jcstress/data/noFiles/input/subfolder").toAbsolutePath();
				Files.createDirectories(source);
				histogram = LowlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/noFiles/input", ".txt");
			} catch (IOException | HistogramServiceException e) {
				numberOfExceptions++;
			}

		}

		@Arbiter
		public void result(HistogramResult result) {
			result.histogram = histogram;
			result.terminated = numberOfExceptions > 0 ? false : true;
		}
	}
	
	@JCStressTest
	@Outcome(id = "terminated=true, histogram=[A] 2, [B] 0, [C] 0, [D] 0, [E] 5, [F] 0, [G] 0, [H] 0, [I] 3, [J] 0, [K] 0, [L] 7, [M] 3, [N] 0, [O] 0, [P] 3, [Q] 0, [R] 2, [S] 3, [T] 0, [U] 0, [V] 0, [W] 0, [X] 0, [Y] 2, [Z] 0,  lines: 1 files: 5 processedFiles: 1 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestSimpleXml {

		String message = "";
		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				histogram = LowlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/simple/input", ".xml");
			} catch (HistogramServiceException e) {
				numberOfExceptions++;
			}

		}

		@Arbiter
		public void result(HistogramResult result) {
			result.histogram = histogram;
			result.terminated = numberOfExceptions > 0 ? false : true;
		}
	}

	@JCStressTest
	@Outcome(id = "terminated=true, histogram=[A] 2, [B] 0, [C] 0, [D] 0, [E] 5, [F] 0, [G] 0, [H] 0, [I] 3, [J] 0, [K] 0, [L] 7, [M] 3, [N] 0, [O] 0, [P] 3, [Q] 0, [R] 2, [S] 3, [T] 0, [U] 0, [V] 0, [W] 0, [X] 0, [Y] 2, [Z] 0,  lines: 1 files: 5 processedFiles: 1 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestOnlyInSubfoldersXml {

		String message = "";
		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				histogram = LowlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/onlyInSubfolders/input", ".xml");
			} catch (HistogramServiceException e) {
				numberOfExceptions++;
			}

		}

		@Arbiter
		public void result(HistogramResult result) {
			result.histogram = histogram;
			result.terminated = numberOfExceptions > 0 ? false : true;
		}
	}

}
