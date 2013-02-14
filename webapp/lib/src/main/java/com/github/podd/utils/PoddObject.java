/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * 
 * @author kutila
 * 
 */
public class PoddObject
{
    /** URI to identify the object */
    private URI uri;
    
    /** Label for this object */
    private String label;
    
    /** The parent of this object */
    private URI directParent = null;
    
    private String relationshipFromDirectParent = null;
    
    /** An optional description about the object */
    private String description;
    
    /**
     * Constructor
     * 
     * @param uri
     */
    public PoddObject(final URI uri)
    {
        this.uri = uri;
    }
    
    public URI getUri()
    {
        return this.uri;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    public void setLabel(final String label)
    {
        this.label = label;
    }
    
    /**
     * @deprecated PoddObject should only have URI and optional label and description
     */
    public URI getDirectParent()
    {
        return this.directParent;
    }
    
    /**
     * @deprecated PoddObject should only have URI and optional label and description
     */
    public void setDirectParent(final URI container)
    {
        this.directParent = container;
    }
    
    /**
     * @deprecated PoddObject should only have URI and optional label and description
     */
    public boolean hasDirectParent()
    {
        return (this.directParent != null);
    }
    
    /**
     * @deprecated PoddObject should only have URI and optional label and description
     */
    public void setRelationshipFromDirectParent(final String relationshipFromDirectParent)
    {
        this.relationshipFromDirectParent = relationshipFromDirectParent;
    }
    
    /**
     * @deprecated PoddObject should only have URI and optional label and description
     */
    public String getRelationshipFromDirectParent()
    {
        return this.relationshipFromDirectParent;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    
}