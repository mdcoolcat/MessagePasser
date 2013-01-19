package ds.lab.messagepasser;

import java.util.ArrayList;
import java.util.HashMap;

import ds.lab.bean.NodeBean;
import ds.lab.bean.RuleBean;

public class Config {
	public static HashMap<String, NodeBean> NODELIST;
	public static ArrayList<RuleBean> SENDRULES;
	public static ArrayList<RuleBean> RECEIVERULES;
	
	public static void parseConfigFile(String configurationFile, String localName) {
		//TODO read file...nodelist..rules
	}
}
