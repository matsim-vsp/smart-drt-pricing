package org.matsim.analysis;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zmeng
 */
public class PersonInDrtServiceAreaFilter {

    private final Population population;
    private final String drtServiceArea;
    private Collection<SimpleFeature> simpleFeatures;



    public PersonInDrtServiceAreaFilter(Population population, String drtServiceArea) {
        this.population = population;
        this.drtServiceArea = drtServiceArea;
        this.simpleFeatures = ShapeFileReader.getAllFeatures(drtServiceArea);
    }

    public void filter(){
        for (Person person :
                this.population.getPersons().values()) {
            var coords = getCoordinatesFromPlan(person.getSelectedPlan());
            person.getAttributes().putAttribute("inDrtServiceArea",inDrtServiceArea(coords));
        }
    }

    private boolean inDrtServiceArea(List<Coordinate> coords) {
        List<Boolean> inServiceArea = new LinkedList<>();
        for (Coordinate coordinate:
             coords) {
            boolean temIn = false;
            for (SimpleFeature s :
                    this.simpleFeatures) {
                Geometry geometry = (Geometry)s;
                if (geometry.contains(new GeometryFactory().createPoint(coordinate)))
                    temIn = true;
                    break;
            }
            inServiceArea.add(temIn);
        }
        return inServiceArea.contains(true);
    }

    private List<Coordinate> getCoordinatesFromPlan(Plan plan){
        var activities = TripStructureUtils.getActivities(plan.getPlanElements(), TripStructureUtils.StageActivityHandling.ExcludeStageActivities);
        return activities.stream().map(activity -> new Coordinate(activity.getCoord().getX(),activity.getCoord().getY())).collect(Collectors.toList());
    }




}
