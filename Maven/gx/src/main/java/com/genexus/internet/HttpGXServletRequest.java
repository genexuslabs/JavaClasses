package com.genexus.internet;

import javax.servlet.http.*;

public class HttpGXServletRequest {
  private HttpServletRequest request = null;

  public HttpGXServletRequest() {
  }

  public void setHttpServletRequest(HttpServletRequest request)
  {
    this.request = request;
  }

  public HttpServletRequest getHttpServletRequest()
  {
    return request;
  }

  public String getServerName()
  {
  return request.getServerName();
  }

  public int getServerPort()
  {
    return request.getServerPort();
  }

  public String getRequestURI()
  {
    return request.getRequestURI();
  }

  public String getServletPath()
  {
    return request.getServletPath();
  }
}
