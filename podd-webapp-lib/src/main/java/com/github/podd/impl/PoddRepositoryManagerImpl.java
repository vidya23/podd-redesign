/**
 * 
 */
package com.github.podd.impl;

import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddRepositoryManagerImpl implements PoddRepositoryManager
{
    
    private Repository repository;
    private URI artifactGraph = PoddRdfConstants.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH;
    private URI schemaGraph = PoddRdfConstants.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
    
    /**
     * Default constructor, which sets up an in-memory MemoryStore repository.
     */
    public PoddRepositoryManagerImpl()
    {
        this.repository = new SailRepository(new MemoryStore());
        try
        {
            this.repository.initialize();
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository");
        }
    }
    
    /**
     * 
     * @param repository
     *            An initialized implementation of Repository.
     */
    public PoddRepositoryManagerImpl(final Repository repository)
    {
        this.repository = repository;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddRepositoryManager#getRepository()
     */
    @Override
    public Repository getRepository() throws OpenRDFException
    {
        return this.repository;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddRepositoryManager#setRepository(org.openrdf.repository.Repository)
     */
    @Override
    public void setRepository(final Repository repository) throws OpenRDFException
    {
        this.repository = repository;
    }
    
    @Override
    public Repository getNewTemporaryRepository() throws OpenRDFException
    {
        Repository result = new SailRepository(new MemoryStore());
        result.initialize();
        
        return result;
    }
    
    /**
     * This method adds information to the PODD artifact management graph, and updates the links for
     * the current version for both the ontology and the inferred ontology.
     * 
     * @param nextOntologyID
     *            The ontology ID that contains the information about the original ontology.
     * @param nextInferredOntologyID
     *            The ontology ID that contains the information about the inferred ontology.
     * @param updateCurrent
     *            If true, will update the current version if it exists. If false it will only add
     *            the current version if it does not exist.
     * @throws RepositoryException
     */
    @Override
    public void updateManagedPoddArtifactVersion(final OWLOntologyID nextOntologyID,
            final OWLOntologyID nextInferredOntologyID, final boolean updateCurrent) throws RepositoryException
    {
        
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.repository.getConnection();
            nextRepositoryConnection.begin();
            
            final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
            // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
            // be
            // generated for each new inferred ontology generation. For reference though, the
            // version is
            // equal to the ontology IRI in the prototype code. See generateInferredOntologyID
            // method
            // for the corresponding code.
            final URI nextInferredOntologyUri = nextInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            
            // type the ontology
            // 1st 3 parameters represent the triple.
            // 4th (the artifactGraph) is the "context" in which the triple is stored
            nextRepositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
            
            // TODO: remove the content of any contexts that are the object of versionIRI statements
            
            // remove previous versionIRI statements
            nextRepositoryConnection
                    .remove(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, null, this.artifactGraph);
            
            // setup a version number link for this version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri,
                    this.artifactGraph);
            
            List<Statement> currentVersions =
                    nextRepositoryConnection.getStatements(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                            false, this.artifactGraph).asList();
            
            // If there are no current versions, or we must update the current version, then do it
            // here
            if(currentVersions.isEmpty() || updateCurrent)
            {
                // remove whatever was previously there for the current version marker
                nextRepositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                        this.artifactGraph);
                
                // then insert the new current version marker
                nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                        this.artifactGraph);
            }
            
            // then do a similar process with the inferred axioms ontology
            nextRepositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
            
            // remove whatever was previously there for the current inferred version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                    this.artifactGraph);
            
            // link from the ontology IRI to the current inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                    nextInferredOntologyUri, this.artifactGraph);
            
            // remove the content for all previous inferred versions
            // NOTE: This list should not ever be very large, as we perform this step every time
            // this method is called to update the version
            final RepositoryResult<Statement> repoResults =
                    nextRepositoryConnection.getStatements(nextOntologyUri,
                            PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, this.artifactGraph);
            while(repoResults.hasNext())
            {
                final URI inferredVersionUri = IRI.create(repoResults.next().getObject().stringValue()).toOpenRDFURI();
                nextRepositoryConnection.remove(inferredVersionUri, null, null, this.artifactGraph);
            }
            
            nextRepositoryConnection.remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null,
                    this.artifactGraph);
            
            // link from the ontology version IRI to the matching inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, this.artifactGraph);
            
            nextRepositoryConnection.commit();
        }
        catch(RepositoryException e)
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
        }
        
    }
    
    /**
     * This method adds information to the Schema Ontology management graph, and updates the links
     * for the current version for both the ontology and the inferred ontology.
     * 
     * @param nextOntologyID
     *            The ontology ID that contains the information about the original ontology.
     * @param nextInferredOntologyID
     *            The ontology ID that contains the information about the inferred ontology.
     * @param updateCurrent
     *            If true, will update the current version if it exists. If false it will only add
     *            the current version if it does not exist.
     * @throws RepositoryException
     */
    @Override
    public void updateCurrentManagedSchemaOntologyVersion(final OWLOntologyID nextOntologyID,
            final OWLOntologyID nextInferredOntologyID, final boolean updateCurrent) throws RepositoryException
    {
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.repository.getConnection();
            nextRepositoryConnection.begin();
            
            final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
            // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
            // be
            // generated for each new inferred ontology generation. For reference though, the
            // version is
            // equal to the ontology IRI in the prototype code. See generateInferredOntologyID
            // method
            // for the corresponding code.
            final URI nextInferredOntologyUri = nextInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            
            // type the ontology
            nextRepositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
            // setup a version number link for this version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri,
                    this.schemaGraph);
            
            List<Statement> currentVersions =
                    nextRepositoryConnection.getStatements(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                            false, this.schemaGraph).asList();
            
            // If there are no current versions, or we must update the current version, then do it
            // here
            if(currentVersions.isEmpty() || updateCurrent)
            {
                // remove whatever was previously there for the current version marker
                nextRepositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                        this.schemaGraph);
                
                // then insert the new current version marker
                nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                        this.schemaGraph);
            }
            
            // then do a similar process with the inferred axioms ontology
            nextRepositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
            
            // remove whatever was previously there for the current inferred version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                    this.schemaGraph);
            
            // link from the ontology IRI to the current inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                    nextInferredOntologyUri, this.schemaGraph);
            
            // link from the ontology version IRI to the matching inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, this.schemaGraph);
            
            nextRepositoryConnection.commit();
        }
        catch(RepositoryException e)
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
        }
    }
    
    @Override
    public void setSchemaManagementGraph(URI schemaManagementGraph)
    {
        this.schemaGraph = schemaManagementGraph;
    }
    
    @Override
    public URI getSchemaManagementGraph()
    {
        return this.schemaGraph;
    }
    
    @Override
    public void setArtifactManagementGraph(URI artifactManagementGraph)
    {
        this.artifactGraph = artifactManagementGraph;
    }
    
    @Override
    public URI getArtifactManagementGraph()
    {
        return this.artifactGraph;
    }
    
}