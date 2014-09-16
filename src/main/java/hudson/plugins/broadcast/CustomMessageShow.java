package hudson.plugins.broadcast;

import hudson.Extension;
import hudson.GlobalMessage;

import java.util.Map;
/**
 * Extension of the GlobalMessage, display active custom messages in Hudson
 * @author Patryk Szczęch
 */
@Extension
public class CustomMessageShow extends GlobalMessage {

	public Map<String, CustomMessage> customMessages = CustomMessageManagementLink.customMessages;
	boolean active = true;
	
	public boolean isEnabled() {
        return active;
    }
	
	/*private boolean doCheckActive() {
		
		return true;
	}*/
}