/*
 * Cloud Foundry 2012.02.03 Beta
 * Copyright (c) [2009-2012] VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product includes a number of subcomponents with
 * separate copyright notices and license terms. Your use of these
 * subcomponents is subject to the terms and conditions of the
 * subcomponent's license, as noted in the LICENSE file.
 */
/* obtained from: https://raw.github.com/cloudfoundry/uaa/master/common/src/main/java/org/cloudfoundry/identity/uaa
/oauth/RemoteTokenServices.java
 * modified to work with ndg_oauth_server */
package org.example;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Dave Syer
 * @author W. van Engen
 */
public class RemoteTokenServices implements ResourceServerTokenServices {

  protected final Log logger = LogFactory.getLog(getClass());

  private RestOperations restTemplate = new RestTemplate();

  private String checkTokenEndpointUrl;

  private String checkTokenEndpointUsername = null;

  private String checkTokenEndpointPassword = null;

  private String scope = "";

  private String accessToken = "";

  public void setRestTemplate(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }

  public void setCheckTokenEndpointUrl(String checkTokenEndpointUrl) {
    this.checkTokenEndpointUrl = checkTokenEndpointUrl;
  }

  public void setCheckTokenEndpointUsername(String username) {
    this.checkTokenEndpointUsername = username;
  }

  public void setCheckTokenEndpointPassword(String password) {
    this.checkTokenEndpointPassword = password;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
    logger.info("checktokenendpointurl: " + checkTokenEndpointUrl);
    System.out.println("checktokenendpointurl: " + checkTokenEndpointUrl);
    logger.debug("accessToken: " + accessToken);
    System.out.println("accessToken: " + accessToken);

    this.accessToken = accessToken;

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
    formData.add("access_token", accessToken);
    if (scope != null) {
      formData.add("scope", scope);
    }
    HttpHeaders headers = new HttpHeaders();
    if (checkTokenEndpointUsername != null && checkTokenEndpointPassword != null) {
      setBasicAuth(headers, checkTokenEndpointUsername, checkTokenEndpointPassword);
    }

    // Map<String, Object> map = postForMap(checkTokenEndpointUrl, formData, headers);
    Map<String, Object> map = getForMap(checkTokenEndpointUrl);

    if (map.containsKey("error")) {
      logger.debug("check_token returned error: " + map.get("error"));
      throw new InvalidTokenException(accessToken);
    }

    List<String> scopes = null;
    if (scope != null) {
      scopes = Arrays.asList(scope.split("\\s*,\\s*"));
    }
    // TODO proper client id from certificate dn
    DefaultAuthorizationRequest clientAuthentication = new DefaultAuthorizationRequest(null, scopes);

    String username = ""; // empty string for authenticated user but identity not known
    if (map.containsKey("user_name")) {
      username = (String) map.get("user_name");
    }
    Authentication userAuthentication = new UsernamePasswordAuthenticationToken(username, null, null);

    clientAuthentication.setApproved(true);
    return new OAuth2Authentication(clientAuthentication, userAuthentication);
  }

  //@Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    throw new UnsupportedOperationException("Not supported: read access token");
  }

  private Map<String, Object> getForMap(String path) {
    HttpHeaders headers = new HttpHeaders();
    ;

    if (headers.getContentType() == null) {
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    Map map = null;

    headers.set("Authorization", String.format("Bearer %s", this.accessToken));
    HttpEntity<String> entity = new HttpEntity<String>(headers);

    try {

      map = restTemplate.exchange(
          path,
          HttpMethod.GET,
          entity,
          Map.class
      ).getBody();
    } catch (Throwable t) {
      System.out.println("t message: " + t.getMessage());
      System.out.println("t cause: " + t.getCause());
      t.printStackTrace();
    }
    try {
      System.out.println("map: " + new ObjectMapper().writeValueAsString(map));
    } catch (IOException e) {
      e.printStackTrace();
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) map;
    return result;
  }

  private void setBasicAuth(HttpHeaders headers, String username, String password) {
    String authz = username + ":" + password;
    byte[] authzencoded = Base64.encodeBase64(authz.getBytes(Charset.forName("US-ASCII")));
    headers.set("Authorization", "Basic " + new String(authzencoded));
  }

}
