package com.github.podd.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with {@link InferredOWLOntologyID}
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtils
{
    private static final Logger log = LoggerFactory.getLogger(OntologyUtils.class);
    
    private OntologyUtils()
    {
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link Model}, or creating a new Model if the given model is
     * null.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     * 
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param result
     *            The Model to contain the resulting statements, or null to have one created
     *            internally
     * @return A model containing the RDF statements about the given ontologies.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static Model ontologyIDsToModel(Collection<InferredOWLOntologyID> input, Model result)
    {
        Model results = result;
        
        if(results == null)
        {
            results = new LinkedHashModel();
        }
        
        for(InferredOWLOntologyID nextOntology : input)
        {
            results.addAll(nextOntology.toRDF());
        }
        
        return results;
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link RDFHandler}.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     * 
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param handler
     *            The handler for handling the RDF statements.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static void ontologyIDsToHandler(Collection<InferredOWLOntologyID> input, RDFHandler handler)
        throws RDFHandlerException
    {
        for(InferredOWLOntologyID nextOntology : input)
        {
            for(Statement nextStatement : nextOntology.toRDF())
            {
                handler.handleStatement(nextStatement);
            }
        }
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link Model}.
     * 
     * @param input
     *            The input model containing RDF statements.
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the model.
     */
    public static List<InferredOWLOntologyID> modelToOntologyIDs(Model input)
    {
        List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        Model typedOntologies = input.filter(null, RDF.TYPE, OWL.ONTOLOGY);
        
        for(Statement nextTypeStatement : typedOntologies)
        {
            if(nextTypeStatement.getSubject() instanceof URI)
            {
                Model versions = input.filter((URI)nextTypeStatement.getSubject(), OWL.VERSIONIRI, null);
                
                for(Statement nextVersion : versions)
                {
                    if(nextVersion.getObject() instanceof URI)
                    {
                        Model inferredOntologies =
                                input.filter((URI)nextVersion.getObject(), PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                                        null);
                        
                        if(inferredOntologies.isEmpty())
                        {
                            // If there were no poddBase#inferredVersion statements, backup by
                            // trying to infer the versions using owl:imports
                            Model importsOntologies = input.filter(null, OWL.IMPORTS, (URI)nextVersion.getObject());
                            
                            if(importsOntologies.isEmpty())
                            {
                                results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                        (URI)nextVersion.getObject(), null));
                            }
                            else
                            {
                                for(Statement nextImportOntology : importsOntologies)
                                {
                                    if(nextImportOntology.getSubject() instanceof URI)
                                    {
                                        results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                                (URI)nextVersion.getObject(), (URI)nextImportOntology.getSubject()));
                                    }
                                    else
                                    {
                                        log.error("Found a non-URI import statement: {}", nextImportOntology);
                                    }
                                    
                                }
                            }
                        }
                        else
                        {
                            for(Statement nextInferredOntology : inferredOntologies)
                            {
                                if(nextInferredOntology.getObject() instanceof URI)
                                {
                                    results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                            (URI)nextVersion.getObject(), (URI)nextInferredOntology.getObject()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link String}.
     * 
     * @param string
     *            The input string containing RDF statements.
     * @param format
     *            The format of RDF statements in the string  
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the string.
     * @throws OpenRDFException
     * @throws IOException
     */
    public static Collection<InferredOWLOntologyID> stringToOntologyID(String string, RDFFormat format)
        throws OpenRDFException, IOException
    {
        Model model = new LinkedHashModel();
        RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(new StatementCollector(model));
        parser.parse(new StringReader(string), "");
        
        return OntologyUtils.modelToOntologyIDs(model);
    }
}
