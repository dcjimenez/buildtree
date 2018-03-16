package org.jenkinsciplugins.buildtree;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import hudson.model.AbstractBuild;

/**
 * This class implements the log parsing of the execution tree.
 */
@SuppressWarnings("rawtypes")
public class BuildTreeRenderer {
	
	// ----------------------------------------------------------------
	// Class properties
	
	// Logger instance
	private static final Logger LOG = Logger.getLogger(BuildTreeRenderer.class.getName());
	
	// -------------------------------------------------------------------------
	// Class methods

	/**
	 * Renders the full execution tree as an HTML tree, ready to be used
	 * at the jelly presentation layer.
	 * @param build Current jenkins build
	 * @param sb Buffer for the HTML rendering of the tree
	 * @return Proper HTML rendering of the execution tree.
	 */
	public BuildBean renderFullTree(
			AbstractBuild build, 
			String jenkinsURL,
			StringBuilder sb) {
		return renderTree(true, build, jenkinsURL, sb);
	}
	
	/**
	 * Renders the partial execution tree as an HTML tree, ready to be used
	 * at the jelly presentation layer.
	 * @param build Current jenkins build
	 * @param sb Buffer for the HTML rendering of the tree
	 * @return Root of the execution tree.
	 */
	public BuildBean renderPartialTree(
			AbstractBuild build, 
			String jenkinsURL,
			StringBuilder sb) {
		return renderTree(false, build, jenkinsURL, sb);
	}
	
	// Renders a full or partial execution tree in HTML
	private BuildBean renderTree(
			boolean absoluteRoot, 
			AbstractBuild build, 
			String jenkinsURL,
			StringBuilder sb) {	
		BuildTreeHelper helper = new BuildTreeHelper();
		BuildBean root = null;
		try {
			root = helper.executionTree(build, absoluteRoot);
		}
		catch (Exception e) {
			LOG.log(Priority.ERROR, e.getMessage(), e);
		}
		// Render the tree into an <ul><li>... structure
		sb.append("<ul>");
		render(sb, root, jenkinsURL, new LinkedList<BuildBean>());
		sb.append("</ul>");
		return root;
	}
	
	// Composes Jenkins URL with another path
	private String composeJenkinsURL(String jenkinsURL, String path) {
		StringBuilder sb = new StringBuilder();
		if (jenkinsURL == null) {
			sb.append(path);
		}
		else {
			sb.append(jenkinsURL);
			if (!jenkinsURL.endsWith("/") && !path.startsWith("/")) {
				sb.append("/");
			}
			sb.append(path);
		}
		return sb.toString();
	}	
	
	/**
	 * Returns the plugin version (as declared in buildtree.properties)
	 * @return Version identifier
	 */
	public String getVersion() {
		String ret = "";
		try (InputStream is = BuildTreeRenderer.class.getClassLoader().
				getResourceAsStream("buildtree.properties")) {
			Properties p = new Properties();
			p.load(is);
			ret = p.getProperty("buildtree.version");
		}
		catch(Exception e) {
			LOG.log(Priority.ERROR, e.getMessage(), e);
		}
		return ret;
	}
	
	// Hex value of a character
    private char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    // Is a certain character unsafe for a URL?
    private boolean isUnsafe(char ch) {
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
	
	/**
	 * Composes the address for the console output of a certain job execution.
	 * @param jenkinsURL Jenkins master URL
	 * @param jobName Name of the job.
	 * @param buildNumber Build number of the job.
	 * @return URL for the console output of the execution.
	 */
	public String getConsoleURL(String jenkinsURL, String jobName, Integer buildNumber) {
		return composeJenkinsURL(jenkinsURL, 
				"job/" + encode(jobName) + "/" + buildNumber + "/console");
	}
	
	// Shamelessly copied from stackoverflow in order to get a proper percent-encoding
	public String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }
	
	// Recursively render this node and its children
	public void render(
				StringBuilder upperSB, 
				BuildBean build, 
				String jenkinsURL,
				List<BuildBean> alreadyRendered) {
		// Takes off some odd node repetitions
		if (!alreadyRendered.contains(build)) {
			alreadyRendered.add(build);
			StringBuilder sb = new StringBuilder();
			// We try to show only the information of the finished builds
			try {
				// Selected if it is the original job from which the view was created
				// URL of the icon to show, depending on the build result
				// Link to the log
				String linkToLog = getConsoleURL(jenkinsURL, 
						build.getJobName(), build.getBuildNumber());
				
				// Use the default Jenkins ball image
				String iconURL;
				if (build.getResult() == null) {
					// clock image
					iconURL = composeJenkinsURL(jenkinsURL, 
							"images/16x16/clock_anime.gif");
				}
				else {
					// definitive result image
					if (!build.getBuild().isBuilding()) { 
						iconURL = composeJenkinsURL(jenkinsURL, 
								"images/16x16/" + build.getResult().color.getImage());
					}
					else {
						// Twinkling image
						iconURL = composeJenkinsURL(jenkinsURL, 
								"images/16x16/" + build.getResult().color.anime().getImage());
					}
				}
				sb.append("<li data-jstree='{\"logURL\":\"" 
						+ linkToLog + "\", \"icon\":\"" + iconURL + "\"}'>");
				// Is it a post build?
				if (build.isPostBuild()) {
					sb.append("[ <span class=\"POSTBUILD\">POSTBUILD</span> ] ");
				}
				// In which node it has been built
				if (build.getNodeName() != null && build.getNodeName().trim().length() != 0) {
					sb.append("[ <span class=\"NODE\">" + build.getNodeName() + "</span> ] ");
				}
				// Is it the originally clicked build?
				if (build.getOriginal()) {
					sb.append("<span style=\"font-weight: bold\">**** </span>");
				}		 
				sb.append(build.getJobName() + " #" + build.getBuildNumber() + 
						" - <span class=\"duration\">" + build.getDurationString() + "</span>");
				
				List<BuildBean> collectedChildren = new LinkedList<>();
				
				collectedChildren.addAll(build.getChildren());
				collectedChildren.addAll(build.getPostBuilds());
				
				if (!collectedChildren.isEmpty()) {
					sb.append("<ul>");
					Collections.sort(collectedChildren);
					for (BuildBean child: collectedChildren) {
						render(sb, child, jenkinsURL, alreadyRendered);
					}
					sb.append("</ul>");					
				}
				
				sb.append("</li>");
				upperSB.append(sb.toString());
			}
			catch(Exception e) {
				LOG.error("Error building the tree: "
						+ build==null?"null":build.toString());
			}
		}
	}
}