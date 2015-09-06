## points-lines-merger

#### use case

you have a map with roads in wkt files, and a bunch of bus stops with location from certain transit agency. Stops' coordinates have been aligned with the map. Now you want each stop has a point representing it and connected with the road network constructed by those wkt files. This tool just does this.

#### input files

- one json file containing point-id and point coordinates. 
- one or multiple wkt files containing linestrings or multilinestrings.

the json structure of the point file:
```json
{
	<point_id_str> : { "x" : <x_double>, "y" : <y_double> },
	<point_id_str> : { "x" : <x_double>, "y" : <y_double> },
	...
}
```
#### output files

- a json file containing all the input points with changed coordinates by the merge:
- the same number of wkt files, with linestrings with merged points
- a wkt point file, containing the points with changed coordinates by merge.

#### merge rule

a point is inserted to its closest line segment, at the perpendicular intersection point, or just moved to the closest segment endpoint if the endpoint is closer.  
If the distance to any segments or endpoints from the point is farther than 50 meters, it does not change this point.

