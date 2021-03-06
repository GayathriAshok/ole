/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.gl.batch;

import java.util.HashMap;
import java.util.Map;

import org.kuali.ole.gl.batch.service.OrganizationReversionProcessService;
import org.kuali.ole.gl.batch.service.YearEndService;
import org.kuali.ole.sys.OLEConstants;
import org.kuali.ole.sys.batch.AbstractWrappedBatchStep;
import org.kuali.ole.sys.batch.service.WrappedBatchExecutorService.CustomBatchExecutor;
import org.springframework.util.StopWatch;

/**
 * A step that runs the reversion and carry forward process. The beginning of year version of the process is supposed to be run at
 * the beginning of a fiscal year, and therefore, it uses prior year accounts instead of current year accounts.
 */
public class OrganizationReversionCurrentYearAccountStep extends AbstractWrappedBatchStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrganizationReversionCurrentYearAccountStep.class);
    private OrganizationReversionProcessService organizationReversionProcessService;
    private YearEndService yearEndService;

    /**
     * @see org.kuali.ole.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
    @Override
    protected CustomBatchExecutor getCustomBatchExecutor() {
        return new CustomBatchExecutor() {
            /**
             * Runs the organization reversion process, retrieving parameter, creating the origin entry group for output entries, and
             * generating the reports on the process.
             * @return true if the job completed successfully, false if otherwise
             * @see org.kuali.ole.sys.batch.Step#execute(String, java.util.Date)
             */
            public boolean execute() {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("OrganizationReversionCurrentYearAccountStep");

                Map jobParameters = organizationReversionProcessService.getJobParameters();
                Map<String, Integer> organizationReversionCounts = new HashMap<String, Integer>();

                getYearEndService().logAllMissingSubFundGroups((Integer) jobParameters.get(OLEConstants.UNIV_FISCAL_YR));

                getOrganizationReversionProcessService().organizationReversionCurrentYearAccountProcess(jobParameters, organizationReversionCounts);

                stopWatch.stop();
                LOG.info("OrganizationReversionCurrentYearAccountStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");
                return true;
            }
        };
    }
    
    /**
     * Sets the organizationReversionProcessService (not to be confused with the OrganizationReversionService, which doesn't do a
     * process, but which does all the database stuff associated with OrganizationReversion records; it's off in Chart), which
     * allows the injection of an implementation of the service.
     * 
     * @param organizationReversionProcessService the implementation of the organizationReversionProcessService to set
     * @see org.kuali.ole.gl.batch.service.OrganizationReversionProcessService
     */
    public void setOrganizationReversionProcessService(OrganizationReversionProcessService organizationReversionProcessService) {
        this.organizationReversionProcessService = organizationReversionProcessService;
    }
    
    /**
     * Gets the yearEndService attribute. 
     * @return Returns the yearEndService.
     */
    public YearEndService getYearEndService() {
        return yearEndService;
    }

    /**
     * Sets the yearEndService attribute value.
     * @param yearEndService The yearEndService to set.
     */
    public void setYearEndService(YearEndService yearEndService) {
        this.yearEndService = yearEndService;
    }

    /**
     * Gets the organizationReversionProcessService attribute. 
     * @return Returns the organizationReversionProcessService.
     */
    public OrganizationReversionProcessService getOrganizationReversionProcessService() {
        return organizationReversionProcessService;
    }
}
