package ds.lab.messagepasser;

import java.util.ArrayList;
import java.util.HashMap;

import ds.lab.bean.NodeBean;
import ds.lab.bean.RuleBean;
import ds.lab.message.MessageAction;

public class Config {
	public static HashMap<String, NodeBean> NODELIST;
	public static ArrayList<RuleBean> SENDRULES;
	public static ArrayList<RuleBean> RECEIVERULES;
	public static int NUM_NODE = 4;//TODO get from configfile
	
	public static void parseConfigFile(String configurationFile, String localName) {
		//TODO read file...nodelist..rules
		ArrayList<RuleBean> rules = new ArrayList<RuleBean>();
		RuleBean r1 = new RuleBean(MessageAction.DUPLICATE);
		r1.setDest("charlie");
		r1.setNth(2);
		rules.add(r1);
		RuleBean r2 = new RuleBean(MessageAction.DELAY);
		r2.setDest("alice");
		r2.setEveryNth(1);
		rules.add(r2);
		SENDRULES = rules;
	}
}
