package fr.pagesjaunes.tools.ramlcodegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bundle {
	
	private static final Logger logger = LoggerFactory.getLogger(Bundle.class);
	
	private String root;
	
	private List<BundleItem> items=new ArrayList<BundleItem>();
	
	public static class BundleItem {
		
		private String path, content;
		
		private BundleItem(String aPath, String aContent) {
			path=aPath;
			content=aContent;
		}
		
		public String getPath() {
			return path;
		}
		
		public String getContent() {
			return content;
		}
	}
	
	public Bundle(String aRoot) {
		root=aRoot;
	}
	
	public String getRoot() {
		return root;
	}
	
	public void addItem(String aPath, String aContent) {
		items.add(new BundleItem(aPath, aContent));
	}
	
	public void addItem(String aPath) {
		addItem(aPath, null);
	}
	
	public void addItems(String... paths) {
		for(int i=0;i<paths.length;i++) {
			addItem(paths[0]);
		}
	}
	
	public List<BundleItem> getItems() {
		return Arrays.asList(items.toArray(new BundleItem[items.size()]));
	}
	
	public boolean create() {
		boolean oError=false;
		for(BundleItem oItem:items) {
			File oFile=new File(root,oItem.getPath());
			if(oItem.getContent()==null) {
				try {
					oFile.mkdirs();
				} catch (SecurityException oE) {
					logger.error("Fail to create directory (security) : "+oFile.getPath());
					logger.debug("Exception while creating directory :"+oFile.getPath(), oE);
					oError=true;
				}
			} else {
				try {
					oFile.getParentFile().mkdirs();
					oFile.createNewFile();
					try(BufferedWriter oWriter=new BufferedWriter(new FileWriter(oFile));) {
						oWriter.write(oItem.getContent());
					}
				} catch(IOException oE) {
					logger.error("Fail to create file : "+oFile.getPath());
					logger.debug("Exception while creating file :"+oFile.getPath(), oE);
					oError=true;
				}
			}
		}
		return oError;
	}
	
	@Override
	public String toString() {
		StringBuffer oBuffer=new StringBuffer("Bundle: ").append(getRoot()).append("\n");
		for(BundleItem oItem:getItems()) {
			oBuffer.append(oItem.getPath()).append("\n--\n");
			if(oItem.getContent()!=null) {
				oBuffer.append(oItem.getContent()).append("\n");
			}
		}
		return oBuffer.toString();
	}

}
