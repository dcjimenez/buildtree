package org.jenkinsciplugins.buildtree;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.Action;

/**
 * This class implements the partial tree action, providing an interface
 * for the functionality, icon, etc.
 */
@SuppressWarnings("rawtypes")
public class PartialBuildTree implements Action {
	
	//----------------------------------------------------------------
	// Class properties
	
	// Logger instance
	private static final Logger LOG = Logger.getLogger(PartialBuildTree.class.getName());
	// Reference to the build object
	private AbstractBuild build = null;
	// Log parse time
	private long logParseTime = 0;
	// Jenkins base URL
	private String jenkinsURL;
	// Is the job still building?
	private boolean stillBuilding = true;
	
	//----------------------------------------------------------------
	// Class methods

	
	/**
	 * Creates a partial build tree action with a reference to the execution whose
	 * build tree we want to build.
	 * @param run Reference to the project execution.
	 */
	public PartialBuildTree(AbstractBuild run) {
		this.build = run;
		try {
			// Get the base Jenkins URL
			EnvVars envVars = build.getEnvironment(null);
			jenkinsURL = envVars.get("JENKINS_URL");
		}
		catch(Exception e) {
			LOG.log(Priority.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public String getDisplayName() {
		return "Partial Build Tree";
	}
	
	/**
	 * @return Time invested in parsing the log, in milliseconds.
	 */
	public long getLogParseTime() {
		return logParseTime;
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getIconFileName()
	 */
	@Override
	public String getIconFileName() {
		return "plugin/buildtree/images/partial_buildtree.png";
	}

	/* (non-Javadoc)
	 * @see hudson.model.Action#getUrlName()
	 */
	@Override
	public String getUrlName() {
		return "PartialBuildTree";
	}
	
	/**
	 * @return Build object whose log has been parsed
	 */
	public AbstractBuild getBuild() {
		return build;
	}
	
	/**
	 * Returns the plugin version (as declared in buildtree.properties)
	 * @return Version identifier
	 */
	public String getVersion() {
		return new BuildTreeRenderer().getVersion();
	}

	/**
	 * Renders the execution tree as an HTML tree, ready to be used
	 * at the jelly presentation layer.
	 * @return Proper HTML rendering of the execution tree.
	 */
	public String renderTree() {
		BuildTreeRenderer helper = new BuildTreeRenderer();
		// Parse the log, stopwatching
		Date startParse = new Date();
		StringBuilder tree = new StringBuilder();
		BuildBean root = helper.renderPartialTree(build, jenkinsURL, tree);
		Date endParse = new Date();
		logParseTime = endParse.getTime() - startParse.getTime();
		LOG.debug(String.format(
			"BuildTree:: %s %d :: parse time -> %d msec",
			build.getDisplayName(), 
			build.getNumber(), 
			logParseTime));
		stillBuilding = root.getBuild().isBuilding();
		return tree.toString();
	}

	/**
	 * @return the stillBuilding
	 */
	public boolean isStillBuilding() {
		return stillBuilding;
	}	

}
