/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.asgardio.tomcat.oidc.agent;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import io.asgardio.java.oidc.sdk.SSOAgentConstants;
import io.asgardio.java.oidc.sdk.exception.SSOAgentException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Logout Servlet is the class for handling OIDC logout requests
 * which is extended from the base class, {@link HttpServlet}.
 *
 *
 * @version     0.1.1
 * @since       0.1.1
 */
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        handleOIDCLogout(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        handleOIDCLogout(req, resp);
    }

    private void handleOIDCLogout(HttpServletRequest req, HttpServletResponse resp)
            throws SSOAgentException, IOException {

        HttpSession currentSession = req.getSession(false);
        LogoutRequest logoutRequest = getLogoutRequest(currentSession);

        logger.log(Level.INFO, "Invalidating the session in the client side upon RP-Initiated logout.");
        currentSession.invalidate();

        resp.sendRedirect(logoutRequest.toURI().toString());
    }

    private LogoutRequest getLogoutRequest(HttpSession currentSession) throws SSOAgentException {

        Properties properties = SSOAgentContextEventListener.getProperties();
        LogoutRequest logoutRequest;
        try {
            URI logoutEP = new URI(properties.getProperty(SSOAgentConstants.OIDC_LOGOUT_ENDPOINT));
            URI redirectionURI = new URI(properties.getProperty(SSOAgentConstants.POST_LOGOUT_REDIRECTION_URI));
            JWT jwtIdToken = JWTParser.parse((String) currentSession.getAttribute(SSOAgentConstants.ID_TOKEN));
            logoutRequest = new LogoutRequest(logoutEP, jwtIdToken, redirectionURI, null);

        } catch (URISyntaxException | ParseException e) {
            throw new SSOAgentException("Error while fetching logout URL.", e);
        }
        return logoutRequest;
    }
}