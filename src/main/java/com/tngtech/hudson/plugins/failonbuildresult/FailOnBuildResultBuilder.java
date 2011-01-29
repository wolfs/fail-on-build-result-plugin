package com.tngtech.hudson.plugins.failonbuildresult;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.List;

/**
 * @author wolfs
 */
public class FailOnBuildResultBuilder extends Builder {
    public String getOtherProject() {
        return otherProject;
    }

    String otherProject;

    public FailOnBuildResultBuilder() {}

    @DataBoundConstructor
    public FailOnBuildResultBuilder(String otherProject) {
        this.otherProject = otherProject;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Job otherProjectJob = Hudson.getInstance().getItemByFullName(otherProject, Job.class);
        Run otherBuild = otherProjectJob.getLastBuild();
        Result buildResult = otherBuild.getResult();
        listener.getLogger().format("Result of build %s %s is %s.",
                otherProjectJob.getFullDisplayName(), otherBuild.getDisplayName(), buildResult);

        if (Result.SUCCESS.isBetterThan(buildResult)) {
            listener.getLogger().println("Result is worse than threshold - failing");
            return false;
        } else {
            return true;
        }
    }


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public FormValidation doCheckOtherProject(@QueryParameter String otherProject) {
            Job otherProjectJob = Hudson.getInstance().getItemByFullName(otherProject, Job.class);
            if (otherProjectJob != null) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Project " + otherProject + " does not exist!");
            }
        }

        @Override
        public String getDisplayName() {
            return "Fail if other project not successful";
        }

        public ComboBoxModel doFillOtherProjectItems() {
            ComboBoxModel model = new ComboBoxModel();
            List<Job> jobs = Hudson.getInstance().getItems(Job.class);
            for (Job job: jobs) {
                model.add(job.getFullName());
            }
            return model;
        }

    }

}
