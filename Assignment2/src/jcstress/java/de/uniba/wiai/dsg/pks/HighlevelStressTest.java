package de.uniba.wiai.dsg.pks;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighlevelHistogramService;

public class HighlevelStressTest {

	static HistogramService service = new HighlevelHistogramService();

	@JCStressTest
	@Outcome(id = "terminated=true, histogram=[A] 5, [B] 0, [C] 0, [D] 3, [E] 10, [F] 2, [G] 0, [H] 2, [I] 6, [J] 0, [K] 0, [L] 5, [M] 5, [N] 1, [O] 0, [P] 4, [Q] 0, [R] 2, [S] 9, [T] 4, [U] 0, [V] 1, [W] 0, [X] 1, [Y] 3, [Z] 0,  lines: 7 files: 5 processedFiles: 4 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestSimple {

		String message = "";
		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				histogram = HighlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/simple/input", ".txt");
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
	@Outcome(id = "terminated=true, histogram=[A] 0, [B] 0, [C] 0, [D] 0, [E] 0, [F] 0, [G] 0, [H] 0, [I] 0, [J] 0, [K] 0, [L] 0, [M] 0, [N] 0, [O] 0, [P] 0, [Q] 0, [R] 0, [S] 0, [T] 0, [U] 0, [V] 0, [W] 0, [X] 0, [Y] 0, [Z] 0,  lines: 0 files: 5 processedFiles: 0 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestSimpleNoFiles {

		String message = "";
		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				histogram = HighlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/simple/input", ".xsd");
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
	@Outcome(id = "terminated=true, histogram=[A] 0, [B] 0, [C] 0, [D] 0, [E] 0, [F] 0, [G] 0, [H] 0, [I] 0, [J] 0, [K] 0, [L] 0, [M] 0, [N] 0, [O] 0, [P] 0, [Q] 0, [R] 0, [S] 0, [T] 0, [U] 0, [V] 0, [W] 0, [X] 0, [Y] 0, [Z] 0,  lines: 0 files: 5 processedFiles: 0 directories: 2]", expect = Expect.ACCEPTABLE, desc = "Correctly counted")
	@State
	public static class TestOnlyInSubfoldersNoFiles {

		String message = "";
		int numberOfExceptions = 0;
		Histogram histogram = new Histogram();

		@Actor
		public void actor1() {
			try {
				histogram = HighlevelStressTest.service.calculateHistogram("../../resources/jcstress/data/onlyInSubfolders/input", ".xsd");
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
