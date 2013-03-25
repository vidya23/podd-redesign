/**
 * 
 */
package com.github.podd.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import com.github.podd.api.PoddRepositoryManager;

/**
 * @author kutila
 * 
 */
@Ignore
public abstract class AbstractPoddRepositoryManagerTest
{
    
    private PoddRepositoryManager testRepositoryManager;
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
        
        this.testRepositoryManager.setSchemaManagementGraph(ValueFactoryImpl.getInstance().createURI(
                "urn:test:schema-graph"));
        this.testRepositoryManager.setArtifactManagementGraph(ValueFactoryImpl.getInstance().createURI(
                "urn:test:artifact-graph"));
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testRepositoryManager.getRepository().shutDown();
        this.testRepositoryManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getArtifactManagementGraph()}.
     */
    @Test
    public final void testGetArtifactManagementGraph() throws Exception
    {
        Assert.assertNotNull("Artifact management graph was null",
                this.testRepositoryManager.getArtifactManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getFileRepositoryManagementGraph()}.
     */
    @Test
    public final void testGetFileRepositoryManagementGraph() throws Exception
    {
        Assert.assertNotNull("File repository management graph was null",
                this.testRepositoryManager.getFileRepositoryManagementGraph());
    }

    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getNewTemporaryRepository()}.
     */
    @Test
    public final void testGetNewTemporaryRepository() throws Exception
    {
        Repository newTempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        try
        {
            newTempRepository = this.testRepositoryManager.getNewTemporaryRepository();
            Assert.assertNotNull("New temporary repository was null", newTempRepository);
            Assert.assertTrue("New temporary repository was not initialized", newTempRepository.isInitialized());
            tempRepositoryConnection = newTempRepository.getConnection();
            tempRepositoryConnection.begin();
            Assert.assertEquals("New temporary repository was not empty", 0, tempRepositoryConnection.size());
        }
        finally
        {
            if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(newTempRepository != null)
            {
                newTempRepository.shutDown();
            }
        }
    }
    
    /**
     * Test method for {@link com.github.podd.impl.PoddRepositoryManagerImpl#getRepository()}.
     */
    @Test
    public final void testGetRepository() throws Exception
    {
        Assert.assertNotNull("Repository was null", this.testRepositoryManager.getRepository());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getSchemaManagementGraph()}.
     */
    @Test
    public final void testGetSchemaManagementGraph() throws Exception
    {
        Assert.assertNotNull("Schema management graph was null", this.testRepositoryManager.getSchemaManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setArtifactManagementGraph(org.openrdf.model.URI)}
     * .
     */
    @Test
    public final void testSetArtifactManagementGraph() throws Exception
    {
        final URI testArtifactMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
        this.testRepositoryManager.setArtifactManagementGraph(testArtifactMgtGraph);
        Assert.assertEquals("Artifact graph was not correctly set", testArtifactMgtGraph,
                this.testRepositoryManager.getArtifactManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setFileRepositoryManagementGraph(org.openrdf.model.URI)}
     * .
     */
    @Test
    public final void testSetFileRepositoryManagementGraph() throws Exception
    {
        final URI testFileRepositoryMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:file-repository-graph");
        this.testRepositoryManager.setFileRepositoryManagementGraph(testFileRepositoryMgtGraph);
        Assert.assertEquals("File Repository graph was not correctly set", testFileRepositoryMgtGraph,
                this.testRepositoryManager.getFileRepositoryManagementGraph());
    }

    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setSchemaManagementGraph(org.openrdf.model.URI)}
     * .
     */
    @Test
    public final void testSetSchemaManagementGraph() throws Exception
    {
        final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:my-test:schema-management-graph");
        this.testRepositoryManager.setSchemaManagementGraph(testSchemaMgtGraph);
        Assert.assertEquals("Schema graph was not correctly set", testSchemaMgtGraph,
                this.testRepositoryManager.getSchemaManagementGraph());
    }
    
}
