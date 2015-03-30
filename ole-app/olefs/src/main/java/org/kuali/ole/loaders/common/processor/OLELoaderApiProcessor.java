package org.kuali.ole.loaders.common.processor;

import com.sun.jersey.api.core.HttpContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.describe.bo.OleLocationLevel;
import org.kuali.ole.describe.bo.OleShelvingScheme;
import org.kuali.ole.docstore.common.document.content.bib.marc.Collection;
import org.kuali.ole.loaders.common.bo.OLELoaderImportResponseBo;
import org.kuali.ole.loaders.common.bo.OLELoaderResponseBo;
import org.kuali.ole.loaders.common.constants.OLELoaderConstants;
import org.kuali.ole.loaders.common.service.OLELoaderService;
import org.kuali.ole.loaders.common.service.impl.OLELoaderServiceImpl;
import org.kuali.ole.loaders.describe.bo.*;
import org.kuali.ole.loaders.describe.service.OLELocationLoaderHelperService;
import org.kuali.ole.loaders.describe.service.OLELocationLoaderService;
import org.kuali.ole.loaders.describe.service.OLEShelvingSchemeLoaderHelperService;
import org.kuali.ole.loaders.describe.service.OLEShelvingSchemeLoaderService;
import org.kuali.ole.loaders.describe.service.impl.OLELocationLoaderHelperServiceImpl;
import org.kuali.ole.loaders.describe.service.impl.OLELocationLoaderServiceImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sheiksalahudeenm on 2/17/15.
 */
@Path("/")
public class OLELoaderApiProcessor {

    private static final Logger LOG = Logger.getLogger(OLELoaderApiProcessor.class);
    OLELocationLoaderService oleLocationLoaderService = new OLELocationLoaderServiceImpl();
    OLELocationLoaderHelperService oleLocationLoaderHelperService = new OLELocationLoaderHelperServiceImpl();
    OLELoaderService oleLoaderService;

    public OLELoaderService getOleLoaderService() {
        if(oleLoaderService == null){
            oleLoaderService = new OLELoaderServiceImpl();
        }
        return oleLoaderService;
    }

    public void setOleLoaderService(OLELoaderService oleLoaderService) {
        this.oleLoaderService = oleLoaderService;
    }

    @GET
    @Path("/location/{pk}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response exportLocation(@Context HttpContext context, @PathParam("pk") String id){
        Object object  = null;
        if(id.matches("^([\\d]*)?$")){
            object = oleLocationLoaderService.exportLocationById(id);
        }else{
            object = oleLocationLoaderService.exportLocationByCode(id);
        }
        if(object instanceof OleLocation){
            object = oleLocationLoaderHelperService.formLocationExportResponse(object,OLELoaderConstants.OLELoaderContext.LOCATION,context.getRequest().getRequestUri().toASCIIString(),true);
            return  Response.status(200).entity(object).build();
        }else if(object instanceof  OLELoaderResponseBo){
            return Response.status(((OLELoaderResponseBo) object).getStatusCode()).entity(((OLELoaderResponseBo) object).getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        return null;
    }

    @GET
    @Path("/location")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response exportAllLocations(@Context HttpContext context){
        List<OleLocation> oleLocationList = oleLocationLoaderService.exportAllLocations(context);
        if(CollectionUtils.isNotEmpty(oleLocationList)){
            Object object = oleLocationLoaderHelperService.formAllLocationExportResponse(context, oleLocationList, OLELoaderConstants.OLELoaderContext.LOCATION,
                    context.getRequest().getRequestUri().toASCIIString());
            return Response.status(200).entity(object).build();
        }
        return Response.status(500).entity(null).build();
    }

    @POST
    @Path("/location")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response importLocations(@Context HttpContext context, String bodyContent){
        LOG.info("Incoming request for Import Locations.");
        Object object = oleLocationLoaderService.importLocations(bodyContent,context);
        return getOleLoaderService().formResponseForImport(object,OLELoaderConstants.OLELoaderContext.LOCATION);
    }

    @PUT
    @Path("/location/{locationId}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response updateLocation(@Context HttpContext context, @PathParam("locationId") String locationId, String body){
        LOG.info("Incoming request for update Location.");
        OLELoaderResponseBo object = oleLocationLoaderService.updateLocationById(locationId, body, context);
        if(object.getStatusCode() == 200){
            return  Response.status(object.getStatusCode()).entity(object.getDetails()).build();
        }else{
            return  Response.status(400).entity(object.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

    }

    @GET
    @Path("/locationLevel/{pk}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response exportLocationLevel(@Context HttpContext context, @PathParam("pk") String id){
        Object object  = null;
        if(id.matches("^([\\d]*)?$")){
            object = oleLocationLoaderService.exportLocationLevelById(id);
        }else{
            object = oleLocationLoaderService.exportLocationLevelByCode(id);
        }
        if(object instanceof OleLocationLevel){
            object = oleLocationLoaderHelperService.formLocationLevelExportResponse(object, OLELoaderConstants.OLELoaderContext.LOCATION_LEVEL, context.getRequest().getRequestUri().toASCIIString(),true);
            return  Response.status(200).entity(object).build();
        }else if(object instanceof  OLELoaderResponseBo){
            return Response.status(((OLELoaderResponseBo) object).getStatusCode()).entity(((OLELoaderResponseBo) object).getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        return null;
    }

    @POST
    @Path("/locationLevel")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response importLocationLevels(@Context HttpContext context, String bodyContent){
        LOG.info("Incoming request for Import Locations Level.");
        Object object = oleLocationLoaderService.importLocationLevels(bodyContent,context);
        return getOleLoaderService().formResponseForImport(object,OLELoaderConstants.OLELoaderContext.LOCATION_LEVEL);
    }

    @PUT
    @Path("/locationLevel/{locationLevelId}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response updateLocationLevel(@Context HttpContext context, @PathParam("locationLevelId") String locationLevelId, String body){
        LOG.info("Incoming request for update Location Level.");
        OLELoaderResponseBo object = oleLocationLoaderService.updateLocationLevelById(locationLevelId, body, context);
        if(object.getStatusCode() == 200){
            return  Response.status(object.getStatusCode()).entity(object.getDetails()).build();
        }else{
            return  Response.status(400).entity(object.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

    }



    @GET
    @Path("/shelvingScheme/{pk}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response exportShelvingScheme(@Context HttpContext context, @PathParam("pk") String id){
        Object object  = null;
        OLEShelvingSchemeLoaderService oleShelvingSchemeLoaderService = (OLEShelvingSchemeLoaderService) getOleLoaderService().getLoaderService("schelingScheme");
        OLEShelvingSchemeLoaderHelperService oleShelvingSchemeLoaderHelperService = (OLEShelvingSchemeLoaderHelperService) getOleLoaderService().getLoaderHelperService("shelvingScheme");
        if(id.matches("^([\\d]*)?$")){
            object = oleShelvingSchemeLoaderService.exportShelvingSchemeById(id);
        }else{
            object = oleShelvingSchemeLoaderService.exportShelvingSchemeById(id);
        }
        if(object instanceof OleShelvingScheme){
            object = oleShelvingSchemeLoaderHelperService.formOleShelvingSchemeExportResponse(object, OLELoaderConstants.OLELoaderContext.LOCATION_LEVEL, context.getRequest().getRequestUri().toASCIIString(), true);
            return  Response.status(200).entity(object).build();
        }else if(object instanceof  OLELoaderResponseBo){
            return Response.status(((OLELoaderResponseBo) object).getStatusCode()).entity(((OLELoaderResponseBo) object).getMessage()).type(MediaType.TEXT_PLAIN).build();
        }
        return null;
    }

    @GET
    @Path("/shelvingScheme")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response exportAllShelvingSchemes(@Context HttpContext context){
        OLEShelvingSchemeLoaderService oleShelvingSchemeLoaderService = (OLEShelvingSchemeLoaderService) getOleLoaderService().getLoaderService("schelingScheme");
        OLEShelvingSchemeLoaderHelperService oleShelvingSchemeLoaderHelperService = (OLEShelvingSchemeLoaderHelperService) getOleLoaderService().getLoaderHelperService("shelvingScheme");
        List<OleShelvingScheme> oleShelvingSchemeList = oleShelvingSchemeLoaderService.exportAllShelvingSchemes();
        if(CollectionUtils.isNotEmpty(oleShelvingSchemeList)){
            Object object = oleShelvingSchemeLoaderHelperService.formAllShelvingSchemeExportResponse(context, oleShelvingSchemeList, OLELoaderConstants.OLELoaderContext.SHELVING_SCHEME,
                    context.getRequest().getRequestUri().toASCIIString());
            return Response.status(200).entity(object).build();
        }
        return Response.status(500).entity(null).build();
    }

    @POST
    @Path("/shelvingScheme")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response importShelvingSchemes(@Context HttpContext context, String bodyContent){
        LOG.info("Incoming request for Import Locations Level.");
        OLEShelvingSchemeLoaderService oleShelvingSchemeLoaderService = (OLEShelvingSchemeLoaderService) getOleLoaderService().getLoaderService("schelingScheme");
        OLEShelvingSchemeLoaderHelperService oleShelvingSchemeLoaderHelperService = (OLEShelvingSchemeLoaderHelperService) getOleLoaderService().getLoaderHelperService("shelvingScheme");
        Object object = oleShelvingSchemeLoaderService.importShelvingSchemes(bodyContent, context);
        return getOleLoaderService().formResponseForImport(object,OLELoaderConstants.OLELoaderContext.SHELVING_SCHEME);
    }

    @PUT
    @Path("/shelvingScheme/{shelvingSchemeId}")
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public Response updateShelvingScheme(@Context HttpContext context, @PathParam("shelvingSchemeId") String shelvingSchemeId, String body){
        LOG.info("Incoming request for update Location.");
        OLEShelvingSchemeLoaderService oleShelvingSchemeLoaderService = (OLEShelvingSchemeLoaderService) getOleLoaderService().getLoaderService("schelingScheme");
        OLELoaderResponseBo object = oleShelvingSchemeLoaderService.updateShelvingSchemeById(shelvingSchemeId, body, context);
        if(object.getStatusCode() == 200){
            return  Response.status(object.getStatusCode()).entity(object.getDetails()).build();
        }else{
            return  Response.status(400).entity(object.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

    }


}
