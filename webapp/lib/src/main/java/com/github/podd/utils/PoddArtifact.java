/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * Simple class to contain details about a Podd Artifact (i.e. Projects at present).
 * 
 * @author kutila
 *
 */
public class PoddArtifact extends PoddObject
{
    
    private String description;
    private String leadInstitution;
    
    /**
     * Constructor
     * 
     * @param uri
     */
    public PoddArtifact(URI uri)
    {
        super(uri);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLeadInstitution()
    {
        return leadInstitution;
    }

    public void setLeadInstitution(String leadInstitution)
    {
        this.leadInstitution = leadInstitution;
    }


    
    
}