/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Set;

import org.kohsuke.MetaInfServices;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.PoddDataRepositoryFactory;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A factory to build {@link PoddDataRepository} instances from a {@link Model}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
@MetaInfServices(PoddDataRepositoryFactory.class)
public class SSHFileRepositoryImplFactory implements PoddDataRepositoryFactory
{
    @Override
    public boolean canCreate(Set<URI> types)
    {
        return types.contains(PoddRdfConstants.PODD_SSH_FILE_REPOSITORY);
    }
    
    @Override
    public PoddDataRepository<?> createDataRepository(Model statements) throws DataRepositoryException
    {
        if(statements.contains(null, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY))
        {
            return new SSHFileRepositoryImpl(statements);
        }
        
        throw new FileRepositoryIncompleteException(statements,
                "Could not create SSH file repository from this configuration");
    }
    
    @Override
    public String getKey()
    {
        return "datarepositoryfactory:" + PoddRdfConstants.PODD_SSH_FILE_REPOSITORY.stringValue();
    }
}