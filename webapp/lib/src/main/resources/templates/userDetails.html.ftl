<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="authenticatedUsername" type="java.lang.String" -->
<#-- @ftlvariable name="requestedUser" type="java.util.Map" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->

<div id="title_pane">
    <h3>User Details</h3>
</div>

<div id="content_pane">
    <#if isAdmin?? && isAdmin>
        <#include "admin_aux.html.ftl"/>

    <div id="main">
    </#if>


	<p>
    <h4 class="errorMsg">${errorMessage!""}</h4>

    <#if requestedUser??>
        <div id="admin_left_pane" class="fieldset_without_border">
            <div class="legend_no_indent">Account Details</div>
            <ol>
                <li><span class="bold">User Name: </span>${requestedUser.identifier!""}</li>
                <li><span class="bold">Email Address: </span>${requestedUser.email!""}</li>
                <li><span class="bold">Status: </span>${requestedUser.userStatus.label!""}</li>
                
				<#if repositoryRoleList??>
	                <li><span class="bold">Roles: </span></li>
	                <#list repositoryRoleList as role>
				 		<li><span class="bold">&nbsp;&nbsp;&nbsp;&nbsp;</span>${role.name!""}</li>
					</#list>
                </#if>
                
                
            </ol>
        </div>

        <div id="admin_right_pane" class="fieldset_without_border">
            <div class="legend_no_indent">Personal Details</div>
            <ol>
                <li><span class="bold">Title: </span>${requestedUser.title!""}</li>
                <li><span class="bold">First Name: </span>${requestedUser.firstName!""}</li>
                <li><span class="bold">Last Name: </span>${requestedUser.lastName!""}</li>
                <li><span class="bold">Organisation/Institution: </span>${requestedUser.organization!""}</li>
                <li><span class="bold">Professional Position: </span>${requestedUser.position!""}</li>
                <li><span class="bold">Phone Number: </span>${requestedUser.phone!""}</li>
                <li><span class="bold">Mailing Address: </span>${requestedUser.address!""}</li>
                <li><span class="bold">URL: </span><a href=${requestedUser.homePage!""}>${requestedUser.homePage!""}</a></li>
                <li><span class="bold">ORCID ID: </span>${requestedUser.orcid!""}</li>
            </ol>
        </div>


        <div id="buttonwrapper">
            <#if isAdmin?? && isAdmin>
                <a href="${baseUrl}/user/edit/${requestedUser.identifier!"unknown-username"}">Edit User</a>
                <a href="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}/editpwd">Change Password</a>
                <#if authenticatedUsername != requestedUser.identifier>
                    <a href="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}/delete">Delete User</a>
                </#if>
                </div>
                </div> <!-- main -->
            <#else>
                <a href="${baseUrl}/user/edit/${requestedUser.identifier!"unknown-username"}">Edit User</a>
                <a href="${baseUrl}/user/${requestedUser.identifier!"unknown-username"}/editpwd">Change Password</a>
                </div>
            </#if>
    </#if>
</div>	<!-- content pane -->