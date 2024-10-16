package io.github.pfwikis.layercompiler.steps;

import java.io.IOException;

import io.github.pfwikis.fractaldetailer.AddDetails;

public class AddFractalDetail extends LCStep {

    @Override
    public byte[] process() throws IOException {
        double maxDistance = ctx.getOptions().isProdDetail()?.15:.25;
        if("ice".equals(getName()))
            System.out.println();
        return AddDetails.addDetails(maxDistance, getInput());
    }

}
