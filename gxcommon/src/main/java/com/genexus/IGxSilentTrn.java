package com.genexus;
import com.genexus.internet.*;
import com.genexus.search.GXContentInfo;

public interface IGxSilentTrn
{
  public void initialize();
  public void LoadKey(Object [] obj);

  public void Save();
  public void Check();
  public void SetMode( String mode);
  public String GetMode();
  public int Errors();
  public MsgList GetMessages();
  public void ReloadFromSDT();
  public boolean Reindex();
  public GXContentInfo getContentInfo();
  public void getInsDefault();
  public void Load();
  public boolean Insert();
  public boolean Update();
  public boolean InsertOrUpdate();
  public void ForceCommitOnExit();
}

