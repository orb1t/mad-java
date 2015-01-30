package uk.co.modularaudio.service.audioproviderregistry;

import uk.co.modularaudio.service.apprenderinggraph.AppRenderingGraph;

public interface AppRenderingIO
{
	public abstract void startRendering();

	public abstract boolean stopRendering();

	public abstract boolean isRendering();

	public abstract boolean testRendering( long testClientRunMillis );

	public abstract AppRenderingGraph getAppRenderingGraph();

	public abstract void destroy();

	public abstract void setShouldRecordPeriods( boolean should );

	public abstract long getCurrentUiFrameTime();
}