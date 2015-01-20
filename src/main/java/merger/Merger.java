package merger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.Coord;
import util.IOUtil;
import util.WKTReader;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Merger {

	private List<String> wktFileNames;

	private List<LinesInFile> network;
	private String pointsFileName;
	private HashMap<String, Coord> pointsMap;
	private WKTReader wktReader = new WKTReader();

	public Merger(List<String> linesFiles, String pointsFile) {
		this.wktFileNames = linesFiles;
		this.pointsFileName = pointsFile;
	}

	public HashMap<String, Coord> getPointsMap() {
		return pointsMap;
	}

	public List<LinesInFile> getNetwork() {
		return this.network;
	}

	public void writeMergedNetwork() {
		for (LinesInFile file : network) {
			IOUtil.writeLinesToWKTFile(file.getLines(), file.getFileName() + ".merged");
		}
	}

	public void buildNetwork() throws IOException {
		network = new ArrayList<LinesInFile>();

		for (String fileName : wktFileNames) {
			File file = new File(fileName);
			List<List<Coord>> lines = wktReader.readLines(file);
			network.add(new LinesInFile(fileName, lines));
		}
	}

	public void importStops() throws IOException {
		pointsMap = IOUtil.loadStopMap(pointsFileName);
	}

	public static void merge(List<LinesInFile> network, HashMap<String, Coord> pointsMap) {
		Set<String> stopIds = pointsMap.keySet();
		Iterator<String> stopIt = stopIds.iterator();
		while (stopIt.hasNext()) {
			String stopId = stopIt.next();
			Coord a = pointsMap.get(stopId);

			// values for the closest and Perpendicular intersecting line
			// segment
			double minA2bcSqr = Double.MAX_VALUE;
			Coord minb = null;
			Coord minc = null;
			double minAbSqr = Double.MAX_VALUE;
			double minAcSqr = Double.MAX_VALUE;
			double minBcSqr = Double.MAX_VALUE;
			double minX2C = Double.MAX_VALUE;
			boolean isOnMinbOrMinc = false;
			List<Coord> minLine = null;

			// values for the closest point
			Coord closestPoint = null;
			double minApSqr = Double.MAX_VALUE;

			for (LinesInFile file : network) {

				for (List<Coord> line : file.getLines()) {
					Iterator<Coord> it = line.iterator();
					assert line.size() >= 2 : "invalide "
							+ WKTReader.LINESTRING + "!";

					double abSqr = Double.MAX_VALUE;
					double acSqr = Double.MAX_VALUE;
					double bcSqr = Double.MAX_VALUE;
					double Sqrdiff = Double.MAX_VALUE;
					double x2c = Double.MAX_VALUE;
					double a2bcSqr = Double.MAX_VALUE;

					Coord b = it.next();

					// check closest point
					abSqr = a.distance2(b);
					if (abSqr < minApSqr) {
						closestPoint = b;
						minApSqr = abSqr;
					}

					Coord c = null;

					while (it.hasNext()) {
						boolean isOnSegmentEnds = false;
						c = it.next();

						abSqr = a.distance2(b);
						acSqr = a.distance2(c);
						bcSqr = b.distance2(c);
						Sqrdiff = Math.abs(abSqr - acSqr) - bcSqr;
						x2c = 0;
						a2bcSqr = 0;

						// for the closest point
						if (acSqr < minApSqr) {
							closestPoint = c;
							minApSqr = acSqr;
						}

						// for the closest and Perpendicular intersecting line
						// segment
						if (Sqrdiff > 0.5) {
							// the Perpendicular line from the point doesn't
							// intersect with the segment
							b = c;
							c = null;
							continue;
						} else if (Sqrdiff > -0.5) {
							// the Perpendicular line from the point intersects
							// with the segment at one of the two ends
							x2c = Math.sqrt(bcSqr);
							a2bcSqr = abSqr > acSqr ? acSqr : abSqr;
							isOnSegmentEnds = true;
						} else {
							// the Perpendicular line from the point intersects
							// within the segment
							x2c = (acSqr + bcSqr - abSqr)
									/ (2 * Math.sqrt(bcSqr));
							a2bcSqr = acSqr - x2c * x2c;
						}
						if (a2bcSqr < minA2bcSqr) {
							minA2bcSqr = a2bcSqr;
							minb = b;
							minc = c;
							minAbSqr = abSqr;
							minAcSqr = acSqr;
							minBcSqr = bcSqr;
							minX2C = x2c;
							isOnMinbOrMinc = isOnSegmentEnds;
							minLine = line;
						}
						b = c;
						c = null;
					}

				}

			}

			if (minb != null && minc != null) {
				// the closest point is closer than the line segment
				if (minApSqr <= minA2bcSqr) {
					if (minApSqr > 2500) {
						System.out.println("stop-" + stopId
								+ " far frm any point1");
					} else {
						// merge to the point
						pointsMap.put(stopId, closestPoint.clone());
					}
					continue;
				}

				if (minA2bcSqr > 2500) {
					System.out.println("stop-" + stopId
							+ " far frm any segment");
				}

				// other wise the segment is closer
				if (isOnMinbOrMinc) {
					// merge to one of the segment ends
					if (minAbSqr <= minAcSqr) {
						// merge to b
						pointsMap.put(stopId, minb.clone());
					} else {
						// merge to c
						pointsMap.put(stopId, minc.clone());
					}
				} else {
					// add a new coord between segment ends
					double d = minX2C / (Math.sqrt(minBcSqr));
					double newX = minc.getX() - d * (minc.getX() - minb.getX());
					double newY = minc.getY() + d * (minb.getY() - minc.getY());
					Coord newPoint = new Coord(newX, newY);

					int bIndex = minLine.indexOf(minb);
					int cIndex = minLine.indexOf(minc);
					assert bIndex == (cIndex - 1) : "invalid line string!";
					minLine.add(cIndex, newPoint);
					pointsMap.put(stopId, newPoint.clone());
				}
			} else {
				if (minApSqr > 2500) {
					System.out
							.println("stop-" + stopId + " far frm any point.");
				} else {
					// merge to the point
					pointsMap.put(stopId, closestPoint.clone());
				}
			}
		}
	}
	
	

	public static void main(String[] args) throws IOException {
		// parsing input options
		String usageStr = "usage:\t<-l lines_wkt_file> <-p points_json_file> [-h]\n" +
				"\tfor n lines_wkt_file, specify -l option n times, once for each file.\n" +
				"\tonly one points_json_file is supported. JSON format:\n" +
				"\t{\n\t    <point_id_str> : { \"x\" : <x_double>, \"y\" : " +
				"<y_double> },\n\t    ...\n\t}";
		
		OptionParser parser = new OptionParser("l:p:h");
		OptionSet options = parser.parse(args);
		
		if (options.has("h") || !options.has("l") || !options.has("p")) {
			System.out.println(usageStr);
			System.exit(0);
		}
		
		// parse lines_wkt_files
		@SuppressWarnings("unchecked")
		List<String> wktFileNames = (List<String>) options.valuesOf("l");
		
		// parse point json file
		String pointsFileName = (String)options.valueOf("p");
		
		// instantiate Merger
		Merger worker = new Merger(wktFileNames, pointsFileName);

		worker.buildNetwork();
		worker.importStops();
		Merger.merge(worker.getNetwork(), worker.getPointsMap());

		IOUtil.writeToJSONFile(worker.getPointsMap(), pointsFileName+".merged");
		worker.writeMergedNetwork();
		IOUtil.writeToWKTPoint(worker.getPointsMap(), "points_merged.wkt");
	}

}
