package ds.lab.messagepasser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class Config {
	public  HashMap<String, NodeBean> NODELIST;// assume remain unchange. TODO port change..
	public ArrayList<RuleBean> sendRules;// may change
	public ArrayList<RuleBean> rcvRules;// may change

	public int NUM_NODE;// TODO get from configfile
	private String configurationFile;
	private String localName;

	public Config(String configurationFile, String localName) {
		this.configurationFile = configurationFile;
		this.localName = localName;
		
		this.parseConfigFile(this.configurationFile,this.localName);
	}
	
	public ArrayList<RuleBean> getSendRules() {
		parseagain(this.configurationFile,this.localName);
		return sendRules;
	}


	public ArrayList<RuleBean> getRcvRules() {
		parseagain(this.configurationFile,this.localName);
		return rcvRules;
	}
	private void parseagain(String configurationFile, String localName)
	{
		this.configurationFile=configurationFile;		//overwriting configfilename
		this.localName=localName;						//overwriting localname
	    parseConfigFile(this.configurationFile,this.localName);	
	}

	private void parseConfigFile(String configurationFile, String localName) {
		sendRules = new ArrayList<RuleBean>();
		rcvRules = new ArrayList<RuleBean>();
		NODELIST = new HashMap<String, NodeBean>();
		FileInputStream input = null;
		try {
			input = new FileInputStream(new File(configurationFile));
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			Yaml yaml = new Yaml(options);
			int keytype = 0;

			Map<String, ArrayList<Map<String, Object>>> obj = (Map<String, ArrayList<Map<String, Object>>>) yaml
					.load(input);
			for (Map.Entry<String, ArrayList<Map<String, Object>>> entry : obj.entrySet()) {
				try {
				if (keytype == 0) // for configuration
				{
					Iterator i = entry.getValue().iterator();
					while (i.hasNext()) {
						Map<String, Object> details = (Map<String, Object>) i.next();
						NodeBean nb = new NodeBean();
						for (Map.Entry<String, Object> innerdetails : details.entrySet()) {
							if (innerdetails.getKey().equalsIgnoreCase("Name"))
								nb.setName(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("IP"))
								nb.setIp(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("Port"))
								nb.setPort(Integer.parseInt((innerdetails.getValue().toString())));
						}
						NODELIST.put(nb.getName(), nb);
					}
					NUM_NODE=NODELIST.size();
				}

				if (keytype == 1) {
					Iterator i = entry.getValue().iterator();
					while (i.hasNext()) {
						Map<String, Object> details = (Map<String, Object>) i.next();
						RuleBean rb = new RuleBean();
						for (Map.Entry<String, Object> innerdetails : details.entrySet()) {
							if (innerdetails.getKey().equalsIgnoreCase("Action")) {
								if (innerdetails.getValue().toString().equalsIgnoreCase("Drop"))
									rb.setAction(MessageAction.DROP);
								if (innerdetails.getValue().toString().equalsIgnoreCase("Delay"))
									rb.setAction(MessageAction.DELAY);
								if (innerdetails.getValue().toString().equalsIgnoreCase("Duplicate"))
									rb.setAction(MessageAction.DUPLICATE);
							}

							if (innerdetails.getKey().equalsIgnoreCase("Src"))
								rb.setSrc(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("Dest"))
								rb.setDest(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("Kind")) {
								rb.setKind(innerdetails.getValue().toString());
//								if (innerdetails.getValue().toString().equalsIgnoreCase("Lookup"))
//									rb.setKind(MessageKind.LOOKUP);
//								if (innerdetails.getValue().toString().equalsIgnoreCase("Ack"))
//									rb.setKind(MessageKind.ACK);
//								if (innerdetails.getValue().toString().equalsIgnoreCase("None"))
//									rb.setKind(MessageKind.NONE);
							}
							if (innerdetails.getKey().equalsIgnoreCase("ID"))
								rb.setId(Integer.parseInt(innerdetails.getValue().toString()));
							if (innerdetails.getKey().equalsIgnoreCase("Nth"))
								rb.setNth(Integer.parseInt(innerdetails.getValue().toString()));

							if (innerdetails.getKey().equalsIgnoreCase("EveryNth"))
								rb.setEveryNth(Integer.parseInt(innerdetails.getValue().toString()));

						}
						sendRules.add(rb);
					}
				}

				if (keytype == 2) {
					Iterator i = entry.getValue().iterator();
					while (i.hasNext()) {
						Map<String, Object> details = (Map<String, Object>) i.next();
						RuleBean rb = new RuleBean();
						for (Map.Entry<String, Object> innerdetails : details.entrySet()) {
							if (innerdetails.getKey().equalsIgnoreCase("Action")) {
								if (innerdetails.getValue().toString().equalsIgnoreCase("Drop"))
									rb.setAction(MessageAction.DROP);
								if (innerdetails.getValue().toString().equalsIgnoreCase("Delay"))
									rb.setAction(MessageAction.DELAY);
								if (innerdetails.getValue().toString().equalsIgnoreCase("Duplicate"))
									rb.setAction(MessageAction.DUPLICATE);
							}

							if (innerdetails.getKey().equalsIgnoreCase("Src"))
								rb.setSrc(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("Dest"))
								rb.setDest(innerdetails.getValue().toString());
							if (innerdetails.getKey().equalsIgnoreCase("Kind"))
								rb.setKind(innerdetails.getValue().toString());
														
							if (innerdetails.getKey().equalsIgnoreCase("Nth"))
								rb.setNth(Integer.parseInt(innerdetails.getValue().toString()));

							if (innerdetails.getKey().equalsIgnoreCase("EveryNth"))
								rb.setEveryNth(Integer.parseInt(innerdetails.getValue().toString()));

							if (innerdetails.getKey().equalsIgnoreCase("ID"))
								rb.setId(Integer.parseInt(innerdetails.getValue().toString()));
						}

						rcvRules.add(rb);
					}
				}

				keytype++;
				} catch (NullPointerException e) {
					System.err.println("Config> No rule");
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}

	}
}
