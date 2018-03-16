package org.jenkinsciplugins.buildtree;


import java.util.Collection;
import java.util.Collections;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.TransientBuildActionFactory;

/**
 * This class is an entry point for the plugin.  It returns a BuildTree action, which
 * maps to an entry in the execution menu list of actions (view console output, etc.).
 * 
 * This instantiates the full tree action.
 */
@Extension
@SuppressWarnings("rawtypes")
public class BuildTreeActionFactory extends TransientBuildActionFactory {

	//----------------------------------------------------------------
	// Class methods
	
    @Override
    public Collection<? extends Action> createFor(AbstractBuild run) {
        return Collections.singleton(new BuildTree(run));
    }
}
