package org.osm2world.core.world.modules.building.indoor;

import org.osm2world.core.math.*;
import org.osm2world.core.math.algorithms.TriangulationUtil;
import org.osm2world.core.math.shapes.ShapeXZ;
import org.osm2world.core.target.Target;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.world.modules.building.BuildingPart;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;
import static org.osm2world.core.math.VectorXYZ.*;
import static org.osm2world.core.target.common.material.NamedTexCoordFunction.GLOBAL_X_Z;
import static org.osm2world.core.target.common.material.TexCoordUtil.triangleTexCoordLists;

public class Ceiling {

    private final BuildingPart buildingPart;
    private final Material material;
    private final PolygonWithHolesXZ polygon;
    private final double floorHeight;
    private Boolean render;
    private final IndoorObjectData data;

    public Ceiling(BuildingPart buildingPart, Material material, PolygonWithHolesXZ polygon, double floorHeightAboveBase, Boolean renderable, IndoorObjectData data){
        this.buildingPart = buildingPart;
        this.material = material;
        this.polygon = polygon;
        this.floorHeight = floorHeightAboveBase;
        this.render = renderable;
        this.data = data;
    }

    public void renderTo(Target target) {

        if (render && polygon != null) {

            double floorEle = buildingPart.getBuildingPartBaseEle() + floorHeight;

            List<VectorXZ> polygonVertices = polygon.getOuter().makeCounterclockwise().getVertexList();

            List<VectorXYZ> verticesWithEle = new ArrayList<>();

            for (int i = 0; i < polygonVertices.size() - 1; i++) {

                List<VectorXYZ> topPoints = IndoorUtil.generateTopPoints(target, Arrays.asList(polygonVertices.get(i), polygonVertices.get(i + 1)), floorEle, data);
                verticesWithEle.addAll(topPoints.subList(0, topPoints.size() - 1));

            }

            verticesWithEle.add(verticesWithEle.get(0));

            PolygonWithHolesXZ poly = new PolygonWithHolesXZ(new SimplePolygonXZ(verticesWithEle.stream().map(v -> v.xz()).collect(toList())), emptyList());


            Map<VectorXZ, Double> XZHeight = new HashMap<>();

            for (VectorXYZ v : verticesWithEle) {
                XZHeight.put(v.xz(), v.y);
            }

            VectorXYZ bottom = new VectorXYZ(0,floorEle - 0.2,0);
            VectorXYZ top = new VectorXYZ(0,floorEle,0);

            List<VectorXYZ> path = new ArrayList<>();
            path.add(bottom);
            path.add(top);

//            target.drawExtrudedShape(material, shape, path, nCopies(2, Z_UNIT), null, null, null);

            Collection<TriangleXZ> triangles = TriangulationUtil.triangulate(poly);

            List<TriangleXYZ> trianglesXYZ = new ArrayList<>();

            for (TriangleXZ triangleXZ : triangles) {
                VectorXYZ v1 = triangleXZ.v1.xyz(XZHeight.get(triangleXZ.v1));
                VectorXYZ v2 = triangleXZ.v2.xyz(XZHeight.get(triangleXZ.v2));
                VectorXYZ v3 = triangleXZ.v3.xyz(XZHeight.get(triangleXZ.v3));
                if (triangleXZ.isClockwise()) {
                    trianglesXYZ.add(new TriangleXYZ(v3, v2, v1));
                } else  {
                    trianglesXYZ.add(new TriangleXYZ(v1, v2, v3));
                }


            }


//            List<TriangleXYZ> trianglesXYZ = triangles.stream()
//                    .map(t -> t.makeClockwise().xyz(floorEle - 0.2))
//                    .collect(toList());

            target.drawTriangles(material, trianglesXYZ,
                    triangleTexCoordLists(trianglesXYZ, material, GLOBAL_X_Z));

        }
    }
}
