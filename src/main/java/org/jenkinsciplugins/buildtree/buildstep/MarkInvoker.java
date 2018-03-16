package org.jenkinsciplugins.buildtree.buildstep;

import java.io.IOException;

import org.jenkinsci.plugins.commons.JobRootFinder;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

/**
 * Este paso de construcción deja una 'marca' que indica al job padre del
 * actual que ha invocado al job actual.  Se utiliza para distinguir aquellos
 * que se han invocado como post-build.
 */
public class MarkInvoker extends Builder {
	
	//---------------------------------------------------------------
	// Métodos de la clase
	
	@DataBoundConstructor
    public MarkInvoker() {
    }
	
	 /* (non-Javadoc)
	 * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		listener.getLogger().println("====================================================");
		listener.getLogger().println("Plugin de árbol de ejecución");
		// Remontarse al padre y añadirle un parámetro 
		AbstractBuild ancestor = new JobRootFinder().getParentBuild(build);
		if (ancestor != null && !ancestor.equals(build)) {
			listener.getLogger().println("Relacionando esta ejecución con " + ancestor + " ...");
			PostBuildAction action = new PostBuildAction(build);
			ancestor.addAction(action);
			ancestor.save();
		}
		else {
			listener.getLogger().println("No se marca la ejecución ya que el ancestro es " 
					+ ancestor);
		}
		listener.getLogger().println("====================================================");
		return true;
	}
	
	@Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {
 
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
 
        @Override
        public String getDisplayName() {
            return "Marca la relación con el job invocante";
        }
    }
}
