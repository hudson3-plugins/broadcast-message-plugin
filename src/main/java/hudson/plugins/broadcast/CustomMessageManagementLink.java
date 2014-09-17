/*
 * The MIT License
 *
 * Copyright (c) 2014, Patryk Szczęch
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package hudson.plugins.broadcast;

import static hudson.init.InitMilestone.PLUGINS_PREPARED;
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.ManagementLink;
import hudson.model.Hudson;
import hudson.util.CopyOnWriteMap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;

/**
 * Add the Custom Messages link to the Manage Hudson page.
 * @author Patryk Szczęch
 */
@Extension
public class CustomMessageManagementLink extends ManagementLink {

	public transient static final Map<String, CustomMessage> customMessages = new CopyOnWriteMap.Tree<String, CustomMessage>();
	public String name;
	
	/**
	 * Text displayed in Hudson Management panel
	 * @return title for the link to the custom messages
	 */
	public String getDisplayName() {
		return Messages.CustomMessageManagementLink_DisplayName();
	}

	/**
	 * Name of the icon displayed in Hudson Management panel
	 * @return icon name
	 */
	@Override
	public String getIconFileName() {
		return "notepad.gif";
	}

	/**
	 * URL name for all custom messages
	 * @return path to the custom messages panel
	 */
	@Override
	public String getUrlName() {
		return "global-message";
	}
	
	/**
	 * Description displayed in Hudson Management panel
	 * @return description of the link to the custom messages
	 */
	@Override
	public String getDescription() {
		return Messages.CustomMessageManagementLink_Description();
	}
	
	/**
	 * Allows to access custom message data by the message name in the url
	 * @param token
	 * @return path to the custom message
	 */
	public CustomMessage getDynamic(String token) {
        return customMessages.get(token);
    }
	
	/**
     * Creates a new custom message.
	 * @throws IOException 
	 * @throws ParseException 
     */
    public HttpResponse doNewCustomMessage(@QueryParameter String name, @QueryParameter String message, @QueryParameter String color, @QueryParameter String icon, @QueryParameter String contact, @QueryParameter String startDate, @QueryParameter String finishDate) throws IOException, ParseException {
    	Hudson.checkGoodName(name);
    	
    	// parse String from query to Date
    	Date start = new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(startDate);
    	Date stop = new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(finishDate);
    	
        CustomMessage cm = new CustomMessage(name, message, color, icon, contact, start, stop);
        cm.save();
    	customMessages.put(name, cm);        
        return new HttpRedirect("/" + getUrlName());
    }    
    
    /**
     * Loads all messages from config files
     * @throws IOException
     */
    public static void load() throws IOException {
        customMessages.clear();
       
        File dir = new File(Hudson.getInstance().getRootDir(), "global-message");
        File[] files = dir.listFiles((FileFilter) new WildcardFileFilter("*.xml"));
        if (files == null) {
            return;
        }
        
        for (File child : files) {
            String name = child.getName();
            name = name.substring(0, name.length() - 4);   // cut off ".xml"
            CustomMessage cm = new CustomMessage(name, null, null, null, null, null, null);
            cm.load();
            customMessages.put(name, cm);
        }
    }
	
    /**
     * Initialize all custom messages from files
     * @param h hudson
     * @throws IOException
     */
	@Initializer(before = PLUGINS_PREPARED)
    public static void init(Hudson h) throws IOException {
		customMessages.clear();
        load();
    }
    
}
