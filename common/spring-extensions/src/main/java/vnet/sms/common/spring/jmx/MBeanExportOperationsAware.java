/**
 * 
 */
package vnet.sms.common.spring.jmx;

import org.springframework.jmx.export.MBeanExportOperations;

/**
 * @author obergner
 * 
 */
public interface MBeanExportOperationsAware {

	void setMBeanExportOperations(MBeanExportOperations mbeanExportOperations);
}
