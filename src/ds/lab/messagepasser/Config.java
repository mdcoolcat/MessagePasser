package ds.lab.messagepasser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import ds.lab.bean.NodeBean;
import ds.lab.bean.RuleBean;
import ds.lab.message.MessageAction;
import ds.lab.message.MessageKind;

public class Config {
	public static HashMap<String, NodeBean> NODELIST;
	public static ArrayList<RuleBean> SENDRULES;
	public static ArrayList<RuleBean> RECEIVERULES;
	public static int NUM_NODE = 4;//TODO get from configfile
	
	public static void parseConfigFile(String configurationFile, String localName) {
		//TODO read file...nodelist..rules
		NODELIST = new HashMap<String, NodeBean>();
		SENDRULES = new ArrayList<RuleBean>();
		RECEIVERULES = new ArrayList<RuleBean>();
		InputStream input;
		try {
			input = new FileInputStream(new File("Lab0.yaml"));
			DumperOptions options = new DumperOptions();
			 options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			 Yaml yaml = new Yaml(options);
			 int keytype=0;
			 
			 Map<String,ArrayList<Map<String,Object>>> obj = (Map<String,ArrayList<Map<String,Object>>>) yaml.load(input);
			 for (Map.Entry<String,ArrayList<Map<String,Object>>> entry : obj.entrySet())
			 {
				 if(keytype==0)		//for configuration
				 {
					 Iterator i = entry.getValue().iterator();
					 while (i.hasNext()) {
			             Map<String,Object> details = (Map<String,Object>)i.next(); 
			             NodeBean nb=new NodeBean();
			        	 for(Map.Entry<String,Object> innerdetails : details.entrySet())
			             {
			            	if(innerdetails.getKey().equalsIgnoreCase("Name"))
			            		nb.setName(innerdetails.getValue().toString());
			            	if(innerdetails.getKey().equalsIgnoreCase("IP"))
			            		nb.setIp(innerdetails.getValue().toString());
			            	if(innerdetails.getKey().equalsIgnoreCase("Port"))
			            		nb.setPort(Integer.parseInt((innerdetails.getValue().toString())));
			             }
			        	 NODELIST.put(nb.getName(), nb);
			         }
				 }
				 
				 if(keytype==1)
				 {
					 Iterator i = entry.getValue().iterator();
			         while (i.hasNext())
			         {
			             Map<String,Object> details = (Map<String,Object>)i.next();    
			        	 RuleBean rb=new RuleBean();
			             for(Map.Entry<String,Object> innerdetails : details.entrySet())
			             {
			            	 if(innerdetails.getKey().equalsIgnoreCase("Action"))
			            	 {
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Drop"))
			            			 rb.setAction(MessageAction.DROP);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Delay"))
			            			 rb.setAction(MessageAction.DELAY);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Duplicate"))
			            			 rb.setAction(MessageAction.DUPLICATE);
			               	 }		
			            	             
			            	 if(innerdetails.getKey().equalsIgnoreCase("Src"))
				            		rb.setSrc(innerdetails.getValue().toString());
				             if(innerdetails.getKey().equalsIgnoreCase("Dest"))
				            		rb.setDest(innerdetails.getValue().toString());
				             if(innerdetails.getKey().equalsIgnoreCase("Kind"))
				             {
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Lookup"))
			            			 rb.setKind(MessageKind.LOOKUP);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Ack"))
			            			 rb.setKind(MessageKind.ACK);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("None"))
			            			 rb.setKind(MessageKind.NONE);
			               	 }	
				             if(innerdetails.getKey().equalsIgnoreCase("ID"))
				            		 rb.setId(Integer.parseInt(innerdetails.getValue().toString()));
			             }
			             SENDRULES.add(rb);
			         }
				 }
				 
				 if(keytype==2)
				 {
					 Iterator i = entry.getValue().iterator();
			         while (i.hasNext())
			         {
			             Map<String,Object> details = (Map<String,Object>)i.next();    
			        	 RuleBean rb=new RuleBean();
			             for(Map.Entry<String,Object> innerdetails : details.entrySet())
			             {
			            	 if(innerdetails.getKey().equalsIgnoreCase("Action"))
			            	 {
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Drop"))
			            			 rb.setAction(MessageAction.DROP);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Delay"))
			            			 rb.setAction(MessageAction.DELAY);
			            		 if(innerdetails.getValue().toString().equalsIgnoreCase("Duplicate"))
			            			 rb.setAction(MessageAction.DUPLICATE);
			               	 }		
			            	             
			            	 if(innerdetails.getKey().equalsIgnoreCase("Src"))
				            		rb.setSrc(innerdetails.getValue().toString());
				             if(innerdetails.getKey().equalsIgnoreCase("Nth"))
				            		rb.setNth(Integer.parseInt(innerdetails.getValue().toString()));
			             }
			        	 
			        	 RECEIVERULES.add(rb);
			         } 
				 }
				 
				 keytype++;
			 }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			
		}

//		ArrayList<RuleBean> sendRules = new ArrayList<RuleBean>();
//		RuleBean r1 = new RuleBean(MessageAction.DUPLICATE);
//		r1.setDest("charlie");
//		r1.setNth(2);
//		sendRules.add(r1);
//		RuleBean r2 = new RuleBean(MessageAction.DELAY);
//		r2.setDest("alice");
//		r2.setEveryNth(2);
//		sendRules.add(r2);
//		SENDRULES = sendRules;
//		
//		ArrayList<RuleBean> receiveRules = new ArrayList<RuleBean>();
//		RuleBean r3 = new RuleBean(MessageAction.DELAY);
//		r3.setSrc("alice");
//		r3.setEveryNth(2);
//		receiveRules.add(r3);
//		RECEIVERULES = receiveRules;
	}
}
