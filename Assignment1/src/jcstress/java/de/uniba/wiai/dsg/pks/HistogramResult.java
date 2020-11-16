package de.uniba.wiai.dsg.pks;

import java.io.Serializable;
import java.net.URI;

import org.openjdk.jcstress.annotations.Result;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

@Result
public class HistogramResult implements Serializable{

	public boolean terminated;
	public Histogram histogram;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((histogram == null) ? 0 : histogram.hashCode());
		result = prime * result + (terminated ? 1231 : 1237);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistogramResult other = (HistogramResult) obj;
		if (histogram == null) {
			if (other.histogram != null)
				return false;
		} else if (!histogram.equals(other.histogram))
			return false;
		if (terminated != other.terminated)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "terminated=" + terminated + ", histogram=" + getHistogramString(histogram) + "]";
	}
	
	
	private String getHistogramString(Histogram histogram) {
		StringBuilder result = new StringBuilder();
		long[] distribution = histogram.getDistribution();
		for (int i = 0; i < distribution.length; i++) {
			result.append("[" + (char)(i + 'A') + "] " + distribution[i] + ", ");
		}
		result.append(" lines: " + histogram.getLines());
		result.append(" files: " + histogram.getFiles());
		result.append(" processedFiles: " + histogram.getProcessedFiles());
		result.append(" directories: " + histogram.getDirectories());
		return result.toString();
	}
	
	
}
