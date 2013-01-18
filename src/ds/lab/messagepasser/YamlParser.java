package ds.lab.messagepasser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import ds.lab.bean.NodeBean;


public class YamlParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String yfile = "lab0.yaml.txt";
		String test = "test";
		String yamlStr = "Name: Alice\nIP: 192.168.0.10\nPort: 12344";
		HashMap<String, NodeBean> nodeList = new HashMap<String, NodeBean>();
		try {
			int cnt = 0;
			DumperOptions options = new DumperOptions();
	        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	        Yaml yaml = new Yaml(options);
	        		NodeBean node = yaml.loadAs(yamlStr.toLowerCase(), NodeBean.class);
	        		System.out.println(node);
	        for (Object data : yaml.loadAll(new FileInputStream(new File(yfile)))) {
	        	System.out.println(data);
//	        	@SuppressWarnings("unchecked")
//				HashMap<String, ArrayList<String>> config = (HashMap<String, ArrayList<String>>) data;
//	        	Yaml beanLoader = new Yaml();
//	        	for (Object yamlStr : config.get("Configuration")) {
//	        		System.out.println(yamlStr);
//	        		NodeBean node = beanLoader.loadAs(yamlStr.toString().toLowerCase(), NodeBean.class);
//	        		nodeList.put(node.getName(), node);
//	        	}
	        	cnt++;
	        }
	        System.out.println(cnt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		}
		
	}

}
