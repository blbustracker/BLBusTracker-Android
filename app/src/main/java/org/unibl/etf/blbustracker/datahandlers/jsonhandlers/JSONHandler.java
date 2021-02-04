package org.unibl.etf.blbustracker.datahandlers.jsonhandlers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONHandler
{
    protected JSONObject jsonObject;

    protected JSONHandler(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    protected JSONArray getJsonArray(String attribute)
    {
        try
        {
            return jsonObject.getJSONArray(attribute);
        } catch (JSONException ex)
        {
        }
        return null;
    }
}
