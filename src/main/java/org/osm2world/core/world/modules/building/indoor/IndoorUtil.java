package org.osm2world.core.world.modules.building.indoor;

import org.osm2world.core.math.LineSegmentXZ;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.target.Target;
import org.osm2world.core.world.modules.building.roof.Roof;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.osm2world.core.math.VectorXZ.listXYZ;

/**
 *  Utility class for indoor classes
 */
public class IndoorUtil {

	private IndoorUtil() { }

	public static List<VectorXYZ> generateTopPoints(Target target, List<VectorXZ> ends, Double heightAboveZero, IndoorObjectData data){

		/* quick return if not in roof */

		if (heightAboveZero <= data.getBuildingPart().getHeightWithoutRoof() + data.getBuildingPart().getBuildingPartBaseEle() + 1e-4) {
			return listXYZ(ends, heightAboveZero);
		}

		// TODO possibly calculated every time

		Collection<LineSegmentXZ> innerSegments = data.getBuildingPart().getRoof().getInnerSegments();

		List<VectorXZ> intersections = new ArrayList<>();
		intersections.add(ends.get(0));

		for (LineSegmentXZ roofSegment : innerSegments) {
			if (roofSegment.intersects(ends.get(0), ends.get(1))) {
				intersections.add(roofSegment.getIntersection(ends.get(0), ends.get(1)));
			}
		}

		intersections.add(ends.get(1));

		intersections.sort((v1, v2) -> Double.compare(v1.subtract(ends.get(0)).length(), v2.subtract(ends.get(0)).length()));

		Roof roof = data.getBuildingPart().getRoof();

		double levelHeightInRoof = heightAboveZero - data.getBuildingPart().getHeightWithoutRoof() - data.getBuildingPart().getBuildingPartBaseEle();
		List<VectorXZ> levelIntersections = new ArrayList<>();

		for (int i = 0; i < intersections.size() - 1; i++) {
			if ((roof.getRoofHeightAt(intersections.get(i)) > levelHeightInRoof
					&& roof.getRoofHeightAt(intersections.get(i + 1)) < levelHeightInRoof)
					|| (roof.getRoofHeightAt(intersections.get(i + 1)) > levelHeightInRoof
					&& roof.getRoofHeightAt(intersections.get(i)) < levelHeightInRoof) ) {

				double z1 = 0;
				double z2 = roof.getRoofHeightAt(intersections.get(i + 1)) - roof.getRoofHeightAt(intersections.get(i));

				double x1 = 0;
				double x2 = intersections.get(i).distanceTo(intersections.get(i + 1));

				LineSegmentXZ wallSegment = new LineSegmentXZ(new VectorXZ(x1, z1), new VectorXZ(x2, z2));

				LineSegmentXZ levelSegment = new LineSegmentXZ(
						new VectorXZ(x1, levelHeightInRoof - roof.getRoofHeightAt(intersections.get(i))),
						new VectorXZ(x2, levelHeightInRoof - roof.getRoofHeightAt(intersections.get(i))));

				VectorXZ wallLevelInt =  wallSegment.getIntersection(levelSegment.p1, levelSegment.p2);

				if (wallLevelInt != null) {

					VectorXZ inter = intersections.get(i).add(
							intersections.get(i + 1).subtract(intersections.get(i))
									.normalize().mult(wallLevelInt.getX()));

					levelIntersections.add(inter);

				}
			}
		}

		intersections.addAll(levelIntersections);

		intersections.sort((v1, v2) -> Double.compare(v1.subtract(ends.get(0)).length(), v2.subtract(ends.get(0)).length()));

		List<VectorXYZ> limitedHeights = new ArrayList<>();

		for (VectorXZ intersection : intersections) {
			limitedHeights.add(
					intersection.xyz(Math.min(data.getBuildingPart().getBuildingPartBaseEle()
									+ data.getBuildingPart().getHeightWithoutRoof()
									+ data.getBuildingPart().getRoof().getRoofHeightAt(intersection),
							heightAboveZero)));
		}

		return limitedHeights;

	}

}
