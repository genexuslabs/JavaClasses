package com.genexus.diagnostics;

import com.genexus.ModelContext;

public class GXDebugInfo
{
    final ModelContext context;
    final GXDebugManager.PgmKey key;
    GXDebugInfo parent;
    final int sId;

    GXDebugManager.GXDebugItem lastItem;
    final GXDebugManager.Stopwatch stopwatch;

    public GXDebugInfo(int sId, ModelContext context, GXDebugManager.PgmKey dbgKey)
    {
        this.sId = sId;
        this.context = context;
        this.key = dbgKey;
        lastItem = null;
        stopwatch = new GXDebugManager.Stopwatch();
    }

    public void trk(int lineNro)
    {
        updateTicks();
        lastItem = GXDebugManager.getInstance().push(this, lineNro, 0);
        stopwatch.restart();
    }

    public void trk(int lineNro, int lineNro2)
    {
        updateTicks();
        lastItem = GXDebugManager.getInstance().push(this, lineNro, lineNro2);
        stopwatch.restart();
    }

    public void onExit()
    {
        updateTicks();
        GXDebugManager.getInstance().onExit(this);
    }

    public void registerPgm(GXDebugInfo parentDbgInfo)
    {
        GXDebugManager.getInstance().pushPgm(this, parentDbgInfo != null ? parentDbgInfo.sId : GXDebugManager.PGM_INFO_NO_PARENT, key);
        if(parentDbgInfo != null)
        {
            parentDbgInfo.updateTicks();
            parentDbgInfo.lastItem = null;
            parentDbgInfo.stopwatch.restart();
        }
    }

    public void onCleanup()
    {
        updateTicks();
        GXDebugManager.getInstance().onCleanup(this);
    }

    private void updateTicks()
    {
        if(lastItem != null)
            lastItem.ticks = stopwatch.getElapsedMicroSecs();
    }
}
