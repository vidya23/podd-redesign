/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UploadArtifactResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test Upload attempt with an artifact that is inconsistent. 
     * Results in an HTTP 500 Internal Server Error with detailed error causes
     * in the RDF body. 
     */
    @Test
    public void testErrorUploadWithInconsistentArtifactRdf() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES,
                        MediaType.APPLICATION_RDF_XML);
        
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                    mediaType, Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
        }
        catch(final ResourceException e)
        {
            // verify: error details
            Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());
            
            final String body = uploadArtifactClientResource.getResponseEntity().getText();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            final Model model = Rio.parse(inputStream, "", responseFormat);
            
            Assert.assertEquals("Not the expected results size", 10, model.size());
            final Set<Resource> errors = model.filter(null, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR).subjects();
            Assert.assertEquals("Not the expected number of Errors", 1, errors.size());
            
            Assert.assertEquals("Not the expected error source", "urn:temp:inconsistentArtifact:1", 
            model.filter(null, PoddRdfConstants.ERR_SOURCE, null).objectString());
            
            //DebugUtils.printContents(model);
        }
    }    
    
    /**
     * Test Upload attempt with an artifact that is inconsistent. 
     * Results in an HTTP 500 Internal Server Error with detailed error causes
     * in the RDF body. 
     */
    @Test
    public void testErrorUploadWithNotInOwlDlProfileArtifactRdf() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_NOT_OWL_DL,
                        MediaType.APPLICATION_RDF_XML);
        
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                    mediaType, Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
        }
        catch(final ResourceException e)
        {
            // verify: error details
            Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());
            
            final String body = uploadArtifactClientResource.getResponseEntity().getText();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            final Model model = Rio.parse(inputStream, "", responseFormat);
            
            Assert.assertEquals("Not the expected results size", 15, model.size());
            final Set<Resource> errors = model.filter(null, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR).subjects();
            Assert.assertEquals("Not the expected number of Errors", 3, errors.size());
            
            Assert.assertEquals(
                    "Expected error sources not found",
                    2,
                    model.filter(
                            null,
                            PoddRdfConstants.ERR_SOURCE,
                            PoddRdfConstants.VF
                                    .createLiteral("ClassAssertion(owl:Individual <mailto:helen.daily@csiro.au>)"))
                            .size());
            
            //DebugUtils.printContents(model);
        }
    }    
    
    /**
     * Test unauthenticated access to "upload artifact" leads to an UNAUTHORIZED error.
     */
    @Test
    public void testErrorUploadWithoutAuthentication() throws Exception
    {
        final ClientResource uploadArtifactResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                        MediaType.APPLICATION_RDF_XML);
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("file", input));
        
        try
        {
            uploadArtifactResource.post(form, MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test upload attempt without actual file leads to a BAD_REQUEST error
     */
    @Test
    public void testErrorUploadWithoutFile() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException with Status Code 400");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    
    /**
     * Test authenticated access to the upload Artifact page in HTML
     */
    @Test
    public void testGetUploadArtifactPageBasicHtml() throws Exception
    {
        // prepare: add an artifact
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("Upload new artifact"));
        Assert.assertTrue(body.contains("type=\"file\""));
        
        this.assertFreemarker(body);
    }
    
    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects an HTML response.
     */
    @Test
    public void testUploadArtifactBasicHtml() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                        MediaType.APPLICATION_RDF_XML);
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("file", input));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // TODO: verify results once a proper success page is incorporated.
        final String body = results.getText();
        Assert.assertTrue(body.contains("Artifact successfully uploaded"));
        this.assertFreemarker(body);
    }
    
    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicRdf() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                        MediaType.APPLICATION_RDF_XML);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                        MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: results (expecting the added artifact's ontology IRI)
        final String body = results.getText();
        Assert.assertTrue(body.contains("http://"));
        Assert.assertFalse(body.contains("html"));
        Assert.assertFalse(body.contains("\n"));
    }
    
    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicRdfWithFormData() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                        MediaType.APPLICATION_RDF_XML);
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("file", input));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                        MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: results (expecting the added artifact's ontology IRI)
        final String body = results.getText();
        Assert.assertTrue(body.contains("http://"));
        Assert.assertFalse(body.contains("html"));
        Assert.assertFalse(body.contains("\n"));
    }
    
    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicTurtle() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_TTL_1_INTERNAL_OBJECT,
                        MediaType.APPLICATION_RDF_TURTLE);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: results (expecting the added artifact's ontology IRI)
        final String body = results.getText();
        
        final Collection<InferredOWLOntologyID> ontologyIDs = OntologyUtils.stringToOntologyID(body, RDFFormat.TURTLE);
        
        Assert.assertNotNull("No ontology IDs in response", ontologyIDs);
        Assert.assertEquals("More than 1 ontology ID in response", 1, ontologyIDs.size());
        Assert.assertTrue("Ontology ID not of expected format",
                ontologyIDs.iterator().next().toString().contains("artifact:1:version:1"));
    }
    
}
