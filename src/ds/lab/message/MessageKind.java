package ds.lab.message;

/**
 * May not necessary
 * @author dmei
 *
 */
public enum MessageKind {
	LOOKUP {
		public String toString() {
			return "lookup";
		}
	},
	
	ACK {
		public String toString() {
			return "ack";
		}
	}, NONE {
		public String toString() {
			return "none";
		}
	}
}
