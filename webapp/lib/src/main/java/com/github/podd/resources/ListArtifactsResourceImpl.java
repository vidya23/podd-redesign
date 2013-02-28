/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resource which lists the existing artifacts in PODD.
 * <p>
 * TODO: list based on authorization, group projects. list project title, description, PI and lead
 * institution
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ListArtifactsResourceImpl extends AbstractPoddResourceImpl
{
    public static final String LIST_PAGE_TITLE_TEXT = "PODD Artifact Listing";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Handle http GET request to serve the list artifacts page.
     */
    @Get("html")
    public Representation getListArtifactsPage(final Representation entity) throws ResourceException
    {
        this.log.info("@Get listArtifacts Page");
        
        Collection<InferredOWLOntologyID> artifacts = getArtifactsInternal();
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "projects.html.ftl");
        dataModel.put("pageTitle", ListArtifactsResourceImpl.LIST_PAGE_TITLE_TEXT);
        
        // Disable currently unimplemented features
        dataModel.put("canFilter", Boolean.FALSE);
        dataModel.put("hasFilter", Boolean.FALSE);
        dataModel.put("userCanCreate", Boolean.FALSE);
        
        this.log.info("artifacts: {}", artifacts);
        
        this.populateDataModelWithArtifactLists(dataModel, artifacts);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    private Collection<InferredOWLOntologyID> getArtifactsInternal() throws ResourceException
    {
        Collection<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        final String publishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_PUBLISHED);
        final String unpublishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_UNPUBLISHED);
        
        // default to both published and unpublished to start with
        boolean published = true;
        boolean unpublished = false;
        
        if(publishedString != null)
        {
            published = Boolean.parseBoolean(publishedString);
        }
        
        // If the user is authenticated, set unpublished to true before checking the query
        // parameters
        if(this.getClientInfo().isAuthenticated())
        {
            unpublished = true;
        }
        
        if(unpublishedString != null)
        {
            unpublished = Boolean.parseBoolean(unpublishedString);
        }
        
        if(published)
        {
            this.log.info("Including published artifacts");
        }
        
        if(unpublished)
        {
            this.log.info("Including unpublished artifacts");
        }
        
        if(!published && !unpublished)
        {
            this.log.error("Both published and unpublished artifacts were disabled in query");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Both published and unpublished artifacts were disabled in query");
        }
        
        try
        {
            if(published)
            {
                final Collection<InferredOWLOntologyID> publishedArtifacts =
                        this.getPoddArtifactManager().listPublishedArtifacts();
                
                for(final InferredOWLOntologyID nextPublishedArtifact : publishedArtifacts)
                {
                    if(this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ,
                            Arrays.asList(nextPublishedArtifact.getOntologyIRI().toOpenRDFURI()), false))
                    {
                        // If the authentication succeeded add the artifact
                        results.add(nextPublishedArtifact);
                    }
                }
            }
            
            if(unpublished)
            {
                this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_LIST);
                final Collection<InferredOWLOntologyID> unpublishedArtifacts =
                        this.getPoddArtifactManager().listUnpublishedArtifacts();
                
                for(final InferredOWLOntologyID nextUnpublishedArtifact : unpublishedArtifacts)
                {
                    if(this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                            Arrays.asList(nextUnpublishedArtifact.getOntologyIRI().toOpenRDFURI()), false))
                    {
                        // If the authentication succeeded add the artifact
                        results.add(nextUnpublishedArtifact);
                    }
                }
            }
        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Database exception", e);
        }
        
        return results;
    }
    
    @Get("rdf|rj|ttl")
    public Representation getListArtifactsRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        Collection<InferredOWLOntologyID> artifactsInternal = getArtifactsInternal();
        
        RDFFormat resultFormat = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName());
        
        if(resultFormat == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                    "Could not find an RDF serialiser matching the requested mime-type: "
                            + variant.getMediaType().getName());
        }
        
        MediaType resultMediaType = MediaType.valueOf(resultFormat.getDefaultMIMEType());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream(8096);
        RDFWriter writer = Rio.createWriter(resultFormat, out);
        
        try
        {
            for(InferredOWLOntologyID nextArtifact : artifactsInternal)
            {
                writer.handleStatement(ValueFactoryImpl.getInstance().createStatement(
                        nextArtifact.getOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
                writer.handleStatement(ValueFactoryImpl.getInstance().createStatement(
                        nextArtifact.getVersionIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
                writer.handleStatement(ValueFactoryImpl.getInstance().createStatement(
                        nextArtifact.getInferredOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
                writer.handleStatement(ValueFactoryImpl.getInstance().createStatement(
                        nextArtifact.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI,
                        nextArtifact.getVersionIRI().toOpenRDFURI()));
                writer.handleStatement(ValueFactoryImpl.getInstance().createStatement(
                        nextArtifact.getVersionIRI().toOpenRDFURI(), PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                        nextArtifact.getInferredOntologyIRI().toOpenRDFURI()));
            }
        }
        catch(RDFHandlerException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not generate RDF output due to an exception in the writer", e);
        }
        
        ByteArrayRepresentation result = new ByteArrayRepresentation(out.toByteArray(), resultMediaType);
        
        return result;
    }
    
    private void populateDataModelWithArtifactLists(final Map<String, Object> dataModel,
            Collection<InferredOWLOntologyID> artifacts)
    {
        final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
        for(final InferredOWLOntologyID artifactUri : artifacts)
        {
            // FIXME: This does not actually find the real labels
            final PoddObjectLabel artifact =
                    new PoddObjectLabelImpl(artifactUri, "The title " + artifactUri,
                            "The Project is really exciting. It could lead to unbelievable productivity in agriculture");
            results.add(artifact);
        }
        dataModel.put("allProjectsList", results);
        
    }
    
}
