package hardcorequesting.bag;

import java.io.Serializable;

public class GroupData implements Serializable {

	public int retrieved;

	public GroupData() {

	}

	public GroupData(int retrieved) {
		super();
		this.retrieved = retrieved;
	}

	/**
	 * @return the retrieved
	 */
	public int getRetrieved() {
		return retrieved;
	}

	/**
	 * @param retrieved
	 *            the retrieved to set
	 */
	public void setRetrieved(int retrieved) {
		this.retrieved = retrieved;
	}
}
