/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class GetMetadataResourceImplTest extends AbstractResourceImplTest
{
    @Test
    public void testErrorGetWithInvalidObjectType() throws Exception
    {
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "NoSuchPoddConcept";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response is empty as no such object exists
        Assert.assertNull("Expected NULL for response text", results.getText());
    }
    
    @Test
    public void testGetChildrenWithInvestigationRdf() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                        .getOntologyIRI().toString();
        
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "Investigation";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "false");
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                PoddWebConstants.METADATA_ONLY_CONTAINS);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 57);
        
        // FIXME: assert that FieldConditions and GrowthConditions are returned as possible object types for #hasEnvironment property 
        // FIXME: assert that labels for object types are returned
        
        Assert.assertEquals("Unexpected no. of properties", 8,
                model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
        Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
    }

    @Test
    public void testGetChildrenWithProjectRdf() throws Exception
    {
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "Project";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "false");
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                PoddWebConstants.METADATA_ONLY_CONTAINS);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 51);
        
        Assert.assertEquals("Unexpected no. of properties", 7,
                model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
        Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
    }
    
    @Test
    public void testGetChildrenWithPublicationRdf() throws Exception
    {
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "Publication";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "false");
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                PoddWebConstants.METADATA_ONLY_CONTAINS);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();

        // verify:
        Assert.assertNull("No content since Publication cannot have child objects", body);
//        final Model model =
//                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 7);
//        
//        Assert.assertEquals("Unexpected no. of properties", 1,
//                model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
//        Assert.assertEquals("Expected no Do-Not-Display properties", 0,
//                model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
    }
    
    @Test
    public void testGetWithGenotypeRdf() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                        .getOntologyIRI().toString();
        
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "Genotype";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "true");
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                PoddWebConstants.METADATA_ALL);
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify: received contents are in RDF
        Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
        Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
        
        final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFXML, 133);
        
        Assert.assertEquals("Unexpected no. of properties", 18,
                model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
        Assert.assertEquals("Expected no Do-Not-Display properties", 3,
                model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
    }
    
    @Test
    public void testGetWithProjectRdf() throws Exception
    {
        final ClientResource createObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
        
        final String objectType = PoddRdfConstants.PODD_SCIENCE + "Project";
        createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
        
        // include-do-not-display-properties defaults to false
        // metadata-policy defaults to exclude sub-properties of poddBase:contains
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 125);
        
        Assert.assertEquals("Unexpected no. of properties", 15,
                model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
        Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
    }
    
}
