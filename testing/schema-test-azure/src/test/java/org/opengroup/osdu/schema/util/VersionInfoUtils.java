package org.opengroup.osdu.schema.util;

import com.google.gson.Gson;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;

public class VersionInfoUtils {

  public VersionInfo getVersionInfoFromResponse(HttpResponse response) {
    String json = response.getBody();
    Gson gson = new Gson();
    return gson.fromJson(json, VersionInfo.class);
  }

  public class VersionInfo {

    public String groupId;
    public String artifactId;
    public String version;
    public String buildTime;
    public String branch;
    public String commitId;
    public String commitMessage;
  }
}
