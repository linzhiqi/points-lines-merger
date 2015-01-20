package merger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import util.Coord;
import junit.framework.TestCase;

public class MergeTest extends TestCase{

	@Test
	public void testMergeToClosestSegment1() {
		List<LinesInFile> network = new ArrayList<LinesInFile>();
		List<List<Coord>> lines = new ArrayList<List<Coord>>();
		List<Coord> line = new ArrayList<Coord>();
		line.add(new Coord(0,5));
		line.add(new Coord(20d/3d, 0));
		lines.add(line);
		LinesInFile file = new LinesInFile("roads.wkt", lines);
		network.add(file);
		
		HashMap<String, Coord> pointsMap = new HashMap<String, Coord>();
		pointsMap.put("001", new Coord(0, 0));
		Coord expectedPoint = new Coord(2.4d, 3.2d);
		assertTrue(line.size()==2);
		Merger.merge(network, pointsMap);
		assertTrue(line.size()==3);
		assertTrue(line.get(1).closeEnough(expectedPoint, 0.01));
	}
	
	@Test
	public void testMergeToClosestSegment2() {
		List<LinesInFile> network = new ArrayList<LinesInFile>();
		List<List<Coord>> lines = new ArrayList<List<Coord>>();
		List<Coord> line = new ArrayList<Coord>();
		
		
		line.add(new Coord(2551999.233146243, 6676091.3199029695));
		line.add(new Coord(2552044.971907583, 6676120.406555651));
		
		
		line.add(new Coord(2552059.006217502, 6676134.659015465));
		line.add(new Coord(2552072.0224945764, 6676152.83817339));
		line.add(new Coord(2552079.8758908003, 6676177.925411328));
		line.add(new Coord(2552088.333283496, 6676239.403275532));
		line.add(new Coord(2552098.695403513, 6676362.221666476));
		
		lines.add(line);
		LinesInFile file = new LinesInFile("roads.wkt", lines);
		network.add(file);
		
		HashMap<String, Coord> pointsMap = new HashMap<String, Coord>();
		pointsMap.put("001", new Coord(2552094.8777803495, 6676311.756324074));
		Coord expectedPoint = new Coord(2552094.4778388753, 6676311.79268239);
		assertTrue(line.size()==7);
		Merger.merge(network, pointsMap);
		assertTrue(line.size()==8);
		assertTrue(line.get(6).closeEnough(expectedPoint, 0.1));
	}
	
	@Test
	public void testMergeToClosestPoint1() {
		List<LinesInFile> network = new ArrayList<LinesInFile>();
		List<List<Coord>> lines = new ArrayList<List<Coord>>();
		List<Coord> line = new ArrayList<Coord>();
		line.add(new Coord(0,5));
		line.add(new Coord(20d/3d, 0));
		line.add(new Coord(1,1));
		lines.add(line);
		LinesInFile file = new LinesInFile("roads.wkt", lines);
		network.add(file);
		
		HashMap<String, Coord> pointsMap = new HashMap<String, Coord>();
		pointsMap.put("001", new Coord(0, 0));
		
		assertTrue(line.size()==3);
		Merger.merge(network, pointsMap);
		assertTrue(line.size()==3);
		assertTrue(pointsMap.get("001").equals(line.get(2)));
	}
	
	@Test
	public void testMergeToClosestSegmentEnd1() {
		List<LinesInFile> network = new ArrayList<LinesInFile>();
		List<List<Coord>> lines = new ArrayList<List<Coord>>();
		List<Coord> line = new ArrayList<Coord>();
		line.add(new Coord(2.4, 3.2));
		line.add(new Coord(20d/3d, 0));
		lines.add(line);
		LinesInFile file = new LinesInFile("roads.wkt", lines);
		network.add(file);
		
		HashMap<String, Coord> pointsMap = new HashMap<String, Coord>();
		pointsMap.put("001", new Coord(0, 0));
		
		assertTrue(line.size()==2);
		Merger.merge(network, pointsMap);
		assertTrue(line.size()==2);
		assertTrue(pointsMap.get("001").equals(line.get(0)));
	}
	
}
