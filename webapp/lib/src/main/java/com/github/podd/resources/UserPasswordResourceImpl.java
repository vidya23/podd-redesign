/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.LocalVerifier;
import org.restlet.security.MapVerifier;
import org.restlet.security.Role;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;
import org.restlet.security.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * This resource handles User password change.
 * 
 * @author kutila
 */
public class UserPasswordResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Display the HTML page for password change
     */
    @Get(":html")
    public Representation getUserPasswordPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getUserPasswordHtml");
        
        final String requestedUserIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requesting change password of user: {}", requestedUserIdentifier);
        
        if(requestedUserIdentifier == null)
        {
            // no identifier specified.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // identify needed Action
        PoddAction action = PoddAction.OTHER_USER_EDIT;
        if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_EDIT;
        }
        
        this.checkAuthentication(action);
        
        // completed checking authorization
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editUserPwd.html.ftl");
        dataModel.put("pageTitle", "Edit Password");
        dataModel.put("authenticatedUserIdentifier", user.getIdentifier());
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
        
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        else
        {
            dataModel.put("requestedUser", poddUser);
            
            final Set<Role> roles = realm.findRoles(poddUser);
            dataModel.put("repositoryRoleList", roles);
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * Handle password change requests
     */
    @Post("rdf|rj|ttl")
    public Representation editPasswordRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("changePasswordRdf");
        
        final String changePwdUserIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requesting change password of user: {}", changePwdUserIdentifier);
        
        if(changePwdUserIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        // check authentication first
        PoddAction action = PoddAction.OTHER_USER_EDIT;
        if(user != null && changePwdUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_EDIT;
        }
        this.checkAuthentication(action);        
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        final RestletUtilUser changePwdUser = nextRealm.findUser(changePwdUserIdentifier);
        if (changePwdUser == null || !(changePwdUser instanceof PoddUser))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
        }
        final PoddUser poddUser = (PoddUser) changePwdUser;
        URI userUri = null;
        try
        {
            // - get input stream with RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model modifiedUserModel = Rio.parse(inputStream, "", inputFormat);
            
            userUri = this.changePassword(nextRealm, modifiedUserModel, poddUser, user.getIdentifier());
        }
        catch (IOException | OpenRDFException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
            
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(userUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PoddRdfConstants.VF.createLiteral(poddUser.getIdentifier()));
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }

    private URI changePassword(PoddSesameRealm nextRealm, Model model, PoddUser changePwdUser, String authenticatedUserIdentifier)
    {
        final String identifierInModel = model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString();
        
        // verify user identifier in Model is same as that from the request
        if (!changePwdUser.getIdentifier().equals(identifierInModel))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Problem with input: user identifiers don't match");
        }

        // changing own password, verify old password.
        //TODO: is there a better way of doing this?
        if (changePwdUser.getIdentifier().equals(authenticatedUserIdentifier))
        {
            final String oldPassword = model.filter(null, PoddRdfConstants.PODD_USER_OLDSECRET, null).objectString();
            
            final Verifier verifier = nextRealm.getVerifier();
            if (verifier == null)
            {
                this.log.warn("Could not access Verifier to check old password");
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to access Verifier");
            }
        
            char[] localSecret = null;
            if (verifier instanceof MapVerifier)
            {
                localSecret = ((MapVerifier)verifier).getLocalSecret(identifierInModel);
            }
            else if (verifier instanceof LocalVerifier)
            {
                localSecret = ((LocalVerifier)verifier).getLocalSecret(identifierInModel);
            }
            else
            {
                this.log.warn("Could not access Verifier to check old password");
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to access Verifier");
            }
            
            if (!SecretVerifier.compare(localSecret, oldPassword.toCharArray()))
            {
                throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Old password is invalid.");
            }
        }
        
        // update sesame Realm with new password
        final String newPassword = model.filter(null, SesameRealmConstants.OAS_USERSECRET, null).objectString();
        this.log.info("[DEBUG] new password is [{}]", newPassword);
        changePwdUser.setSecret(newPassword.toCharArray());
        
        return nextRealm.updateUser(changePwdUser);
    }
    
}