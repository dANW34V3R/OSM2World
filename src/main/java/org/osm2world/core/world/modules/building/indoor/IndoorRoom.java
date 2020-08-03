package org.osm2world.core.world.modules.building.indoor;

import org.osm2world.core.map_data.data.*;
import org.osm2world.core.target.Renderable;
import org.osm2world.core.target.Target;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.target.common.material.Materials;
import org.osm2world.core.world.modules.building.BuildingDefaults;
import org.osm2world.core.world.modules.building.BuildingPart;

import java.util.ArrayList;
import java.util.List;

public class IndoorRoom implements Renderable {

    private final List<IndoorWall> walls;
    private final IndoorFloor floor;
    private final Ceiling ceiling;

    private final IndoorObjectData data;

    public IndoorRoom(IndoorObjectData data){

        this.data = data;

        this.walls = splitIntoIndoorWalls();

        floor = new IndoorFloor(data.getBuildingPart(),
                data.getSurface(),
                data.getPolygon(),
                data.getLevelHeightAboveBase(),
                data.getRenderableLevels().contains(data.getMinLevel()));

        ceiling = new Ceiling(data.getBuildingPart(),
                data.getMaterial(BuildingDefaults.getDefaultsFor(data.getBuildingPart().getTags()).materialWall),
                data.getPolygon(),
                data.getTopOfTopLevelHeightAboveBase(),
                data.getRenderableLevels().contains(data.getMaxLevel()), data);
    }

    private List<IndoorWall> splitIntoIndoorWalls(){

        List<IndoorWall> result = new ArrayList<>();

        result.add(new IndoorWall(data.getBuildingPart(), data.getMapElement()));

        return result;
    }



    @Override
    public void renderTo(Target target) {

        walls.forEach(w -> w.renderTo(target));

        floor.renderTo(target);

        ceiling.renderTo(target);

    }
}

