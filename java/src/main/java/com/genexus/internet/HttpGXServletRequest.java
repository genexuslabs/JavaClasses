package com.genexus.internet;

import com.genexus.servlet.http.IHttpServletRequest;

public class HttpGXServletRequest {
  private IHttpServletRequest request = null;

  public HttpGXServletRequest() {
  }

  public void setHttpServletRequest(IHttpServletRequest request)
  {
    this.request = request;
  }

  public IHttpServletRequest getHttpServletRequest()
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
