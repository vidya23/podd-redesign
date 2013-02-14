/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;

/**
 * Interface containing URI constants for the Ontologies needed in PODD.
 * 
 * @author kutila
 * 
 */
public interface PoddRdfConstants
{
    public static final ValueFactory VALUE_FACTORY = ValueFactoryImpl.getInstance();

    /** Path to dcTerms.owl */
    public static final String PATH_PODD_DCTERMS = "/ontologies/dcTerms.owl";

    /** Path to foaf.owl */
    public static final String PATH_PODD_FOAF = "/ontologies/foaf.owl";

    /** Path to poddUser.owl */
    public static final String PATH_PODD_USER = "/ontologies/poddUser.owl";

    /** Path to poddBase.owl */
    public static final String PATH_PODD_BASE = "/ontologies/poddBase.owl";

    /** Path to poddScience.owl */
    public static final String PATH_PODD_SCIENCE = "/ontologies/poddScience.owl";

    /** Path to poddPlant.owl */
    public static final String PATH_PODD_PLANT = "/ontologies/poddPlant.owl";

    /** Path to poddAnimal.owl */
    public static final String PATH_PODD_ANIMAL = "/ontologies/poddAnimal.owl";

    
    public static final String PODD_USER_BASE = "http://purl.org/podd/ns/poddUser#";

    public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    public static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";

    public static final String PODD_PLANT = "http://purl.org/podd/ns/poddPlant#";

    
    /**
     * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
     * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
     * using the version IRI of the original ontology, which must be unique.
     */
    public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";

    
    
    
    public static final URI OWL_VERSION_IRI = PoddRdfConstants.VALUE_FACTORY.createURI(OWL.NAMESPACE, "versionIRI");
    
    public static final URI PODDBASE_HAS_PUBLICATION_STATUS = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
    
    public static final URI PODDBASE_HAS_TOP_OBJECT = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "artifactHasTopObject");
    
    public static final URI PODDBASE_PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "Published");

    public static final URI PODDBASE_NOT_PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "NotPublished");
    
    public static final URI PODDBASE_FILE_REFERENCE_TYPE = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "PoddFileReference");
    
    public static final URI PODDBASE_CONTAINS = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "contains");
    
    public static final URI PODDBASE_WEIGHT = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "weight");

    /**
     * The OMV vocabulary defines a property for the current version of an ontology, so we are
     * reusing it here.
     */
    public static final URI OMV_CURRENT_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            "http://omv.ontoware.org/ontology#", "currentVersion");
    
    /**
     * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
     * ontology when linking from the ontology IRI.
     */
    public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "currentInferredVersion");
    
    /**
     * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
     * a particular versioned ontology.
     */
    public static final URI PODD_BASE_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "inferredVersion");
    
    /** Default value is urn:podd:default:artifactmanagementgraph:  */
    public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:artifactmanagementgraph:");
    
    /** Default value is urn:podd:default:schemamanagementgraph  */
    public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:schemamanagementgraph");

    /** Default value is urn:podd:default:usermanagementgraph:  */
    public static final URI DEF_USER_MANAGEMENT_GRAPH = VALUE_FACTORY
            .createURI("urn:podd:default:usermanagementgraph:");
    
}