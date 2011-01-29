/*
 * Copyright (c) 2011 Stefan Wolf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tngtech.hudson.plugins.failonbuildresult;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
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
    private static final String FAIL_ON_BUILD_RESULT_LISTENER_PREFIX = "[FailOnBuildResult] ";

    public String getOtherJob() {
        return otherJob;
    }

    String otherJob;

    public FailOnBuildResultBuilder() {}

    @DataBoundConstructor
    public FailOnBuildResultBuilder(String otherJob) {
        this.otherJob = otherJob;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Job otherJobObject = Hudson.getInstance().getItemByFullName(otherJob, Job.class);
        Run otherBuild = otherJobObject.getLastCompletedBuild();
        if (otherBuild == null) {
            listener.getLogger().println(FAIL_ON_BUILD_RESULT_LISTENER_PREFIX +
                    Messages.FailOnBuildResultBuilder_OtherJobNeverBuilt(otherJob));
            return false;
        }
        Result buildResult = otherBuild.getResult();
        listener.getLogger().println(FAIL_ON_BUILD_RESULT_LISTENER_PREFIX +
                Messages.FailOnBuildResultBuilder_ResultOfBuildIs(otherBuild.getFullDisplayName(), buildResult));

        if (Result.SUCCESS.isBetterThan(buildResult)) {
            listener.getLogger().println(FAIL_ON_BUILD_RESULT_LISTENER_PREFIX +
                    Messages.FailOnBuildResultBuilder_ResultIsWorseThanThreshold());
            return false;
        } else {
            listener.getLogger().println(FAIL_ON_BUILD_RESULT_LISTENER_PREFIX +
                    Messages.FailOnBuildResultBuilder_ResultIsBetterOrEqualThanThreshold());
            return true;
        }
    }


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.FailOnBuildResultBuilder_DisplayName();
        }

        public FormValidation doCheckOtherJob(@QueryParameter String otherJob) {
            Job otherProjectJob = Hudson.getInstance().getItemByFullName(otherJob, Job.class);
            if (otherProjectJob != null) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.FailOnBuildResultBuilder_OtherJobValidation(otherJob));
            }
        }

        public ComboBoxModel doFillOtherJobItems() {
            ComboBoxModel model = new ComboBoxModel();
            List<Job> jobs = Hudson.getInstance().getItems(Job.class);
            for (Job job: jobs) {
                model.add(job.getFullName());
            }
            return model;
        }

    }

}
