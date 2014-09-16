package hudson.plugins.broadcast;

import hudson.BulkChange;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.AbstractModelObject;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.model.listeners.SaveableListener;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.thoughtworks.xstream.XStream;

/**
 * Custom Message
 * @author Patryk Szczęch
 */
public class CustomMessage extends AbstractModelObject implements Saveable {

	public String name;
	public String message;
	public String color;
	public String icon;
	public String creator;
	public String modificator;
	public String contact;
	public Date createdDate;
	public Date modifiedDate;
	public Date startDate;
	public Date finishDate;
	public DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	
	/**
	 * CustomMessage constructor
	 * @param name
	 * @param message
	 * @param color
	 * @param icon
	 * @param contact
	 * @param startDate
	 * @param finishDate
	 */
	public CustomMessage(String name, String message, String color, String icon, String contact, Date startDate, Date finishDate) {
		String u;
		try {
        	u = User.current().getFullName();
        } catch (NullPointerException e) {
        	u = "unknown";
        	e.printStackTrace();
        }
		
        this.name = name;
        this.message = message;
        this.color = color;
        this.icon = icon;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.creator = u;
        this.modificator = u;
        this.contact = contact;
        this.createdDate = Calendar.getInstance().getTime();
        this.modifiedDate = Calendar.getInstance().getTime();
    }

	/**
	 * Name of the message
	 * @return name of the message
	 */
	public String getDisplayName() {
		return name;
	}
	
	/**
	 * Content of the message
	 * @return message content
	 */
	public String getDisplayMessage() {
		return message;
	}

	/**
	 * URL of the actual custom message
	 * @return path to custom message page
	 */
	public String getSearchUrl() {
        return name;
	}
	
	/**
	 * Gets the icon of message depending on status: green if active, grey if unactive
	 * @return icon
	 */
	public String getIconFileName() {
		if (Calendar.getInstance().getTime().after(startDate) && Calendar.getInstance().getTime().before(finishDate)) {
			return "green.png";
		} else {
			return "grey.png";
		}
	}
	
	/**
	 * Used to display the startDate in a proper format
	 * @return formatted date
	 */
	public String getFormatStartDate() {
		return df.format(this.startDate);
	}
	
	/**
	 * Used to display the finishDate in a proper format
	 * @return
	 */
	public String getFormatFinishDate() {
		return df.format(this.finishDate);
	}
	
	/**
	 * Used to overwrite message properties when configuration is changed
	 * @param req
	 * @param rsp
	 * @throws ServletException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, ParseException {
		JSONObject src = req.getSubmittedForm();

		String redirect = ".";
        String newName = src.getString("name");
        String newMessage = src.getString("message");
        String newColor = src.getString("color");
        String newIcon = src.getString("icon");
        String newContact = src.getString("contact");
        Date start = new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(src.getString("formatStartDate"));
        Date stop = new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(src.getString("formatFinishDate"));
        XmlFile oldFile = null;
        
        // check if current user is not null
        String u;
        try {
        	u = User.current().getFullName();
        } catch (NullPointerException e) {
        	u = "unknown";
        	e.printStackTrace();
        }
        
        if (!name.equals(newName) || !message.equals(newMessage) || !icon.equals(newIcon) || !color.equals(newColor) || !contact.equals(newContact) || !startDate.equals(start) || !finishDate.equals(stop)) {
            Hudson.checkGoodName(newName);
            oldFile = getConfigFile();
            // rename, remessage, redate
            CustomMessageManagementLink.customMessages.remove(name);
            this.name = newName;
            this.message = newMessage;
            this.color = newColor;
            this.icon = newIcon;
            this.contact = newContact;
            this.modificator = u;
            this.modifiedDate = Calendar.getInstance().getTime();
            this.startDate = start;
            this.finishDate = stop;
            CustomMessageManagementLink.customMessages.put(name, this);
            redirect = "../" + Util.rawEncode(newName) + '/';
        }

        save();
        if (oldFile != null) {
            oldFile.delete();
        }
        rsp.sendRedirect2(redirect);
	}
	
	/**
	 * Used to delete custom message
	 * @param rsp
	 * @throws IOException
	 * @throws ServletException
	 */
	public synchronized void doDeleteMessage(StaplerResponse rsp) throws IOException, ServletException {
        requirePOST();
        getConfigFile().delete();
        CustomMessageManagementLink.customMessages.remove(name);
        rsp.sendRedirect2("..");
    }
	
	/**
	 * Checks the status of message: active/unactive
	 * @return boolean status
	 */
	public boolean getCheckActive() {
		if (Calendar.getInstance().getTime().after(this.startDate) && Calendar.getInstance().getTime().before(this.finishDate)) {
			return true;
		}
		return false;
	}

	/**
	 * Saves configuration in the file
	 */
	public synchronized void save() throws IOException {
		if (BulkChange.contains(this)) {
            return;
        }
        getConfigFile().write(this);
        SaveableListener.fireOnChange(this, getConfigFile());
	}
	
	/**
	 * Loads configuration from the file
	 * @throws IOException
	 */
	public synchronized void load() throws IOException {
        getConfigFile().unmarshal(this);
    }
	
	/**
	 * Gets the proper config file
	 * @return
	 */
	private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(Hudson.getInstance().getRootDir(), "global-message/" + name + ".xml"));
    }
	
	public static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("global-message", CustomMessage.class);
    }
}