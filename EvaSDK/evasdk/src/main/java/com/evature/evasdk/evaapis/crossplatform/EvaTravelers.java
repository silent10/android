package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EvaTravelers  implements Serializable{
	private final static String TAG = "EvaTravellers";
	private Integer adult;        // null means it wasn't specified by the user,  0 means it was explicitly specified to zero by the user
	private Integer child;
	private Integer infant;
	private Integer elderly;

    public int[] childAges;

    public EvaTravelers() {

    }

	public EvaTravelers(JSONObject jTravelers, List<String> parseErrors) {
		try {
			if (jTravelers.has("Adult")) {
				adult = jTravelers.getInt("Adult");
			}
			if (jTravelers.has("Child")) {
				child = jTravelers.getInt("Child");
			}
			if (jTravelers.has("Infant")) {
				infant = jTravelers.getInt("Infant");
			}
			if (jTravelers.has("Elderly")) {
				elderly = jTravelers.getInt("Elderly");
			}
            if (jTravelers.has("child_ages")) {
                JSONArray jAges = jTravelers.getJSONArray("child_ages");
                childAges = new int[jAges.length()];
                for (int i=0; i<jAges.length(); i++) {
                    childAges[i] = jAges.getInt(i);
                }
            }
		} catch (JSONException e) {
			parseErrors.add("Error during parsing Travelers: "+e.getMessage());
			DLog.e(TAG, "Travelers Parse error ", e);
		}
	}

    /***
     * @return Integer number of adults (not elderly!) specified,  null if none were specified
     */
    public Integer sepcifiedAdults() {
        return adult;
    }

    /***
     * @return Integer number of children (not infants!) specified,  null if none were specified
     */
    public Integer sepcifiedChildren() {
        return child;
    }

    /***
     * @return Integer number of elderly (not adults!) specified,  null if none were specified
     */
    public Integer sepcifiedElderly() {
        return elderly;
    }

    /***
     * @return Integer number of infants (not children!) specified,  null if none were specified
     */
    public Integer sepcifiedInfants() {
        return infant;
    }

    /***
     * @return Total number of adults (adult+elderly) - if none are specified the result is zero
     */
	public int getAllAdults() {
        return getAdults() + getElderly();
	}

    /***
     * @return Total number of children (children+infants) - if none are specified the result is zero
     */
	public int getAllChildren() {
        return getChildren() + getInfants();
	}

    public int getAdults() {
        return adult == null ? 0 : adult.intValue();
    }
    public int getChildren() {
        return child == null ? 0 : child.intValue();
    }
    public int getElderly() {
        return elderly == null ? 0 : elderly.intValue();
    }
    public int getInfants() {
        return infant == null ? 0 : infant.intValue();
    }
}
