package io.github.pfwikis;

import static io.github.pfwikis.model.Geometry.ROUND_TO_7;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.pfwikis.model.*;

public class DownloadLoI {

    public static void main(String[] args) throws IOException {
        String url = Helper.buildQuery("https://pathfinderwiki.com/w/api.php",
            "action","ask",
            "format","json",
            "utf8","1",
            "api_version","3","formatversion","2",
            "query", String.join("",
                   "[[Has meta type::Location of Interest]][[Has coordinates::+]][[:+]]",
                "OR [[Has meta type::Location of Interest]][[Has coordinates::+]][[-Has subobject::PathfinderWiki:Map Locations Without Articles]]",
                "|?Has location type",
                "|?Has coordinates",
                "|?Has name"
            )
        );

        int offset = 0;

        var lois = new ArrayList<LoI>();

        while (true) {
            var type = new ObjectMapper().getTypeFactory().constructParametricType(Response.class, LoI.class);
            Response<LoI> array = Helper.read(url + URLEncoder.encode("|offset=" + offset, StandardCharsets.UTF_8), type);
            offset += 50;
            lois.addAll(array.getQuery().getResults());
            if (array.getQuery().getResults().size() < 50) {
                break;
            }
        }
        lois.sort(Comparator.comparing(LoI::getName));

        System.out.println("Found " + lois.size() + " LoIs.");

        var arr = new ArrayList<Feature>();
        for (var loi : lois) {
            try {
                var properties = new Properties();
                properties.setName(Helper.handleName(loi.getName(), loi.getPageName()));
                properties.setLink("https://pathfinderwiki.com/wiki/" + loi.getPageName().replace(' ', '_'));
                properties.setType(loi.getType());
                properties.setFullUrl(loi.getFullUrl());
                var geometry = new Geometry();
                geometry.setCoordinates(List.of(
                    loi.getCoordsLon().round(ROUND_TO_7),
                    loi.getCoordsLat().round(ROUND_TO_7)
                ));

                var feature = new Feature(properties, geometry);

                arr.add(feature);
            } catch (Exception e) {
                System.err.println("Failed for " + loi.getPageName());
                e.printStackTrace();
            }
        }

        var result = new FeatureCollection("cities", arr);
        Jackson.get().writer().withDefaultPrettyPrinter().writeValue(new File("../sources/locations.geojson"), result);
    }
}
