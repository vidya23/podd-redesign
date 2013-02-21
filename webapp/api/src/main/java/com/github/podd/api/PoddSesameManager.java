/**
 * 
 */
package com.github.podd.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages interactions with Sesame Repositories for PODD.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddSesameManager
{
    
    /**
     * Get all versions of the ontology with the given IRI that are managed in the given context in
     * the given repository connection.
     * <p>
     * If there are multiple versions available, then the most current version must be first in the
     * list.
     * 
     * @param ontologyIRI
     *            The Ontology IRI identifying the ontology for which versions must be accessed.
     * @param connection
     *            The repository connection to use when querying for ontology versions.
     * @param ontologyManagementGraph
     *            The URI identifying the context in which the ontologies are managed.
     * @throws OpenRDFException
     */
    List<InferredOWLOntologyID> getAllOntologyVersions(IRI ontologyIRI, RepositoryConnection connection,
            URI ontologyManagementGraph) throws OpenRDFException;
    
    /**
     * Returns current version details of an artifact ontology which has the given IRI as the
     * Ontology IRI or Version IRI.
     * 
     * @param ontologyIRI
     *            The IRI of the ontology to get current version info.
     * @param repositoryConnection
     * @param managementGraph
     *            The context of the Artifact Management Graph
     * @return An InferredOWLOntologyID containing details of the current managed version of the
     *         ontology.
     * @throws OpenRDFException
     * @throws UnmanagedArtifactIRIException
     *             If the given IRI does not refer to a managed artifact ontology
     * 
     * @since 04/01/2013
     */
    InferredOWLOntologyID getCurrentArtifactVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedArtifactIRIException;
    
    /**
     * Returns current version details of an ontology which has the given IRI as the Ontology IRI or
     * Version IRI.
     * 
     * @param ontologyIRI
     *            The IRI of the ontology to get current version info.
     * @param repositoryConnection
     * @param managementGraph
     *            The context of the Schema Management Graph
     * @return An InferredOWLOntologyID containing details of the current managed version of the
     *         ontology.
     * @throws OpenRDFException
     * @throws UnmanagedSchemaIRIException
     *             If the given IRI does not refer to a managed schema ontology
     * 
     * @since 18/12/2012
     */
    InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedSchemaIRIException;
    
    /**
     * Retrieves the ontology IRIs for all import statements found in the given Repository
     * Connection.
     * 
     * @param repositoryConnection
     * @param context
     * @return A Set containing ontology IRIs for all import statements.
     * @throws OpenRDFException
     */
    Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException;
    
    /**
     * Retrieves from the given Repository Connection, an Ontology IRI which identifies an artifact.
     * 
     * @param repositoryConnection
     * @param context
     * @return The IRI of the ontology, or null if the Repository does not contain statements
     *         representing an ontology.
     * @throws OpenRDFException
     */
    IRI getOntologyIRI(final RepositoryConnection repositoryConnection, final URI context) throws OpenRDFException;
    
    /**
     * Returns true if the combination of the Ontology IRI and the Version IRI in the given
     * ontologyID were previously published.
     * 
     * @param ontologyID
     * @param repositoryConnection
     * @return
     * @throws OpenRDFException
     */
    boolean isPublished(OWLOntologyID ontologyID, RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    /**
     * Sets the given Ontology IRI to be published. This restricts the ability to publish the
     * ontology again.
     * 
     * @param ontologyIRI
     *            The Ontology IRI identifying the ontology that needs to be published
     * @param repositoryConnection
     * @param context
     * @throws OpenRDFException
     * @throws UnmanagedArtifactIRIException
     *             If this is not a managed ontology
     */
    void setPublished(IRI ontologyIRI, RepositoryConnection repositoryConnection, URI context) throws OpenRDFException,
        UnmanagedArtifactIRIException;
    
    /**
     * Deletes the given ontologies, including removing and rearranging their links in the ontology
     * management graph as necessary.
     * 
     * @param requestedArtifactIds
     *            A collection of InferredOWLOntologyID objects containing the ontologies to be
     *            deleted, including the inferred ontology IRIs.
     * @param repositoryConnection
     *            The connection to the repository to use.
     * @param ontologyManagementGraph
     *            The URI of the context in the repository containing the management information for
     *            the ontologies.
     * @throws OpenRDFException
     */
    void deleteOntologies(Collection<InferredOWLOntologyID> requestedArtifactIds,
            RepositoryConnection repositoryConnection, URI ontologyManagementGraph) throws OpenRDFException;
    
}
