package org.ametro.catalog.storage;

import java.io.File;
import java.util.ArrayList;

import org.ametro.Constants;
import org.ametro.catalog.Catalog;

import android.os.AsyncTask;
import android.util.Log;

public class CatalogStorage implements ICatalogBuilderListener {

	public static final int CATALOG_LOCAL = 0;
	public static final int CATALOG_IMPORT = 1;
	public static final int CATALOG_ONLINE = 2;
	
	public CatalogStorage(File localStorage, File localPath, File importStorage, File importPath, File onlineStorage, String onlineUrl){
		this.mLocalStorage = localStorage;
		this.mLocalPath = localPath;
		this.mImportStorage = importStorage;
		this.mImportPath = importPath;
		this.mOnlineStorage = onlineStorage;
		this.mOnlineUrl = onlineUrl;
		
		this.mOnlineCatalogBuilder = new CatalogBuilder();
		this.mOnlineCatalogBuilder.addOnCatalogBuilderEvents(this);
		
		this.mLocalCatalogBuilder = new CatalogBuilder();
		this.mLocalCatalogBuilder.addOnCatalogBuilderEvents(this);
		
		this.mImportCatalogBuilder = new CatalogBuilder();
		this.mImportCatalogBuilder.addOnCatalogBuilderEvents(this);
		
	}
	
	public void addCatalogChangedListener(ICatalogStorageListener listener){
		mCatalogListeners.add(listener);
	}
	
	public void removeCatalogChangedListener(ICatalogStorageListener listener){
		mCatalogListeners.remove(listener);
	}

	public void fireCatalogChanged(int catalogId, Catalog catalog){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogLoaded(catalogId, catalog);
		}
	}

	public void fireCatalogOperationFailed(int catalogId, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogOperationFailed(catalogId, message);
		}
	}

	public void fireCatalogOperationProgress(int catalogId, int progress, int total, String message){
		for(ICatalogStorageListener listener : mCatalogListeners){
			listener.onCatalogOperationProgress(catalogId, progress, total, message);
		}
	}
	
	public Catalog getLocalCatalog() {
		return mLocalCatalog;
	}	
	
	public Catalog getOnlineCatalog() {
		return mOnlineCatalog;
	}
	
	public Catalog getImportCatalog() {
		return mImportCatalog;
	}	
	
	public void requestLocalCatalog(boolean refresh)
	{
		if(mLoadLocalCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadLocalCatalogTask==null){
					mLoadLocalCatalogTask = new LocalCatalogLoadTask();
					mLoadLocalCatalogTask.execute(refresh);
				}
			}
		}		
	}
	
	public void requestImportCatalog(boolean refresh)
	{
		if(mLoadImportCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadImportCatalogTask==null){
					mLoadImportCatalogTask = new ImportCatalogLoadTask();
					mLoadImportCatalogTask.execute(refresh);
				}
			}
		}		
	}
	
	public void requestOnlineCatalog(boolean refresh)
	{
		if(mLoadOnlineCatalogTask==null){
			synchronized (mMutex) {
				if(mLoadOnlineCatalogTask==null){
					mLoadOnlineCatalogTask = new OnlineCatalogLoadTask();
					mLoadOnlineCatalogTask.execute(refresh);
				}
			}
		}	
	}	
	
	void cleanupLoadLocalCatalogTask(){
		synchronized (mMutex) {
			mLoadLocalCatalogTask = null;
		}
	}
	
	
	void cleanupLoadImportCatalogTask(){
		synchronized (mMutex) {
			mLoadImportCatalogTask = null;
		}
	}	
	
	void cleanupLoadOnlineCatalogTask(){
		synchronized (mMutex) {
			mLoadOnlineCatalogTask = null;
		}
	}		
	
	/*package*/ File mLocalStorage;
	/*package*/ File mLocalPath;
	/*package*/ File mImportStorage;
	/*package*/ File mImportPath;
	/*package*/ File mOnlineStorage;
	/*package*/ String mOnlineUrl;
	
	/*package*/ Object mMutex = new Object();
	/*package*/ LocalCatalogLoadTask mLoadLocalCatalogTask;
	/*package*/ ImportCatalogLoadTask mLoadImportCatalogTask;
	/*package*/ OnlineCatalogLoadTask mLoadOnlineCatalogTask;
	
	/*package*/ Catalog mLocalCatalog;
	/*package*/ Catalog mOnlineCatalog;
	/*package*/ Catalog mImportCatalog;

	/*package*/ CatalogBuilder mLocalCatalogBuilder;
	/*package*/ CatalogBuilder mOnlineCatalogBuilder;
	/*package*/ CatalogBuilder mImportCatalogBuilder;
	
	/*package*/ ArrayList<ICatalogStorageListener> mCatalogListeners = new ArrayList<ICatalogStorageListener>();

	private class OnlineCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mOnlineCatalogBuilder.downloadCatalog(mOnlineStorage, mOnlineUrl, params[0]);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested online catalog");
			}
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Online catalog request response: " + result!=null ? result.toString() : "null");
			}
			mOnlineCatalog = result;
			cleanupLoadOnlineCatalogTask();
			fireCatalogChanged(CATALOG_ONLINE, result);
			super.onPostExecute(result);
		}
		
		protected void onCancelled() {
			cleanupLoadImportCatalogTask();
			super.onCancelled();
		}
	}	
	
	private class LocalCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mLocalCatalogBuilder.loadCatalog(mLocalStorage, mLocalPath, params[0], CatalogBuilder.FILE_TYPE_AMETRO);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested local catalog");
			}
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Local catalog request response: " + result!=null ? result.toString() : "null");
			}
			mLocalCatalog = result;
			cleanupLoadLocalCatalogTask();
			fireCatalogChanged(CATALOG_LOCAL, result);
			super.onPostExecute(result);
		}
		
		protected void onCancelled() {
			cleanupLoadLocalCatalogTask();
			super.onCancelled();
		}
	}	
	
	private class ImportCatalogLoadTask extends AsyncTask<Boolean, Void, Catalog> {
		protected Catalog doInBackground(Boolean... params) {
			return mImportCatalogBuilder.loadCatalog(mImportStorage, mImportPath, params[0], CatalogBuilder.FILE_TYPE_PMETRO);
		}
		
		protected void onPreExecute() {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Requested import catalog");
			}
			super.onPreExecute();
		}
		
		protected void onPostExecute(Catalog result) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
				Log.d(Constants.LOG_TAG_MAIN, "Import catalog request response: " + result!=null ? result.toString() : "null");
			}
			mImportCatalog = result;
			cleanupLoadImportCatalogTask();
			fireCatalogChanged(CATALOG_IMPORT, result);
			super.onPostExecute(result);
		}
		
		protected void onCancelled() {
			cleanupLoadImportCatalogTask();
			super.onCancelled();
		}
	}

	private int getCatalogId(CatalogBuilder source) {
		if(source == mImportCatalogBuilder){
			return CATALOG_IMPORT;
		}else if(source == mOnlineCatalogBuilder){
			return CATALOG_ONLINE;
		}else if(source == mLocalCatalogBuilder){
			return CATALOG_LOCAL;
		}
		throw new RuntimeException("Unknown CatalogBuilder instance");
	}
	
	public void onCatalogBuilderOperationFailed(CatalogBuilder source, String message) {
		fireCatalogOperationFailed(getCatalogId(source), message);
	}

	public void onCatalogBuilderOperationProgress(CatalogBuilder source, int progress, int total, String message) {
		fireCatalogOperationProgress(getCatalogId(source), progress, total, message);
	}


}